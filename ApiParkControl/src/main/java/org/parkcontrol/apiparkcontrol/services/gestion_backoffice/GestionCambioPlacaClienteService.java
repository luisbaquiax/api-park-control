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
public class GestionCambioPlacaClienteService {
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
    private FileStorageService fileStorageService;
    @Autowired
    private S3StorageService s3StorageService;
    @Autowired
    private SuscripcionClienteService suscripcionClienteService;

    @Value("${s3.bucket.backend}")
    private String S3_BUCKET_BACKEND;

    @Value("${storage.path.local}")
    private String URL_BASE_LOCAL;

    @Value("${aws.region}")
    private String region;

    @Value("${storage.type}")
    private String storageType;

    //Nueva solicitud de cambio de placa por parte del cliente
    @Transactional
    public String crearNuevaSolicitudCambioPlaca(SolicitudCambioPlacaDTO solicitudCambioPlacaDTO, MultipartFile archivoEvidencia) throws Exception {
        //Buscar la suscripcion
        Suscripcion suscripcion = suscripcionRepository.findById(solicitudCambioPlacaDTO.getIdSuscripcion())
                .orElseThrow(() -> new Exception("Suscripción no encontrada con ID: " + solicitudCambioPlacaDTO.getIdSuscripcion()));

        //Vericamos que la suscripción este activa
        if (!suscripcion.getEstado().equals(Suscripcion.EstadoSuscripcion.ACTIVA)) {
            throw new Exception("La suscripción no está activa. No se puede procesar la solicitud de cambio de placa.");
        }

        //Buscar el vehiculo actual
        Vehiculo vehiculoActual = vehiculoRepository.findById(solicitudCambioPlacaDTO.getIdVehiculoActual())
                .orElseThrow(() -> new Exception("Vehículo no encontrado con ID: " + solicitudCambioPlacaDTO.getIdVehiculoActual()));

        //Revisamos que el vehiculo no pertenezca a ninguna otra suscripción activa
        //List<Suscripcion> suscripcionesActivas = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculoActual.getId(), Suscripcion.EstadoSuscripcion.ACTIVA);

        /*if(!suscripcionesActivas.isEmpty()){
            throw new Exception("El vehículo ya pertenece a otra suscripción activa. No se puede procesar la solicitud de cambio de placa.");
        }*/

        //Verificamos que el vehiculo pertenezca a la suscripcion indicada
        if (!suscripcion.getVehiculo().getId().equals(vehiculoActual.getId())) {
            throw new Exception("El vehículo no pertenece a la suscripción indicada.");
        }

        //Buscamos el vehiculo con la nueva placa para verificar que exista
        Vehiculo vehiculoConNuevaPlaca = vehiculoRepository.findByPlaca(solicitudCambioPlacaDTO.getPlacaNueva());
        if (vehiculoConNuevaPlaca == null) {
            throw new Exception("No existe un vehículo registrado con la nueva placa proporcionada.");
        }

        //Verificamos que la nueva placa no sea la misma que la actual
        if (vehiculoActual.getPlaca().equals(solicitudCambioPlacaDTO.getPlacaNueva())) {
            throw new Exception("La nueva placa no puede ser la misma que la placa actual.");
        }

        //Verificamos que la nueva placa no pertenezca a otra suscripción activa
        List<Suscripcion> suscripcionesConNuevaPlaca = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculoConNuevaPlaca.getId(), Suscripcion.EstadoSuscripcion.ACTIVA);
        if (!suscripcionesConNuevaPlaca.isEmpty()) {
            throw new Exception("La nueva placa ya pertenece a otra suscripción activa.");
        }

        //Revisar que no exista una solicitud pendiente para la misma suscripción
        List<SolicitudCambioPlaca> solicitudesPendientes = solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(suscripcion.getId(), SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE);
        if (!solicitudesPendientes.isEmpty()) {
            throw new Exception("Ya existe una solicitud de cambio de placa pendiente para esta suscripción.");
        }

        //Revisamos que no haya un cambio de placa del usuario en los ultimos 6 meses
        //Buscamos todas las solicitudes aprobadas del cliente
        List<SolicitudCambioPlaca> solicitudesAprobadas = solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(solicitudCambioPlacaDTO.getIdCliente(), SolicitudCambioPlaca.EstadoSolicitud.APROBADA);
        LocalDate seisMesesAtras = LocalDate.now().minusMonths(6);
        for (SolicitudCambioPlaca solicitud : solicitudesAprobadas) {
            if (solicitud.getFechaEfectiva() != null && solicitud.getFechaEfectiva().toLocalDate().isAfter(seisMesesAtras)) {
                throw new Exception("El cliente ya ha realizado un cambio de placa en los últimos 6 meses.");
            }
        }
        //Crear la nueva solicitud de cambio de placa
        SolicitudCambioPlaca solicitudCambioPlaca = new SolicitudCambioPlaca();
        solicitudCambioPlaca.setSuscripcion(suscripcion);
        solicitudCambioPlaca.setVehiculoActual(vehiculoActual);
        solicitudCambioPlaca.setPlacaNueva(solicitudCambioPlacaDTO.getPlacaNueva());
        solicitudCambioPlaca.setMotivo(SolicitudCambioPlaca.Motivo.valueOf(solicitudCambioPlacaDTO.getMotivo()));
        solicitudCambioPlaca.setDescripcionMotivo(solicitudCambioPlacaDTO.getDescripcionMotivo());
        solicitudCambioPlaca.setFechaSolicitud(LocalDateTime.now());
        solicitudCambioPlaca.setEstado(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE);
        solicitudCambioPlacaRepository.save(solicitudCambioPlaca);

        if (archivoEvidencia == null || archivoEvidencia.isEmpty()) {
            throw new Exception("El archivo de evidencia no puede estar vacio");
        }
        //Verificamos donde se va a guardar el archivo en local o en la nube
        String urlEvidencia = "";
        if (storageType.equals("s3")) {
            urlEvidencia = s3StorageService.uploadToS3(archivoEvidencia);
        } else {
            urlEvidencia = fileStorageService.getUrl(archivoEvidencia);
        }
        //Guardar la evidencia del cambio de placa
        EvidenciaCambioPlaca evidenciaCambioPlaca = new EvidenciaCambioPlaca();
        evidenciaCambioPlaca.setSolicitudCambioPlac(solicitudCambioPlaca);
        evidenciaCambioPlaca.setTipoDocumento(EvidenciaCambioPlaca.TipoDocumento.valueOf(solicitudCambioPlacaDTO.getTipoDocumento()));
        evidenciaCambioPlaca.setNombreArchivo(archivoEvidencia.getOriginalFilename());
        evidenciaCambioPlaca.setUrlDocumento(urlEvidencia);
        evidenciaCambioPlaca.setDescripcion(solicitudCambioPlacaDTO.getDescripcionEvidencia());
        evidenciaCambioPlaca.setFechaCarga(LocalDateTime.now());
        evidenciaCambioPlacaRepository.save(evidenciaCambioPlaca);

        return "Solicitud de cambio de placa creada con éxito con ID: " + solicitudCambioPlaca.getId();
    }

    //Obtener todas las solicitudes de cambio de placa de un cliente
    public List<DetalleSolicitudesCambioPlacaDTO> obtenerSolicitudesCambioPlacaCliente(Long idCliente) throws Exception {
        //Verificamos que el cliente exista
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + idCliente));

        //Obtenemos todas las solicitudes de cambio de placa del cliente
        List<SolicitudCambioPlaca> solicitudesCambioPlaca = solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(idCliente);
        List<DetalleSolicitudesCambioPlacaDTO> solicitudesDTO = new ArrayList<>();

        for (SolicitudCambioPlaca solicitud : solicitudesCambioPlaca) {
            DetalleSolicitudesCambioPlacaDTO dto = new DetalleSolicitudesCambioPlacaDTO();
            dto.setIdSolicitudCambio(solicitud.getId());
            dto.setPlacaNueva(solicitud.getPlacaNueva());
            dto.setMotivo(solicitud.getMotivo().name());
            dto.setDescripcionMotivo(solicitud.getDescripcionMotivo());
            dto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
            dto.setEstado(solicitud.getEstado().name());
            dto.setFechaRevision(solicitud.getFechaRevision() != null ? solicitud.getFechaRevision().toString() : null);
            dto.setObservacionesRevision(solicitud.getObservacionesRevision());
            dto.setFechaEfectiva(solicitud.getFechaEfectiva() != null ? solicitud.getFechaEfectiva().toString() : null);

            //Detalles de la suscripcion
            Suscripcion suscripcion = solicitud.getSuscripcion();
            PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaSuscripcionesDTO = suscripcionClienteService.obtenerEmpresaSuscripciones(suscripcion.getEmpresa());
            ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcionClienteDTO = new ClientePlanesSuscripcionDTO.SuscripcionClienteDTO(
                    suscripcion.getId(),
                    suscripcion.getPeriodoContratado().toString(),
                    suscripcion.getDescuentoAplicado().doubleValue(),
                    suscripcion.getPrecioPlan().doubleValue(),
                    suscripcion.getHorasMensualesIncluidas(),
                    suscripcion.getHorasConsumidas().doubleValue(),
                    suscripcion.getFechaInicio().toLocalDate().toString(),
                    suscripcion.getFechaFin().toLocalDate().toString(),
                    suscripcion.getFechaCompra().toLocalDate().toString(),
                    suscripcion.getEstado().toString(),
                    suscripcion.getTarifaBaseReferencia().getPrecioPorHora().doubleValue(),
                    null,//Debido a que este puede cambiar despues
                    empresaSuscripcionesDTO.getSuscripciones().stream()
                            .filter(plan -> plan.getId().equals(suscripcion.getTipoPlan().getId()))
                            .findFirst()
                            .orElse(null),
                    empresaSuscripcionesDTO.getSucursales()
            );

            dto.setSuscripcionCliente(suscripcionClienteDTO);

            //Detalles del vehiculo actual
            Vehiculo vehiculoActual = solicitud.getVehiculoActual();
            VehiculoClienteDTO vehiculoActualDTO = new VehiculoClienteDTO(
                    vehiculoActual.getId(),
                    vehiculoActual.getPlaca(),
                    vehiculoActual.getMarca(),
                    vehiculoActual.getModelo(),
                    vehiculoActual.getColor(),
                    vehiculoActual.getTipoVehiculo().name()
            );

            dto.setVehiculoActual(vehiculoActualDTO);
            //Detalles del vehiculo nuevo
            Vehiculo vehiculoNuevo = vehiculoRepository.findByPlaca(solicitud.getPlacaNueva());
            VehiculoClienteDTO vehiculoNuevoDTO = new VehiculoClienteDTO(
                    vehiculoNuevo.getId(),
                    vehiculoNuevo.getPlaca(),
                    vehiculoNuevo.getMarca(),
                    vehiculoNuevo.getModelo(),
                    vehiculoNuevo.getColor(),
                    vehiculoNuevo.getTipoVehiculo().name()
            );
            dto.setVehiculoNuevo(vehiculoNuevoDTO);
            //Detalles de la evidencia
            EvidenciaCambioPlaca evidencia = evidenciaCambioPlacaRepository.findBySolicitudCambioPlac_Id(solicitud.getId());
            String urlEvidencia = "";
            if (storageType.equals("local")) {
                urlEvidencia = URL_BASE_LOCAL + evidencia.getUrlDocumento();
            } else {
                urlEvidencia = "https://" + S3_BUCKET_BACKEND + ".s3." + region + ".amazonaws.com/" + evidencia.getUrlDocumento();

            }

            DetalleSolicitudesCambioPlacaDTO.DetalleEvidenciaCambioPlacaDTO evidenciaDTO = new DetalleSolicitudesCambioPlacaDTO.DetalleEvidenciaCambioPlacaDTO(
                    evidencia.getId(),
                    evidencia.getTipoDocumento().name(),
                    evidencia.getNombreArchivo(),
                    urlEvidencia,
                    evidencia.getDescripcion(),
                    evidencia.getFechaCarga().toString()
            );
            dto.setEvidenciaCambioPlaca(evidenciaDTO);

            solicitudesDTO.add(dto);
        }
        return solicitudesDTO;
    }


}
