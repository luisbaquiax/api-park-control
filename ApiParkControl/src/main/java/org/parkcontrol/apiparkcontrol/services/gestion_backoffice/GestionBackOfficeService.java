package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.*;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;
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
    @Autowired
    private PermisoTemporalRepository permisoTemporalRepository;
    @Autowired
    private SolicitudTemporalClienteService solicitudTemporalClienteService;

    @Autowired
    private EmailService emailService;

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

    //Obtener todas las solicitudes de permiso temporal
    public List<BackOfficeDetalleSolicitudesTemporalDTO> obtenerTodasSolicitudesPermisoTemporal(Long idUsuarioBackOffice) throws Exception {
        //Obtemos la Empresa del usuario de backoffice
        Usuario usuarioBackOffice = usuarioRepository.findById(idUsuarioBackOffice).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Empresa empresaBackOffice = backofficeRepository.findByUsuario_IdUsuario(usuarioBackOffice.getIdUsuario()).getFirst().getEmpresa();
        //Obtenemos todas las solicitudes de permiso temporal asociadas a la empresa
        List<PermisoTemporal> permisosTemporales = permisoTemporalRepository.findBySuscripcion_Empresa_IdEmpresa(empresaBackOffice.getIdEmpresa());

        //Filtrar todos los clientes de las solicitudes
        List<Long> idsClientes = new ArrayList<>();
        for (PermisoTemporal permisoTemporal : permisosTemporales) {
            Long idCliente = permisoTemporal.getSuscripcion().getUsuario().getIdUsuario();
            if (!idsClientes.contains(idCliente)) {
                idsClientes.add(idCliente);
            }
        }

        List<BackOfficeDetalleSolicitudesTemporalDTO> listaDetalleSolicitudesTemporal = new ArrayList<>();
        //Construir la lista de BackOfficeDetalleSolicitudesTemporalDTO
        for (Long idCliente : idsClientes) {
            Usuario cliente = usuarioRepository.findById(idCliente).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            BackOfficeDetalleSolicitudesTemporalDTO detalleSolicitudesTemporalDTO = new BackOfficeDetalleSolicitudesTemporalDTO();
            detalleSolicitudesTemporalDTO.setIdUsuario(cliente.getIdUsuario());
            detalleSolicitudesTemporalDTO.setNombreCompleto(cliente.getPersona().getNombre() + " " + cliente.getPersona().getApellido());
            detalleSolicitudesTemporalDTO.setEmail(cliente.getPersona().getCorreo());
            detalleSolicitudesTemporalDTO.setTelefono(cliente.getPersona().getTelefono());
            detalleSolicitudesTemporalDTO.setCui(cliente.getPersona().getDpi());
            detalleSolicitudesTemporalDTO.setDireccion(cliente.getPersona().getDireccionCompleta());
            //Obtenemos los detalles de las solicitudes de permiso temporal del cliente
            List<DetalleSolicitudesTemporalDTO> detalleSolicitudesTemporal = new ArrayList<>();
            detalleSolicitudesTemporal = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(idCliente);
            detalleSolicitudesTemporalDTO.setDetalleSolicitudesTemporal(detalleSolicitudesTemporal);
            listaDetalleSolicitudesTemporal.add(detalleSolicitudesTemporalDTO);
        }
        return listaDetalleSolicitudesTemporal;

    }

    //Aceptar  una solicitud de permiso temporal
    @Transactional
    public String aprobarSolicitudPermisoTemporal(ResolverSolicitudTemporalDTO resolverSolicitudPermiso) {
        //Validar que el usuario de backoffice exista
        Usuario usuarioBackOffice = usuarioRepository.findById(resolverSolicitudPermiso.getAprobadoPor())
                .orElseThrow(() -> new RuntimeException("Usuario de backoffice no encontrado"));
        //Validar que el permiso temporal exista
        PermisoTemporal permisoTemporal = permisoTemporalRepository.findById(resolverSolicitudPermiso.getIdSolicitudTemporal())
                .orElseThrow(() -> new RuntimeException("Permiso temporal no encontrado"));
        //Validar que el permiso temporal este en estado "Pendiente"
        if (!permisoTemporal.getEstado().equals(PermisoTemporal.EstadoPermiso.PENDIENTE)) {
            throw new RuntimeException("El permiso temporal ya ha sido revisado");
        }
        //Validamos fecha de inicio y fin
        LocalDate fechaInicio = LocalDate.parse(resolverSolicitudPermiso.getFechaInicio());
        LocalDate fechaFin = LocalDate.parse(resolverSolicitudPermiso.getFechaFin());
        if (fechaFin.isBefore(fechaInicio)) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        //Verificar que las sucursales existan
        if(!resolverSolicitudPermiso.getSucursalesAsignadas().isEmpty()){
            //separar los ids de las sucursales de comas
            String [] idsSucursales = resolverSolicitudPermiso.getSucursalesAsignadas().split(",");
            for (String idSucursalStr : idsSucursales) {
                Long idSucursal = Long.parseLong(idSucursalStr.trim());
                Sucursal sucursal = sucursalRepository.findById(idSucursal)
                        .orElseThrow(() -> new RuntimeException("Sucursal con ID " + idSucursal + " no encontrada"));
            }

        }

        //Actualizar el permiso temporal
        permisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);
        permisoTemporal.setFechaAprobacion(LocalDateTime.now());
        permisoTemporal.setAprobadoPor(usuarioBackOffice);
        permisoTemporal.setObservaciones(resolverSolicitudPermiso.getObservaciones());
        permisoTemporal.setFechaInicio(LocalDate.parse(resolverSolicitudPermiso.getFechaInicio()).atStartOfDay());
        permisoTemporal.setFechaFin(LocalDate.parse(resolverSolicitudPermiso.getFechaFin()).atStartOfDay());
        permisoTemporal.setUsosMaximos(resolverSolicitudPermiso.getUsosMaximos());
        permisoTemporal.setSucursalesValidas(resolverSolicitudPermiso.getSucursalesAsignadas());
        permisoTemporalRepository.save(permisoTemporal);

        //Enviar correo al cliente notificando la aprobacion del permiso temporal
        try{
            String correoCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getCorreo();
            String nombreCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getNombre();
            String asunto = "Permiso Temporal Aprobado";
            String mensaje = "Estimado/a " + nombreCliente + ",\n\n" +
                    "Nos complace informarle que su solicitud de permiso temporal ha sido aprobada.\n" +
                    "Detalles del permiso temporal:\n" +
                    "Placa Temporal: " + permisoTemporal.getPlacaTemporal() + "\n" +
                    "Tipo de Vehículo Permitido: " + permisoTemporal.getTipoVehiculoPermitido().name() + "\n" +
                    "Motivo: " + permisoTemporal.getMotivo() + "\n" +
                    "Fecha de Inicio: " + resolverSolicitudPermiso.getFechaInicio() + "\n" +
                    "Fecha de Fin: " + resolverSolicitudPermiso.getFechaFin() + "\n" +
                    "Usos Máximos: " + resolverSolicitudPermiso.getUsosMaximos() + "\n\n" +
                    "Gracias por confiar en nuestros servicios.\n\n" +
                    "Atentamente,\n" +
                    "El equipo de ParkControl";

            emailService.enviarEmailGenerico(correoCliente, asunto, mensaje);
        } catch (Exception e){
            //Si hay un error al enviar el correo, no se detiene el proceso
            throw new RuntimeException("Error al enviar correo de notificación: " + e.getMessage());

        }

        return "La solicitud de permiso temporal ha sido " + permisoTemporal.getEstado().name().toLowerCase()+ " exitosamente.";
    }
    /*
    Ejemplo json
    {
        "idSolicitudTemporal": 1,
        "aprobadoPor": 5,
        "observaciones": "Solicitud aprobada",
        "fechaInicio": "2025-07-01",
        "fechaFin": "2025-07-10",
        "usosMaximos": 5,
        "sucursalesAsignadas": "1"
    }
     */

    //Rechazar una solicitud de permiso temporal
    @Transactional
    public String rechazarSolicitudPermisoTemporal(ResolverSolicitudTemporalDTO resolverSolicitudPermiso) {
        //Validar que el usuario de backoffice exista
        Usuario usuarioBackOffice = usuarioRepository.findById(resolverSolicitudPermiso.getAprobadoPor())
                .orElseThrow(() -> new RuntimeException("Usuario de backoffice no encontrado"));
        //Validar que el permiso temporal exista
        PermisoTemporal permisoTemporal = permisoTemporalRepository.findById(resolverSolicitudPermiso.getIdSolicitudTemporal())
                .orElseThrow(() -> new RuntimeException("Permiso temporal no encontrado"));
        //Validar que el permiso temporal este en estado "Pendiente"
        if (!permisoTemporal.getEstado().equals(PermisoTemporal.EstadoPermiso.PENDIENTE)) {
            throw new RuntimeException("El permiso temporal ya ha sido revisado");
        }
        //Actualizar el permiso temporal
        permisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.RECHAZADO);
        permisoTemporal.setFechaAprobacion(LocalDateTime.now());
        permisoTemporal.setAprobadoPor(usuarioBackOffice);
        permisoTemporal.setObservaciones(resolverSolicitudPermiso.getObservaciones());
        permisoTemporalRepository.save(permisoTemporal);

        //Enviar correo al cliente notificando el rechazo del permiso temporal
        try{
            String correoCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getCorreo();
            String nombreCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getNombre();
            String asunto = "Permiso Temporal Rechazado";
            String mensaje = "Estimado/a " + nombreCliente + ",\n\n" +
                    "Lamentamos informarle que su solicitud de permiso temporal ha sido rechazada.\n" +
                    "Motivo del rechazo: " + resolverSolicitudPermiso.getObservaciones() + "\n\n" +
                    "Si tiene alguna pregunta o necesita más información, no dude en contactarnos.\n\n" +
                    "Atentamente,\n" +
                    "El equipo de ParkControl";
            emailService.enviarEmailGenerico(correoCliente, asunto, mensaje);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de notificación: " + e.getMessage());
        }

        return "La solicitud de permiso temporal ha sido rechazada exitosamente.";
    }

    //Cancelar una solicitud de permiso temporal debido a irregularidades
    @Transactional
    public String revocarSolicitudPermisoTemporal(ResolverSolicitudTemporalDTO resolverSolicitudPermiso) {
        //Validar que el permiso temporal exista
        PermisoTemporal permisoTemporal = permisoTemporalRepository.findById(resolverSolicitudPermiso.getIdSolicitudTemporal())
                .orElseThrow(() -> new RuntimeException("Permiso temporal no encontrado"));
        //Validar que el permiso temporal este en estado "Activo"
        if (!permisoTemporal.getEstado().equals(PermisoTemporal.EstadoPermiso.ACTIVO)) {
            throw new RuntimeException("El permiso temporal no está activo y no puede ser cancelado");
        }
        //Actualizar el permiso temporal
        permisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.REVOCADO);
        permisoTemporal.setObservaciones(resolverSolicitudPermiso.getObservaciones());
        permisoTemporalRepository.save(permisoTemporal);

        try {
            //Enviar correo al cliente notificando la cancelacion del permiso temporal
            String correoCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getCorreo();
            String nombreCliente = permisoTemporal.getSuscripcion().getUsuario().getPersona().getNombre();
            String asunto = "Permiso Temporal Cancelado";
            String mensaje = "Estimado/a " + nombreCliente + ",\n\n" +
                    "Le informamos que su permiso temporal ha sido cancelado debido a irregularidades.\n" +
                    "Motivo de la cancelación: " + resolverSolicitudPermiso.getObservaciones() + "\n\n" +
                    "Si tiene alguna pregunta o necesita más información, no dude en contactarnos.\n\n" +
                    "Atentamente,\n" +
                    "El equipo de ParkControl";
            emailService.enviarEmailGenerico(correoCliente, asunto, mensaje);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo de notificación: " + e.getMessage());
        }

        return "La solicitud de permiso temporal ha sido cancelada exitosamente.";
    }


}