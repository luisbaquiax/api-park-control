package org.parkcontrol.apiparkcontrol.services.planes_suscripcion;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanesSuscripcionService {
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
    private BitacoraConfiguracionDescuentoRepository bitacoraConfiguracionDescuentoRepository;;

    //Obtenemos todos los planes de suscripcion vigentes de una empresa
    public List<DetalleTipoPlanDTO> obtenerPlanesSuscripcionPorEmpresa(Long idUsuario) {
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada para el usuario con ID: " + idUsuario));
        List<TipoPlan> tiposPlan = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(empresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        List<DetalleTipoPlanDTO> detalleTipoPlanDTOS = new ArrayList<>();
        for (TipoPlan tipoPlan : tiposPlan) {
            ConfiguracionDescuentoPlan configDescuento = configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(tipoPlan.getId(), ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
            DetalleTipoPlanDTO detalleTipoPlanDTO = new DetalleTipoPlanDTO(
                    tipoPlan.getId(),
                    tipoPlan.getEmpresa().getIdEmpresa(),
                    tipoPlan.getNombrePlan().toString(),
                    tipoPlan.getCodigoPlan(),
                    tipoPlan.getDescripcion(),
                    tipoPlan.getHorasMensuales(),
                    tipoPlan.getDiasAplicables(),
                    tipoPlan.getCoberturaHoraria(),
                    tipoPlan.getOrdenBeneficio(),
                    tipoPlan.getActivo().toString(),
                    tipoPlan.getFechaCreacion().toString(),
                    null // Inicialmente nulo, se llenará más adelante
            );

            DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configDescuentoDTO = null;
            if (configDescuento != null) {
                configDescuentoDTO = new DetalleTipoPlanDTO.ConfiguracionDescuentoDTO(
                        configDescuento.getId(),
                        configDescuento.getDescuentoMensual().doubleValue(),
                        configDescuento.getDescuentoAnualAdicional().doubleValue(),
                        configDescuento.getFechaVigenciaInicio().toString(),
                        configDescuento.getFechaVigenciaFin().toString(),
                        configDescuento.getEstado().toString(),
                        configDescuento.getCreadoPor().getIdUsuario(),
                        configDescuento.getFechaCreacion().toString());
            }
            detalleTipoPlanDTO.setConfiguracionDescuento(configDescuentoDTO);
            detalleTipoPlanDTOS.add(detalleTipoPlanDTO);
        }

        return detalleTipoPlanDTOS;
    }

    //Nuevo plan de suscripcion para una empresa
    @Transactional
    public String crearNuevoPlanSuscripcion(NuevoPlanDTO nuevoTipoPlanDTO) {
        Empresa empresa = empresaRepository.findById(nuevoTipoPlanDTO.getIdEmpresa()).orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + nuevoTipoPlanDTO.getIdEmpresa()));

        //Verificar que no exista un plan con el mismo nombre en la empresa
        List<TipoPlan> tiposPlanExistentes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(empresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        for (TipoPlan tipoPlanExistente : tiposPlanExistentes) {
            if (tipoPlanExistente.getNombrePlan().toString().equals(nuevoTipoPlanDTO.getNombrePlan())) {
                throw new RuntimeException("Ya existe un plan con el nombre " + nuevoTipoPlanDTO.getNombrePlan() + " para la empresa con ID: " + empresa.getIdEmpresa());
            }
        }
        TipoPlan nuevoTipoPlan = new TipoPlan();
        nuevoTipoPlan.setEmpresa(empresa);
        nuevoTipoPlan.setNombrePlan(TipoPlan.NombrePlan.valueOf(nuevoTipoPlanDTO.getNombrePlan()));
        nuevoTipoPlan.setCodigoPlan(nuevoTipoPlanDTO.getCodigoPlan());
        nuevoTipoPlan.setDescripcion(nuevoTipoPlanDTO.getDescripcion());
        nuevoTipoPlan.setHorasMensuales(nuevoTipoPlanDTO.getHorasMensuales());
        nuevoTipoPlan.setDiasAplicables(nuevoTipoPlanDTO.getDiasAplicables());
        nuevoTipoPlan.setCoberturaHoraria(nuevoTipoPlanDTO.getCoberturaHoraria());
        nuevoTipoPlan.setOrdenBeneficio(obtenerOrdenBeneficio(nuevoTipoPlanDTO.getNombrePlan()));
        nuevoTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        TipoPlan planActual = tipoPlanRepository.save(nuevoTipoPlan);

        //Crear configuracion de descuento
        ConfiguracionDescuentoPlan nuevaConfigDescuento = new ConfiguracionDescuentoPlan();
        nuevaConfigDescuento.setTipoPlan(planActual);
        nuevaConfigDescuento.setDescuentoMensual(BigDecimal.valueOf(nuevoTipoPlanDTO.getDescuentoMensual()));
        nuevaConfigDescuento.setDescuentoAnualAdicional(BigDecimal.valueOf(nuevoTipoPlanDTO.getDescuentoAnualAdicional()));
        nuevaConfigDescuento.setFechaVigenciaInicio(LocalDate.parse(nuevoTipoPlanDTO.getFechaVigenciaInicio()).atStartOfDay());
        nuevaConfigDescuento.setFechaVigenciaFin(LocalDate.parse(nuevoTipoPlanDTO.getFechaVigenciaFin()).atStartOfDay());
        Usuario usuarioCreador = usuarioRepository.findById(nuevoTipoPlanDTO.getIdUsuarioCreacion()).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + nuevoTipoPlanDTO.getIdUsuarioCreacion()));
        nuevaConfigDescuento.setCreadoPor(usuarioCreador);
        nuevaConfigDescuento.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        //Validar los descuentos antes de guardar
        boolean validacionDescuentos = validarPorcentajesDescuento(
                nuevoTipoPlanDTO.getDescuentoMensual(),
                nuevoTipoPlanDTO.getNombrePlan(),
                nuevoTipoPlanDTO.getDescuentoAnualAdicional(),
                empresa.getIdEmpresa()
        );
        if (!validacionDescuentos) {
            throw new RuntimeException("Los porcentajes de descuento no cumplen con las reglas de negocio para la empresa con ID: " + empresa.getIdEmpresa());
        }

        ConfiguracionDescuentoPlan conf = configuracionDescuentoPlanRepository.save(nuevaConfigDescuento);

        //Registrar en la bitacora
        BitacoraConfiguracionDescuento bitacora = new BitacoraConfiguracionDescuento();
        bitacora.setConfiguracionDescuento(conf);
        bitacora.setAccion(BitacoraConfiguracionDescuento.Accion.CREACION);
        bitacora.setDescuentoMensualNuevo(conf.getDescuentoMensual());
        bitacora.setDescuentoAnualNuevo(conf.getDescuentoAnualAdicional());
        bitacora.setUsuarioResponsable(usuarioCreador);
        bitacora.setObservaciones("Creación de nueva configuración de descuento para el plan "+ nuevoTipoPlanDTO.getNombrePlan());
        bitacoraConfiguracionDescuentoRepository.save(bitacora);


        return "Nuevo plan de suscripción creado con éxito para la empresa con ID: " + empresa.getIdEmpresa();
    }
    /*
    Ejemplo JSON para crear un nuevo plan de suscripcion
    {
        "idEmpresa": 1,
        "nombrePlan": "WORKWEEK",
        "codigoPlan": "WW-001",
        "descripcion": "Plan Workweek para uso de lunes a viernes",
        "horasMensuales": 160,
        "diasAplicables": "Lunes a Viernes",
        "coberturaHoraria": "08:00 - 18:00",
        "descuentoMensual": 15.00,
        "descuentoAnualAdicional": 6.00,
        "fechaVigenciaInicio": "2025-07-01",
        "fechaVigenciaFin": "2026-06-30",
        "idUsuarioCreacion": 2
    }
     */

    //Editar un plan de suscripcion existente (se generará un nuevo registro de plan y de configuracion)

    @Transactional
    public String editarPlanSuscripcion(NuevoPlanDTO editarPlan){
        TipoPlan tipoPlanExistente = tipoPlanRepository.findById(editarPlan.getIdTipoPlan())
                .orElseThrow(() -> new RuntimeException("Tipo de plan no encontrado con ID: " + editarPlan.getIdTipoPlan()));

        //Marcar el plan existente como historico
        tipoPlanExistente.setActivo(TipoPlan.EstadoConfiguracion.HISTORICO);
        tipoPlanRepository.save(tipoPlanExistente);

        //Crear nuevo plan con los datos editados
        TipoPlan nuevoTipoPlan = new TipoPlan();
        nuevoTipoPlan.setEmpresa(tipoPlanExistente.getEmpresa());
        nuevoTipoPlan.setNombrePlan(TipoPlan.NombrePlan.valueOf(editarPlan.getNombrePlan()));
        nuevoTipoPlan.setCodigoPlan(editarPlan.getCodigoPlan());
        nuevoTipoPlan.setDescripcion(editarPlan.getDescripcion());
        nuevoTipoPlan.setHorasMensuales(editarPlan.getHorasMensuales());
        nuevoTipoPlan.setDiasAplicables(editarPlan.getDiasAplicables());
        nuevoTipoPlan.setCoberturaHoraria(editarPlan.getCoberturaHoraria());
        nuevoTipoPlan.setOrdenBeneficio(obtenerOrdenBeneficio(editarPlan.getNombrePlan()));
        nuevoTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        TipoPlan planActual = tipoPlanRepository.save(nuevoTipoPlan);

        //Marcar la configuracion de descuento vigente como historica
        ConfiguracionDescuentoPlan configDescuentoExistente = configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(tipoPlanExistente.getId(), ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        if (configDescuentoExistente != null) {
            configDescuentoExistente.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.HISTORICO);
            configuracionDescuentoPlanRepository.save(configDescuentoExistente);
        }

        //Crear nueva configuracion de descuento
        ConfiguracionDescuentoPlan nuevaConfigDescuento = new ConfiguracionDescuentoPlan();
        nuevaConfigDescuento.setTipoPlan(planActual);
        nuevaConfigDescuento.setDescuentoMensual(BigDecimal.valueOf(editarPlan.getDescuentoMensual()));
        nuevaConfigDescuento.setDescuentoAnualAdicional(BigDecimal.valueOf(editarPlan.getDescuentoAnualAdicional()));
        nuevaConfigDescuento.setFechaVigenciaInicio(LocalDate.parse(editarPlan.getFechaVigenciaInicio()).atStartOfDay());
        nuevaConfigDescuento.setFechaVigenciaFin(LocalDate.parse(editarPlan.getFechaVigenciaFin()).atStartOfDay());
        Usuario usuarioCreador = usuarioRepository.findById(editarPlan.getIdUsuarioCreacion()).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + editarPlan.getIdUsuarioCreacion()));
        nuevaConfigDescuento.setCreadoPor(usuarioCreador);
        nuevaConfigDescuento.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);

        //Validar los descuentos antes de guardar
        boolean validacionDescuentos = validarPorcentajesDescuento(
                editarPlan.getDescuentoMensual(),
                editarPlan.getNombrePlan(),
                editarPlan.getDescuentoAnualAdicional(),
                tipoPlanExistente.getEmpresa().getIdEmpresa()
        );
        if (!validacionDescuentos) {
            throw new RuntimeException("Los porcentajes de descuento no cumplen con las reglas de negocio para la empresa con ID: " + tipoPlanExistente.getEmpresa().getIdEmpresa());
        }

        ConfiguracionDescuentoPlan nuevaConf= configuracionDescuentoPlanRepository.save(nuevaConfigDescuento);

        //Registrar en la bitacora
        BitacoraConfiguracionDescuento bitacora = new BitacoraConfiguracionDescuento();
        bitacora.setConfiguracionDescuento(nuevaConf);
        bitacora.setAccion(BitacoraConfiguracionDescuento.Accion.ACTUALIZACION);
        bitacora.setDescuentoMensualAnterior(configDescuentoExistente.getDescuentoMensual());
        bitacora.setDescuentoMensualNuevo(nuevaConf.getDescuentoMensual());
        bitacora.setDescuentoAnualAnterior(configDescuentoExistente.getDescuentoAnualAdicional());
        bitacora.setDescuentoAnualNuevo(nuevaConf.getDescuentoAnualAdicional());
        bitacora.setUsuarioResponsable(usuarioCreador);
        bitacora.setObservaciones("Edición de configuración de descuento para el plan "+ editarPlan.getNombrePlan());
        bitacoraConfiguracionDescuentoRepository.save(bitacora);

        return "Plan de suscripción editado con éxito para la empresa con ID: " + tipoPlanExistente.getEmpresa().getIdEmpresa();
    }
    /*
    Ejemplo JSON para editar un plan de suscripcion
    {
        "idTipoPlan": 3,
        "idEmpresa": 1,
        "nombrePlan": "WORKWEEK",
        "codigoPlan": "WW-002",
        "descripcion": "Plan Workweek actualizado para uso de lunes a viernes",
        "horasMensuales": 170,
        "diasAplicables": "Lunes a Viernes",
        "coberturaHoraria": "08:00 - 19:00",
        "descuentoMensual": 16.00,
        "descuentoAnualAdicional": 7.00,
        "fechaVigenciaInicio": "2025-08-01",
        "fechaVigenciaFin": "2026-07-31",
        "idUsuarioCreacion": 2
    }
     */

    //Obtener el orden de beneficio según el nombre del plan
    private Integer obtenerOrdenBeneficio(String nombrePlan) {
        switch (nombrePlan) {
            case "FULL_ACCESS":
                return 1;
            case "WORKWEEK":
                return 2;
            case "OFFICE_LIGHT":
                return 3;
            case "DIARIO_FLEXIBLE":
                return 4;
            case "NOCTURNO":
                return 5;
            default:
                throw new RuntimeException("Nombre de plan no válido: " + nombrePlan);
        }
    }

    /*Los porcentajes deben ser todos distintos y mantener el orden de beneficio: Full
Access > Workweek > Office Light > Diario Flexible > Nocturno.*/
    //Metodo para validar los porcentajes de descuento

    public boolean validarPorcentajesDescuento(Double porcentajeMensual, String nombrePlan, Double porcentajeAnual, Long idEmpresa) {
        List<TipoPlan> tiposPlan = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(idEmpresa, TipoPlan.EstadoConfiguracion.VIGENTE);
        Integer ordenBeneficioNuevoPlan = obtenerOrdenBeneficio(nombrePlan);
        for (TipoPlan tipoPlan : tiposPlan) {
            ConfiguracionDescuentoPlan configDescuento = configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(tipoPlan.getId(), ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
            if (configDescuento != null) {
                Integer ordenBeneficioExistente = tipoPlan.getOrdenBeneficio();
                Double porcentajeMensualExistente = configDescuento.getDescuentoMensual().doubleValue();
                Double porcentajeAnualExistente = configDescuento.getDescuentoAnualAdicional().doubleValue();

                if (ordenBeneficioNuevoPlan < ordenBeneficioExistente) {
                    // Nuevo plan tiene mayor beneficio, sus porcentajes deben ser mayores
                    if (porcentajeMensual <= porcentajeMensualExistente || porcentajeAnual <= porcentajeAnualExistente) {
                        return false;
                    }
                } else if (ordenBeneficioNuevoPlan > ordenBeneficioExistente) {
                    // Nuevo plan tiene menor beneficio, sus porcentajes deben ser menores
                    if (porcentajeMensual >= porcentajeMensualExistente || porcentajeAnual >= porcentajeAnualExistente) {
                        return false;
                    }
                } else {
                    // Mismo orden de beneficio, no permitido
                    return false;
                }
            }
        }
        return true;
    }




}
