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
public class SolicitudTemporalClienteService {
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
    @Autowired
    private PermisoTemporalRepository permisoTemporalRepository;

    //Nuevo permiso temporal
    @Transactional
    public String solicitarPermisoTemporal(SolicitarPermisoTemporalDTO solicitarPermisoTemporalDTO) {
        //Validamos que la suscripcion exista
        Suscripcion suscripcion = suscripcionRepository.findById(solicitarPermisoTemporalDTO.getIdSuscripcion())
                .orElseThrow(() -> new RuntimeException("Suscripcion no encontrada"));

        //Validamos que la suscripcion este activa
        if (!suscripcion.getEstado().equals(Suscripcion.EstadoSuscripcion.ACTIVA)) {
            throw new RuntimeException("La suscripcion no esta activa");
        }

        //Validamos que la placa este registrada
        Vehiculo vehiculo = vehiculoRepository.findByPlaca(solicitarPermisoTemporalDTO.getPlacaTemporal());
        if (vehiculo == null) {
            throw new RuntimeException("La placa temporal no esta registrada en el sistema");
        }

        //Creamos el permiso temporal
        PermisoTemporal permisoTemporal = new PermisoTemporal();
        permisoTemporal.setSuscripcion(suscripcion);
        permisoTemporal.setPlacaTemporal(solicitarPermisoTemporalDTO.getPlacaTemporal());
        permisoTemporal.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.valueOf(solicitarPermisoTemporalDTO.getTipoVehiculoPermitido()));
        permisoTemporal.setMotivo(solicitarPermisoTemporalDTO.getMotivo());
        permisoTemporal.setUsosRealizados(0);
        permisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE);
        permisoTemporalRepository.save(permisoTemporal);
        return "Solicitud de permiso temporal creada exitosamente";

    }

    //Detalles de permiso temporales
    public List<DetalleSolicitudesTemporalDTO> obtenerDetallesPermisosTemporales(Long idCliente){
        List<DetalleSolicitudesTemporalDTO> detalleSolicitudesTemporalDTOList = new ArrayList<>();

        //Obtenemos las suscripciones del cliente
        List<Suscripcion> suscripcionList = suscripcionRepository.findByUsuario_IdUsuario(idCliente);

        for (Suscripcion suscripcion : suscripcionList) {
            //Obtenemos los permisos temporales de la suscripcion
            List<PermisoTemporal> permisoTemporalList = permisoTemporalRepository.findBySuscripcion_Id(suscripcion.getId());

            for (PermisoTemporal permisoTemporal : permisoTemporalList) {
                DetalleSolicitudesTemporalDTO detalleSolicitudesTemporalDTO = new DetalleSolicitudesTemporalDTO();
                detalleSolicitudesTemporalDTO.setIdPermisoTemporal(permisoTemporal.getId());
                detalleSolicitudesTemporalDTO.setPlacaTemporal(permisoTemporal.getPlacaTemporal());
                detalleSolicitudesTemporalDTO.setTipoVehiculoPermitido(permisoTemporal.getTipoVehiculoPermitido().name());
                detalleSolicitudesTemporalDTO.setMotivo(permisoTemporal.getMotivo());
                detalleSolicitudesTemporalDTO.setFechaInicio(permisoTemporal.getFechaInicio() != null ? permisoTemporal.getFechaInicio().toString() : null);
                detalleSolicitudesTemporalDTO.setFechaFin(permisoTemporal.getFechaFin() != null ? permisoTemporal.getFechaFin().toString() : null);
                detalleSolicitudesTemporalDTO.setUsosMaximos(permisoTemporal.getUsosMaximos());
                detalleSolicitudesTemporalDTO.setUsosRealizados(permisoTemporal.getUsosRealizados());
                detalleSolicitudesTemporalDTO.setEstado(permisoTemporal.getEstado().name());
                detalleSolicitudesTemporalDTO.setFechaAprobacion(permisoTemporal.getFechaAprobacion() != null ? permisoTemporal.getFechaAprobacion().toString() : null);
                detalleSolicitudesTemporalDTO.setObservaciones(permisoTemporal.getObservaciones());

                //Detalles de la suscripcion
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
                        null,
                        empresaSuscripcionesDTO.getSuscripciones().stream()
                                .filter(plan -> plan.getId().equals(suscripcion.getTipoPlan().getId()))
                                .findFirst()
                                .orElse(null),
                        empresaSuscripcionesDTO.getSucursales()
                );
                detalleSolicitudesTemporalDTO.setSuscripcionCliente(suscripcionClienteDTO);

                //Sucursales validas
                List<ObtenerSucursalesEmpresaDTO.SucursalDTO> sucursalesDisponibles = new ArrayList<>();
                if (permisoTemporal.getSucursalesValidas() != null && !permisoTemporal.getSucursalesValidas().isEmpty()) {
                    String[] sucursalIds = permisoTemporal.getSucursalesValidas().split(",");
                    for (String sucursalId : sucursalIds) {
                        Long idSucursal = Long.parseLong(sucursalId.trim());
                        Sucursal sucursal = sucursalRepository.findById(idSucursal).orElse(null);
                        if (sucursal != null) {
                            ObtenerSucursalesEmpresaDTO.SucursalDTO sucursalDTO = new ObtenerSucursalesEmpresaDTO.SucursalDTO();
                            sucursalDTO.setIdSucursal(sucursal.getIdSucursal());
                            sucursalDTO.setNombre(sucursal.getNombre());
                            sucursalDTO.setDireccionCompleta(sucursal.getDireccionCompleta());
                            sucursalesDisponibles.add(sucursalDTO);
                        }
                    }
                }
                detalleSolicitudesTemporalDTO.setSucursalesDisponiblesPermiso(sucursalesDisponibles);

                detalleSolicitudesTemporalDTOList.add(detalleSolicitudesTemporalDTO);
            }
        }

        return detalleSolicitudesTemporalDTOList;
    }

}
