package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.*;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GestionBackOfficeService {
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private BitacoraConfiguracionDescuentoRepository bitacoraConfiguracionDescuentoRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;
    @Autowired
    private SolicitudCambioPlacaRepository solicitudCambioPlacaRepository;
    @Autowired
    private EvidenciaCambioPlacaRepository evidenciaCambioPlacaRepository;
    @Autowired
    private GestionCambioPlacaClienteService gestionCambioPlacaClienteService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private S3StorageService s3StorageService;
    @Autowired
    private SuscripcionClienteService suscripcionClienteService;
    @Autowired
    private BackofficeRepository backofficeRepository;

    //Obtener todas las solicitudes de cambio de placa
    public List<BackOfficeDetalleSolicitudes> obtenerTodasSolicitudesCambioPlaca(Long idUsuarioBackOffice) throws Exception {
        //Obtemos la Empresa del usuario de backoffice
        Usuario usuarioBackOffice = usuarioRepository.findById(idUsuarioBackOffice).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Empresa empresaBackOffice = backofficeRepository.findByUsuario_IdUsuario(usuarioBackOffice.getIdUsuario()).getFirst().getEmpresa();
        //Obtenemos todas las solicitudes de cambio de placa asociadas a la empresa
        List<SolicitudCambioPlaca> solicitudesCambioPlaca = solicitudCambioPlacaRepository.findBySuscripcion_Empresa_IdEmpresa(empresaBackOffice.getIdEmpresa());
        //Filtrar todos los clientes de las solicitudes
        List<Long> idsClientes = new ArrayList<>();
        for (SolicitudCambioPlaca solicitud : solicitudesCambioPlaca) {
            Long idCliente = solicitud.getSuscripcion().getUsuario().getIdUsuario();
            if (!idsClientes.contains(idCliente)) {
                idsClientes.add(idCliente);
            }
        }
        List<BackOfficeDetalleSolicitudes> listaDetalleSolicitudes = new ArrayList<>();

        //Construir la lista de BackOfficeDetalleSolicitudes
        for (Long idCliente : idsClientes) {
            Usuario cliente = usuarioRepository.findById(idCliente).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            BackOfficeDetalleSolicitudes detalleSolicitudes = new BackOfficeDetalleSolicitudes();
            detalleSolicitudes.setIdUsuario(cliente.getIdUsuario());
            detalleSolicitudes.setNombreCompleto(cliente.getPersona().getNombre() + " " + cliente.getPersona().getApellido());
            detalleSolicitudes.setEmail(cliente.getPersona().getCorreo());
            detalleSolicitudes.setTelefono(cliente.getPersona().getTelefono());
            detalleSolicitudes.setCui(cliente.getPersona().getDpi());
            detalleSolicitudes.setDireccion(cliente.getPersona().getDireccionCompleta());


            detalleSolicitudes.setDetalleSolicitudesCambioPlaca(gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(idCliente));
            listaDetalleSolicitudes.add(detalleSolicitudes);
        }
        return listaDetalleSolicitudes;
    }

    //Aceptar o rechazar una solicitud de cambio de placa
    @Transactional
    public String revisarSolicitudCambioPlaca(ResolverSolicitudCambioDTO revisarSolicitudCambioDTO) throws Exception {
        //Validar que el usuario de backoffice exista
        Usuario usuarioBackOffice = usuarioRepository.findById(revisarSolicitudCambioDTO.getIdUsuarioBackoffice())
                .orElseThrow(() -> new RuntimeException("Usuario de backoffice no encontrado"));
        //Validar que la solicitud de cambio de placa exista
        SolicitudCambioPlaca solicitudCambioPlaca = solicitudCambioPlacaRepository.findById(revisarSolicitudCambioDTO.getIdSolicitudCambio())
                .orElseThrow(() -> new RuntimeException("Solicitud de cambio de placa no encontrada"));
        //Validar que la solicitud este en estado "Pendiente"
        if (!solicitudCambioPlaca.getEstado().equals(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE)) {
            throw new RuntimeException("La solicitud de cambio de placa ya ha sido revisada");
        }
        //Actualizar la solicitud de cambio de placa
        solicitudCambioPlaca.setEstado(SolicitudCambioPlaca.EstadoSolicitud.valueOf(revisarSolicitudCambioDTO.getEstado()));
        solicitudCambioPlaca.setFechaRevision(LocalDateTime.now());
        solicitudCambioPlaca.setObservacionesRevision(revisarSolicitudCambioDTO.getObservacionesRevision());
        solicitudCambioPlaca.setRevisadoPor(usuarioBackOffice);
        solicitudCambioPlaca.setFechaEfectiva(LocalDateTime.now());
        solicitudCambioPlacaRepository.save(solicitudCambioPlaca);

        //Si la solicitud es aprobada, actualizar el vehiculo y la suscripcion
        if (revisarSolicitudCambioDTO.getEstado().equals("APROBADA")) {
            //Actualizar el vehiculo en la suscripcion
            Suscripcion suscripcion = solicitudCambioPlaca.getSuscripcion();
            //Obtenemos el vehiculo
            Vehiculo vehiculoNuevo = vehiculoRepository.findByPlaca(solicitudCambioPlaca.getPlacaNueva());
            suscripcion.setVehiculo(vehiculoNuevo);
            suscripcionRepository.save(suscripcion);
        }

        return "La solicitud de cambio de placa ha sido " + revisarSolicitudCambioDTO.getEstado().toLowerCase() + " exitosamente.";
    }

}
