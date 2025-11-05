package org.parkcontrol.apiparkcontrol.services.empresa_flotilla;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.VehiculoClienteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GestionEmpresaFlotillaService {
    @Autowired
    private  EmpresaRepository empresaRepository;
    @Autowired
    private  SucursalRepository sucursalRepository;
    @Autowired
    private  UsuarioRepository usuarioRepository;
    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private EmpresaFlotillaRepository empresaFlotillaRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private PlanCorporativoRepository planCorporativoRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private SuscripcionFlotillaRepository suscripcionFlotillaRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;

    //Nueva empresa de flotilla
    @Transactional
    public String nuevaEmpresaFlotilla(NuevaEmpresaFlotillaDTO nuevaEmpresaFlotillaDTO) {
        // Crear y guardar la empresa
        EmpresaFlotilla empresaFlotilla = new EmpresaFlotilla();
        empresaFlotilla.setNombreEmpresa(nuevaEmpresaFlotillaDTO.getNombreEmpresa());
        empresaFlotilla.setDireccion(nuevaEmpresaFlotillaDTO.getDireccion());
        empresaFlotilla.setNit(nuevaEmpresaFlotillaDTO.getNit());
        empresaFlotilla.setRazonSocial(nuevaEmpresaFlotillaDTO.getRazonSocial());
        empresaFlotilla.setTelefono(nuevaEmpresaFlotillaDTO.getTelefono());
        empresaFlotilla.setCorreoContacto(nuevaEmpresaFlotillaDTO.getCorreoContacto());
        empresaFlotilla.setEstado(EmpresaFlotilla.EstadoEmpresaFlotilla.ACTIVA);
        empresaFlotilla.setFechaRegistro(LocalDateTime.now());
        empresaFlotillaRepository.save(empresaFlotilla);

        return "Nueva empresa de flotilla creada con éxito.";
    }

    //Nuevo plan corporativo
    @Transactional
    public String nuevoPlanCorporativo(NuevoPlanCorporativoDTO nuevoPlanCorporativoDTO) {
        EmpresaFlotilla empresaFlotilla = empresaFlotillaRepository.findById(nuevoPlanCorporativoDTO.getIdEmpresaFlotilla()).orElseThrow(() -> new RuntimeException("Empresa de flotilla no encontrada."));
        TipoPlan tipoPlan = tipoPlanRepository.findById(nuevoPlanCorporativoDTO.getIdTipoPlan()).orElseThrow(() -> new RuntimeException("Tipo de plan no encontrado."));
        //Revisar si ya existe un plan corporativo activo para la empresa de flotilla
        PlanCorporativo planCorporativo = planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(empresaFlotilla.getIdEmpresaFlotilla(), PlanCorporativo.EstadoPlanCorporativo.ACTIVO);
        if (planCorporativo != null) {
            throw new RuntimeException("Ya existe un plan corporativo activo para esta empresa de flotilla.");
        }

        // Crear y guardar el plan corporativo
        PlanCorporativo nuevoPlan = new PlanCorporativo();
        nuevoPlan.setEmpresaFlotilla(empresaFlotilla);
        nuevoPlan.setTipoPlan(tipoPlan);
        nuevoPlan.setNombrePlanCorporativo(nuevoPlanCorporativoDTO.getNombrePlanCorporativo());
        nuevoPlan.setNumeroPlacasContratadas(nuevoPlanCorporativoDTO.getNumeroPlacasContratadas());
        nuevoPlan.setDescuentoCorporativoAdicional(BigDecimal.valueOf(nuevoPlanCorporativoDTO.getDescuentoCorporativoAdicional()));
        nuevoPlan.setPrecioPlanCorporativo(BigDecimal.valueOf(nuevoPlanCorporativoDTO.getPrecioPlanCorporativo()));
        nuevoPlan.setFechaInicio(LocalDate.parse(nuevoPlanCorporativoDTO.getFechaInicio()).atStartOfDay());
        nuevoPlan.setFechaFin(LocalDate.parse(nuevoPlanCorporativoDTO.getFechaFin()).atStartOfDay());
        Usuario creadoPor = usuarioRepository.findById(nuevoPlanCorporativoDTO.getIdCreadoPor()).orElseThrow(() -> new RuntimeException("Usuario creador no encontrado."));
        nuevoPlan.setCreadoPor(creadoPor);
        nuevoPlan.setFechaCreacion(LocalDateTime.now());
        nuevoPlan.setEstado(PlanCorporativo.EstadoPlanCorporativo.ACTIVO);
        planCorporativoRepository.save(nuevoPlan);
        return "Nuevo plan corporativo creado con éxito.";
    }

    //Activar plan corporativo
    @Transactional
    public String activarPlanCorporativo(Long idPlanCorporativo) {
        PlanCorporativo planCorporativo = planCorporativoRepository.findById(idPlanCorporativo).orElseThrow(() -> new RuntimeException("Plan corporativo no encontrado."));
        if (planCorporativo.getEstado() == PlanCorporativo.EstadoPlanCorporativo.ACTIVO) {
            throw new RuntimeException("El plan corporativo ya está activo.");
        }
        //Verificar que no exista otro plan corporativo activo para la misma empresa de flotilla
        PlanCorporativo planExistente = planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(planCorporativo.getEmpresaFlotilla().getIdEmpresaFlotilla(), PlanCorporativo.EstadoPlanCorporativo.ACTIVO);
        if (planExistente != null) {
            throw new RuntimeException("Ya existe un plan corporativo activo para esta empresa de flotilla.");
        }

        planCorporativo.setEstado(PlanCorporativo.EstadoPlanCorporativo.ACTIVO);
        planCorporativoRepository.save(planCorporativo);
        return "Plan corporativo activado con éxito.";
    }

    //Desactivar plan corporativo
    @Transactional
    public String desactivarPlanCorporativo(Long idPlanCorporativo) {
        PlanCorporativo planCorporativo = planCorporativoRepository.findById(idPlanCorporativo).orElseThrow(() -> new RuntimeException("Plan corporativo no encontrado."));
        if (planCorporativo.getEstado() == PlanCorporativo.EstadoPlanCorporativo.CANCELADO) {
            throw new RuntimeException("El plan corporativo ya está inactivo.");
        }
        planCorporativo.setEstado(PlanCorporativo.EstadoPlanCorporativo.CANCELADO);
        planCorporativoRepository.save(planCorporativo);
        return "Plan corporativo desactivado con éxito.";
    }

    //Suscribir vehículo a plan corporativo
    @Transactional
    public String suscribirVehiculoPlanCorporativo(SuscripcionFlotillaDTO suscripcionFlotillaDTO) {
        PlanCorporativo planCorporativo = planCorporativoRepository.findById(suscripcionFlotillaDTO.getIdPlanCorporativo()).orElseThrow(() -> new RuntimeException("Plan corporativo no encontrado."));
        Vehiculo vehiculo = vehiculoRepository.findById(suscripcionFlotillaDTO.getIdVehiculo()).orElseThrow(() -> new RuntimeException("Vehículo no encontrado."));

        //Verificar que el vehículo no esté ya suscrito a ningun otro plan corporativo activo
        SuscripcionFlotilla suscripcionExistente = suscripcionFlotillaRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), SuscripcionFlotilla.EstadoSuscripcion.ACTIVA);
        if (suscripcionExistente != null) {
            throw new RuntimeException("El vehículo ya está suscrito a este plan corporativo.");
        }

        //Verificar que el vehiculo no esta en un plan de suscripcion individual activo
        List<Suscripcion> suscripcionVehiculo = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), Suscripcion.EstadoSuscripcion.ACTIVA);
        if (!suscripcionVehiculo.isEmpty()) {
            throw new RuntimeException("El vehículo ya está suscrito a un plan individual.");
        }

        // Crear y guardar la suscripción flotilla
        SuscripcionFlotilla nuevaSuscripcion = new SuscripcionFlotilla();
        nuevaSuscripcion.setPlanCorporativo(planCorporativo);
        nuevaSuscripcion.setVehiculo(vehiculo);
        nuevaSuscripcion.setFechaAsignacion(LocalDateTime.now());
        nuevaSuscripcion.setEstado(SuscripcionFlotilla.EstadoSuscripcion.ACTIVA);
        suscripcionFlotillaRepository.save(nuevaSuscripcion);
        return "Vehículo suscrito al plan corporativo con éxito.";
    }

    //Cancelar suscripción de vehículo a plan corporativo
    @Transactional
    public String cancelarSuscripcionVehiculoPlanCorporativo(Long idSuscripcionFlotilla) {
        SuscripcionFlotilla suscripcionFlotilla = suscripcionFlotillaRepository.findById(idSuscripcionFlotilla).orElseThrow(() -> new RuntimeException("Suscripción de flotilla no encontrada."));
        if (suscripcionFlotilla.getEstado() == SuscripcionFlotilla.EstadoSuscripcion.INACTIVA) {
            throw new RuntimeException("La suscripción de flotilla ya está inactiva.");
        }
        suscripcionFlotilla.setEstado(SuscripcionFlotilla.EstadoSuscripcion.INACTIVA);
        suscripcionFlotillaRepository.save(suscripcionFlotilla);
        return "Suscripción de vehículo a plan corporativo cancelada con éxito.";
    }

    //Obtener detalle de empresas de flotilla
    public DetalleEmpresaFlotillaDTO obtenerDetalleEmpresasFlotilla(Long idUsuarioEmpresa) {
        // Obtener la empresa asociada al usuario
        List<Empresa> empresas = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa);
        if (empresas.isEmpty()) {
            throw new RuntimeException("Empresa no encontrada para el usuario proporcionado.");
        }
        Empresa empresaGeneral = empresas.get(0);


        //La unica forma de acceder a la empresa es atraves del PlanCorporativo a traves del tipo de plan
        List<EmpresaFlotilla> empresasFlotilla = empresaFlotillaRepository.findAll();
        List<DetalleEmpresaFlotillaDTO.EmpresaFlotillaDTO> empresaFlotillaDTOList = new ArrayList<>();
        for (EmpresaFlotilla empresa : empresasFlotilla) {
            DetalleEmpresaFlotillaDTO.EmpresaFlotillaDTO empresaDTO = new DetalleEmpresaFlotillaDTO.EmpresaFlotillaDTO();
            empresaDTO.setIdEmpresaFlotilla(empresa.getIdEmpresaFlotilla());
            empresaDTO.setNombreEmpresa(empresa.getNombreEmpresa());
            empresaDTO.setRazonSocial(empresa.getRazonSocial());
            empresaDTO.setNit(empresa.getNit());
            empresaDTO.setTelefono(empresa.getTelefono());
            empresaDTO.setCorreoContacto(empresa.getCorreoContacto());
            empresaDTO.setDireccion(empresa.getDireccion());
            empresaDTO.setEstado(empresa.getEstado().name());
            empresaDTO.setFechaRegistro(empresa.getFechaRegistro());

            // Obtener planes corporativos asociados a la empresa de flotilla
            List<PlanCorporativo> planesCorporativos = planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(empresa.getIdEmpresaFlotilla(), empresaGeneral.getIdEmpresa());
            List<DetalleEmpresaFlotillaDTO.PlanCorporativoDTO> planCorporativoDTOList = new ArrayList<>();

            for (PlanCorporativo plan : planesCorporativos) {
                DetalleEmpresaFlotillaDTO.PlanCorporativoDTO planDTO = new DetalleEmpresaFlotillaDTO.PlanCorporativoDTO();
                planDTO.setIdPlanCorporativo(plan.getIdPlanCorporativo());
                planDTO.setNombrePlanCorporativo(plan.getNombrePlanCorporativo());
                planDTO.setNumeroPlacasContratadas(plan.getNumeroPlacasContratadas());
                planDTO.setTipoPlan(String.valueOf(plan.getTipoPlan().getNombrePlan()));
                planDTO.setDescuentoCorporativoAdicional(plan.getDescuentoCorporativoAdicional().doubleValue());
                planDTO.setPrecioPlanCorporativo(plan.getPrecioPlanCorporativo().doubleValue());
                planDTO.setEstado(plan.getEstado().name());
                planDTO.setFechaInicio(plan.getFechaInicio());
                planDTO.setFechaFin(plan.getFechaFin());
                planDTO.setCreadoPor(plan.getCreadoPor().getNombreUsuario());
                planDTO.setFechaCreacion(plan.getFechaCreacion());

                // Obtener suscripciones de vehículos asociadas al plan corporativo
                List<SuscripcionFlotilla> suscripcionesVehiculos = suscripcionFlotillaRepository.findByPlanCorporativo_IdPlanCorporativo(plan.getIdPlanCorporativo());
                List<DetalleEmpresaFlotillaDTO.SuscripcionVehiculoDTO> suscripcionVehiculoDTOList = new ArrayList<>();
                for (SuscripcionFlotilla suscripcion : suscripcionesVehiculos) {
                    DetalleEmpresaFlotillaDTO.SuscripcionVehiculoDTO suscripcionDTO = new DetalleEmpresaFlotillaDTO.SuscripcionVehiculoDTO();
                    suscripcionDTO.setIdSuscripcionFlotilla(suscripcion.getIdSuscripcionFlotilla());
                    suscripcionDTO.setPlacaVehiculo(suscripcion.getVehiculo().getPlaca());
                    suscripcionDTO.setFechaAsignacion(suscripcion.getFechaAsignacion());
                    suscripcionDTO.setEstado(suscripcion.getEstado().name());
                    suscripcionVehiculoDTOList.add(suscripcionDTO);
                }
                planDTO.setSuscripcionesVehiculos(suscripcionVehiculoDTOList);
                planCorporativoDTOList.add(planDTO);
            }
            empresaDTO.setPlanesCorporativos(planCorporativoDTOList);
            empresaFlotillaDTOList.add(empresaDTO);
        }
        DetalleEmpresaFlotillaDTO detalleDTO = new DetalleEmpresaFlotillaDTO();
        detalleDTO.setEmpresasFlotilla(empresaFlotillaDTOList);
        return detalleDTO;

    }

    //Obtenemos los vehiculos del cliente
    public List<VehiculoClienteDTO> obtenerVehiculos() {


        List<Vehiculo> vehiculos = vehiculoRepository.findAll();
        List<VehiculoClienteDTO> vehiculoClienteDTOS = new ArrayList<>();

        for (Vehiculo vehiculo : vehiculos) {
            VehiculoClienteDTO vehiculoClienteDTO = new VehiculoClienteDTO(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getColor(),
                    vehiculo.getTipoVehiculo().toString()
            );
            vehiculoClienteDTOS.add(vehiculoClienteDTO);
        }

        return vehiculoClienteDTOS;
    }


}
