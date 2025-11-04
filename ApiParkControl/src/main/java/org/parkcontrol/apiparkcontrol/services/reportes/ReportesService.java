package org.parkcontrol.apiparkcontrol.services.reportes;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.DetalleEmpresaFlotillaDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.DetalleSucursalesIncidenciasDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetalleTransaccionTicketDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetallesLiquidacionesDTO;
import org.parkcontrol.apiparkcontrol.services.empresa_flotilla.GestionEmpresaFlotillaService;
import org.parkcontrol.apiparkcontrol.services.gestion_incidencias.ResolucionIncidenciasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
import org.parkcontrol.apiparkcontrol.dto.reportes.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ReportesService {
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
    private OcupacionSucursalRepository ocupacionSucursalRepository;
    @Autowired
    private TransaccionTicketRepository transaccionTicketRepository;
    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Autowired
    private CorteCajaRepository corteCajaRepository;
    @Autowired
    private LiquidacionComercioRepository liquidacionComercioRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;
    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private ResolucionIncidenciasService resolucionIncidenciasService;
    @Autowired
    private GestionEmpresaFlotillaService gestionEmpresaFlotillaService;


    /*
    Reporte de ocupación por sucursal y tipo de vehículo (2R/4R): muestra porcentaje de uso frente a la
capacidad declarada en cada sucursal.
     */
    public List<ReporteOcupacionDTO> generarReporteOcupacionPorSucursal(Long idUsuarioEmpresa) {
        // Obtener todas las sucursales de la empresa asociada al usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();

        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());
        List<ReporteOcupacionDTO> reportes = new ArrayList<>();

        for (Sucursal sucursal : sucursales) {
            ReporteOcupacionDTO reporte = new ReporteOcupacionDTO();
            reporte.setIdSucursal(sucursal.getIdSucursal());
            reporte.setNombreSucursal(sucursal.getNombre());
            reporte.setDireccionCompleta(sucursal.getDireccionCompleta());
            reporte.setCiudad(sucursal.getCiudad());
            reporte.setDepartamento(sucursal.getDepartamento());
            reporte.setHoraApertura(sucursal.getHoraApertura().toString());
            reporte.setHoraCierre(sucursal.getHoraCierre().toString());
            reporte.setCapacidad2Ruedas(sucursal.getCapacidad2Ruedas());
            reporte.setCapacidad4Ruedas(sucursal.getCapacidad4Ruedas());

            // Obtener la última ocupación registrada para la sucursal
            OcupacionSucursal ultimaOcupacion = ocupacionSucursalRepository
                    .findTopBySucursalOrderByFechaHoraDesc(sucursal);

            if (ultimaOcupacion != null) {
                ReporteOcupacionDTO.DetalleOcupacionDTO detalle = new ReporteOcupacionDTO.DetalleOcupacionDTO();
                detalle.setFechaHora(ultimaOcupacion.getFechaHora().toString());
                detalle.setOcupacion2R(ultimaOcupacion.getOcupacion2R());
                detalle.setCapacidad2R(ultimaOcupacion.getCapacidad2R());
                detalle.setOcupacion4R(ultimaOcupacion.getOcupacion4R());
                detalle.setCapacidad4R(ultimaOcupacion.getCapacidad4R());
                //Calcular porcentaje por si acaso
                BigDecimal porcentaje2R = BigDecimal.ZERO;
                BigDecimal porcentaje4R = BigDecimal.ZERO;
                if (ultimaOcupacion.getCapacidad2R() != 0) {
                    porcentaje2R = BigDecimal.valueOf(ultimaOcupacion.getOcupacion2R())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(ultimaOcupacion.getCapacidad2R()), 2, BigDecimal.ROUND_HALF_UP);
                }
                if (ultimaOcupacion.getCapacidad4R() != 0) {
                    porcentaje4R = BigDecimal.valueOf(ultimaOcupacion.getOcupacion4R())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(ultimaOcupacion.getCapacidad4R()), 2, BigDecimal.ROUND_HALF_UP);
                }

                detalle.setPorcentajeOcupacion2R(porcentaje2R.toString() + "%");
                detalle.setPorcentajeOcupacion4R(porcentaje4R.toString() + "%");

                reporte.setDetallesOcupacion(detalle);
            }

            reportes.add(reporte);
        }

        return reportes;
    }

    /*
    Reporte de facturación por sucursal: detalle de ingresos generados por cobros a clientes sin
suscripción y excedentes de suscriptores.
     */

    public List<ReportesFacturacionSucursalDTO> generarReporteFacturacionPorSucursal(Long idUsuarioEmpresa) {
        // Este es un ejemplo básico y debe ajustarse según la lógica de negocio y los datos disponibles

        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());
        List<ReportesFacturacionSucursalDTO> reportes = new ArrayList<>();

        for (Sucursal sucursal : sucursales) {
            ReportesFacturacionSucursalDTO reporte = new ReportesFacturacionSucursalDTO();
            reporte.setIdSucursal(sucursal.getIdSucursal());
            reporte.setNombreSucursal(sucursal.getNombre());
            reporte.setDireccionCompleta(sucursal.getDireccionCompleta());
            reporte.setCiudad(sucursal.getCiudad());
            reporte.setDepartamento(sucursal.getDepartamento());
            reporte.setHoraApertura(sucursal.getHoraApertura().toString());
            reporte.setHoraCierre(sucursal.getHoraCierre().toString());
            reporte.setCapacidad2Ruedas(sucursal.getCapacidad2Ruedas());
            reporte.setCapacidad4Ruedas(sucursal.getCapacidad4Ruedas());

            // Obtener el detalle de transacciones de tickets para la sucursal
            List<TransaccionTicket> transacciones = transaccionTicketRepository.findByTicket_Sucursal_IdSucursal(sucursal.getIdSucursal());
            List<DetalleTransaccionTicketDTO> detallesFacturacion = new ArrayList<>();
            for (TransaccionTicket transaccionTicket : transacciones) {
                DetalleTransaccionTicketDTO detalleTransaccionTicketDTO = new DetalleTransaccionTicketDTO();
                detalleTransaccionTicketDTO.setIdTransaccion(transaccionTicket.getIdTransaccion());
                detalleTransaccionTicketDTO.setIdTicket(transaccionTicket.getTicket().getId());
                if(transaccionTicket.getTicket().getSuscripcion() != null){
                    detalleTransaccionTicketDTO.setNombreCliente(transaccionTicket.getTicket().getSuscripcion().getUsuario().getPersona().getNombre()) ;

                }else{
                    detalleTransaccionTicketDTO.setNombreCliente("Cliente ocasional");
                }
                detalleTransaccionTicketDTO.setTipoCobro(transaccionTicket.getTipoCobro().toString());
                detalleTransaccionTicketDTO.setHorasCobradas(transaccionTicket.getHorasCobradas().toString());
                detalleTransaccionTicketDTO.setHorasGratisComercio(transaccionTicket.getHorasGratisComercio().toString());
                detalleTransaccionTicketDTO.setTarifaAplicada(transaccionTicket.getTarifaAplicada().toString());
                detalleTransaccionTicketDTO.setSubtotal(transaccionTicket.getSubtotal().toString());
                detalleTransaccionTicketDTO.setDescuento(transaccionTicket.getDescuento().toString());
                detalleTransaccionTicketDTO.setTotal(transaccionTicket.getTotal().toString());
                detalleTransaccionTicketDTO.setMetodoPago(transaccionTicket.getMetodoPago());
                detalleTransaccionTicketDTO.setNumeroTransaccion(transaccionTicket.getNumeroTransaccion());
                detalleTransaccionTicketDTO.setEstado(transaccionTicket.getEstado().toString());
                detalleTransaccionTicketDTO.setFechaTransaccion(transaccionTicket.getFechaTransaccion().toString());
                detallesFacturacion.add(detalleTransaccionTicketDTO);
            }
            reporte.setDetallesFacturacion(detallesFacturacion);

            reportes.add(reporte);
        }

        return reportes;
    }

    /*
    Reporte de suscripciones activas e inactivas: incluye consumo de horas por cliente, saldo disponible
y excedentes cobrados.
     */
    public List<ReporteSuscripcionesDTO> generarReporteSuscripciones(Long idUsuarioEmpresa) {
        // Obtener la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();

        // Obtener todos los planes de la empresa
        List<TipoPlan> planes = tipoPlanRepository.findByEmpresa_IdEmpresa(empresa.getIdEmpresa());

        List<ReporteSuscripcionesDTO> reportes = new ArrayList<>();
        for (TipoPlan plan : planes) {
            ReporteSuscripcionesDTO reporte = new ReporteSuscripcionesDTO();
            reporte.setIdTipoPlan(plan.getId());
            reporte.setNombrePlan(String.valueOf(plan.getNombrePlan()));
            reporte.setCodigoPlan(plan.getCodigoPlan());
            reporte.setDescripcion(plan.getDescripcion());
            reporte.setPrecioPlan(plan.getPrecioPlan().doubleValue());
            reporte.setHorasDia(plan.getHorasDia());
            reporte.setHorasMensuales(plan.getHorasMensuales());
            reporte.setDiasAplicables(plan.getDiasAplicables());
            reporte.setCoberturaHoraria(plan.getCoberturaHoraria());
            reporte.setOrdenBeneficio(plan.getOrdenBeneficio());
            reporte.setEstadoPlan(plan.getActivo().toString());
            reporte.setFechaCreacion(plan.getFechaCreacion().toString());

            // Configuración de descuento
            ConfiguracionDescuentoPlan configDescuento = configuracionDescuentoPlanRepository.findByTipoPlan_Id(plan.getId());
            if (configDescuento != null) {
                DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configDTO = new DetalleTipoPlanDTO.ConfiguracionDescuentoDTO();
                configDTO.setIdConfiguracionDescuento(configDescuento.getId());
                configDTO.setDescuentoMensual(configDescuento.getDescuentoMensual().doubleValue());
                configDTO.setDescuentoAnualAdicional(configDescuento.getDescuentoAnualAdicional().doubleValue());
                configDTO.setFechaCreacionDescuento(configDescuento.getFechaCreacion().toString());
                configDTO.setFechaVigenciaInicio(configDescuento.getFechaVigenciaInicio().toString());
                configDTO.setFechaVigenciaFin(configDescuento.getFechaVigenciaFin().toString());
                configDTO.setEstadoConfiguracion(configDescuento.getEstado().toString());

                reporte.setConfiguracionDescuento(configDTO);
            }

            // Detalles de suscriptores
            List<ReporteSuscripcionesDTO.DetalleSuscriptoresDTO> detallesSuscriptores = new ArrayList<>();
            List<Suscripcion> suscripciones = suscripcionRepository.findByTipoPlan_Id(plan.getId());
            for (Suscripcion suscripcion : suscripciones) {
                ReporteSuscripcionesDTO.DetalleSuscriptoresDTO detalle = new ReporteSuscripcionesDTO.DetalleSuscriptoresDTO();
                detalle.setIdSuscripcion(suscripcion.getId());
                detalle.setNombreSuscriptor(suscripcion.getUsuario().getPersona().getNombre());
                detalle.setPlacaVehiculo(suscripcion.getVehiculo().getPlaca());
                detalle.setMontoTarifaReferenciada(suscripcion.getTarifaBaseReferencia().getPrecioPorHora().doubleValue());
                detalle.setHorasMensualesIncluidas(suscripcion.getHorasMensualesIncluidas());
                detalle.setHorasUtilizadasMes(suscripcion.getHorasConsumidas().intValue());
                detalle.setFechaInicioSuscripcion(suscripcion.getFechaInicio().toString());
                detalle.setFechaFinSuscripcion(suscripcion.getFechaFin().toString());
                //Cálculo de totales
                detalle.setTotalHorasUtilizadas(calcularHorasUtilizadasTotales(suscripcion.getId()));
                detalle.setTotalExcedentePagado(calcularExcedentes(suscripcion.getId()));
                detalle.setEstadoSuscripcion(suscripcion.getEstado().toString());
                detallesSuscriptores.add(detalle);
            }
            reporte.setDetallesSuscriptores(detallesSuscriptores);
            reportes.add(reporte);
        }
        return reportes;


    }


    /*Reporte de beneficios otorgados por comercios afiliados: horas gratis aplicadas, sucursal
correspondiente, clientes beneficiados y total a liquidar al comercio en el corte de caja. */

    public List<ReporteComercioAfiliadoDTO> generarReporteComercioAfiliado(Long idUsuarioEmpresa) {
        //Obtenemos la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();
        //Obtenemos los comercios afiliados a la empresa(que viene de la sucursal)
        List<ConvenioComercioSucursal> comerciosAfiliados = convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresa(empresa.getIdEmpresa());
        //Filtramos los comercios de los convenios sin repetirse
        List<ComercioAfiliado> comerciosUnicos = new ArrayList<>();
        for (ConvenioComercioSucursal convenio : comerciosAfiliados) {
            ComercioAfiliado comercio = convenio.getComercioAfiliado();
            if (!comerciosUnicos.contains(comercio)) {
                comerciosUnicos.add(comercio);
            }
        }

        List<ReporteComercioAfiliadoDTO> reportes = new ArrayList<>();
        for (ComercioAfiliado comercio : comerciosUnicos) {
            ReporteComercioAfiliadoDTO reporte = new ReporteComercioAfiliadoDTO();
            reporte.setIdComercioAfiliado(comercio.getId());
            reporte.setNombreComercial(comercio.getNombreComercial());
            reporte.setRazonSocial(comercio.getRazonSocial());
            reporte.setNit(comercio.getNit());
            reporte.setTipoComercio(comercio.getTipoComercio().toString());
            reporte.setCorreoContacto(comercio.getCorreoContacto());
            reporte.setEstado(comercio.getEstado().toString());

            //Detalles del convenio
            List<ReporteComercioAfiliadoDTO.DetalleConvenioComercioDTO> detallesConvenio = new ArrayList<>();
            List<ConvenioComercioSucursal> convenios = convenioComercioSucursalRepository.findByComercioAfiliado_IdAndEstado(comercio.getId(), ConvenioComercioSucursal.Estado.ACTIVO);
            for (ConvenioComercioSucursal convenio : convenios) {
                ReporteComercioAfiliadoDTO.DetalleConvenioComercioDTO detalle = new ReporteComercioAfiliadoDTO.DetalleConvenioComercioDTO();
                detalle.setIdConvenioComercio(convenio.getId());
                detalle.setNombreSucursal(convenio.getSucursal().getNombre());
                detalle.setHorasGratisMaximo(convenio.getHorasGratisMaximo().toString());
                detalle.setPeriodoCorte(convenio.getPeriodoCorte().toString());
                detalle.setTarifaPorHora(convenio.getTarifaPorHora().toString());
                detalle.setFechaInicioConvenio(convenio.getFechaInicioConvenio().toString());
                detalle.setFechaFinConvenio(convenio.getFechaFinConvenio().toString());
                detalle.setEstado(convenio.getEstado().toString());
                detalle.setFechaCreacion(convenio.getFechaCreacion().toString());
                detallesConvenio.add(detalle);
            }
            reporte.setDetallesConvenioComercio(detallesConvenio);

            //Detalles de cortes de caja y liquidaciones
            List<DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO> detallesCorteCaja = new ArrayList<>();
            List<LiquidacionComercio> liquidaciones = liquidacionComercioRepository.findByComercio_Id(comercio.getId());
            for (LiquidacionComercio liquidacion : liquidaciones) {
                DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalleLiquidacion = new DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO();
                detalleLiquidacion.setIdLiquidacion(liquidacion.getIdLiquidacion());
                detalleLiquidacion.setComercioNombre(liquidacion.getComercio().getNombreComercial());
                detalleLiquidacion.setTotalHorasOtorgadas(liquidacion.getTotalHorasOtorgadas().toString());
                detalleLiquidacion.setTarifaPorHora(liquidacion.getTarifaPorHora().toString());
                detalleLiquidacion.setMontoTotal(liquidacion.getMontoTotal().toString());
                detalleLiquidacion.setEstado(liquidacion.getEstado().toString());
                detalleLiquidacion.setFechaFacturacion(liquidacion.getFechaFacturacion() != null ? liquidacion.getFechaFacturacion().toString() : null);
                detalleLiquidacion.setFechaPago(liquidacion.getFechaPago() != null ? liquidacion.getFechaPago().toString() : null);
                detalleLiquidacion.setObservaciones(liquidacion.getObservaciones());
                detallesCorteCaja.add(detalleLiquidacion);
            }
            reporte.setDetallesCorteCaja(detallesCorteCaja);

            //Clientes beneficiados
            List<ReporteComercioAfiliadoDTO.ClientesBeneficiadosDTO> clientesBeneficiados = new ArrayList<>();
            List<AcreditacionHorasComercio> acreditaciones = acreditacionHorasComercioRepository.findByConvenio_ComercioAfiliado_Id(comercio.getId());
            for (AcreditacionHorasComercio acreditacion : acreditaciones) {
                ReporteComercioAfiliadoDTO.ClientesBeneficiadosDTO clienteBeneficiado = new ReporteComercioAfiliadoDTO.ClientesBeneficiadosDTO();
                clienteBeneficiado.setIdCliente(acreditacion.getTicket().getSuscripcion().getUsuario().getIdUsuario());
                clienteBeneficiado.setNombreCliente(acreditacion.getTicket().getSuscripcion().getUsuario().getPersona().getNombre());
                clienteBeneficiado.setHorasGratisOtorgadas(acreditacion.getHorasOtorgadas().toString());
                clienteBeneficiado.setSucursalNombre(acreditacion.getConvenio().getSucursal().getNombre());
                clienteBeneficiado.setFechaBeneficio(acreditacion.getFechaAcreditacion().toString());
                clientesBeneficiados.add(clienteBeneficiado);
            }
            reporte.setClientesBeneficiados(clientesBeneficiados);
            reportes.add(reporte);
        }
        return reportes;

    }

    /*Reporte de cortes de caja (diario, semanal, mensual o anual): ingresos totales por sucursal,
desglose por tarifas, suscripciones y cargos a comercios.
     */
    public List<ReporteCorteDeCajaDTO> generarReporteCortesDeCaja(Long idUsuarioEmpresa) {

        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());
        List<ReporteCorteDeCajaDTO> reportes = new ArrayList<>();

        for (Sucursal sucursal : sucursales) {
            ReporteCorteDeCajaDTO reporte = new ReporteCorteDeCajaDTO();
            reporte.setIdSucursal(sucursal.getIdSucursal());
            reporte.setNombreSucursal(sucursal.getNombre());
            reporte.setDireccionCompleta(sucursal.getDireccionCompleta());
            reporte.setCiudad(sucursal.getCiudad());
            reporte.setDepartamento(sucursal.getDepartamento());
            reporte.setHoraApertura(sucursal.getHoraApertura().toString());
            reporte.setHoraCierre(sucursal.getHoraCierre().toString());
            reporte.setCapacidad2Ruedas(sucursal.getCapacidad2Ruedas());
            reporte.setCapacidad4Ruedas(sucursal.getCapacidad4Ruedas());

            // Obtener el detalle de cortes de caja para la sucursal
            List<DetallesLiquidacionesDTO.CortesDeCajaDTO> cortesDeCajas = new ArrayList<>();
            List<CorteCaja> cortes = corteCajaRepository.findBySucursal_IdSucursal(sucursal.getIdSucursal());
            for (CorteCaja corte : cortes) {
                DetallesLiquidacionesDTO.CortesDeCajaDTO corteDTO = new DetallesLiquidacionesDTO.CortesDeCajaDTO();
                corteDTO.setIdCorteCaja(corte.getIdCorteCaja());
                corteDTO.setSucursalNombre(corte.getSucursal().getNombre());
                corteDTO.setPeriodo(corte.getPeriodo().toString());
                corteDTO.setFechaInicio(corte.getFechaInicio().toString());
                corteDTO.setFechaFin(corte.getFechaFin().toString());
                //corteDTO.setTotalIngresosTarifas(corte.getTotalIngresosTarifas().toString());
                //corteDTO.setTotalIngresosExcedentes(corte.getTotalIngresosExcedentes().toString());
                corteDTO.setTotalHorasComercio(corte.getTotalHorasComercio().toString());
                corteDTO.setTotalLiquidacionComercios(corte.getTotalLiquidacionComercios().toString());
                corteDTO.setTotalNeto(corte.getTotalNeto().toString());
                corteDTO.setGeneradoPorNombreUsuario(corte.getGeneradoPor().getPersona().getNombre());
                corteDTO.setFechaGeneracion(corte.getFechaGeneracion().toString());
                corteDTO.setEstado(corte.getEstado().toString());
                //Detalles de liquidaciones por comercio
                List<DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO> detallesComercios = new ArrayList<>();
                List<LiquidacionComercio> liquidaciones = liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(corte.getIdCorteCaja());
                for (LiquidacionComercio liquidacion : liquidaciones) {
                    DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalleLiquidacion = new DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO();
                    detalleLiquidacion.setIdLiquidacion(liquidacion.getIdLiquidacion());
                    detalleLiquidacion.setComercioNombre(liquidacion.getComercio().getNombreComercial());
                    detalleLiquidacion.setTotalHorasOtorgadas(liquidacion.getTotalHorasOtorgadas().toString());
                    detalleLiquidacion.setTarifaPorHora(liquidacion.getTarifaPorHora().toString());
                    detalleLiquidacion.setMontoTotal(liquidacion.getMontoTotal().toString());
                    detalleLiquidacion.setEstado(liquidacion.getEstado().toString());
                    detalleLiquidacion.setFechaFacturacion(liquidacion.getFechaFacturacion() != null ? liquidacion.getFechaFacturacion().toString() : null);
                    detalleLiquidacion.setFechaPago(liquidacion.getFechaPago() != null ? liquidacion.getFechaPago().toString() : null);
                    detalleLiquidacion.setObservaciones(liquidacion.getObservaciones());
                    detallesComercios.add(detalleLiquidacion);
                }
                corteDTO.setDetallesComercios(detallesComercios);
                cortesDeCajas.add(corteDTO);
            }
            reporte.setCortesDeCajas(cortesDeCajas);
            //Detalles de ingresos por tarifas y excedentes
            List<DetalleTransaccionTicketDTO> detallesDeIngresosPorTarifasYExcedentes = new ArrayList<>();
            List<TransaccionTicket> transacciones = transaccionTicketRepository.findByTicket_Sucursal_IdSucursal(sucursal.getIdSucursal());
            for (TransaccionTicket transaccionTicket : transacciones) {
                DetalleTransaccionTicketDTO detalleTransaccionTicketDTO = new DetalleTransaccionTicketDTO();
                detalleTransaccionTicketDTO.setIdTransaccion(transaccionTicket.getIdTransaccion());
                detalleTransaccionTicketDTO.setIdTicket(transaccionTicket.getTicket().getId());
                if (transaccionTicket.getTicket().getSuscripcion() != null){
                    detalleTransaccionTicketDTO.setNombreCliente(transaccionTicket.getTicket().getSuscripcion().getUsuario().getPersona().getNombre());
                }else{
                    detalleTransaccionTicketDTO.setNombreCliente("Cliente ocasional");
                }
                detalleTransaccionTicketDTO.setTipoCobro(transaccionTicket.getTipoCobro().toString());
                detalleTransaccionTicketDTO.setHorasCobradas(transaccionTicket.getHorasCobradas().toString());
                detalleTransaccionTicketDTO.setHorasGratisComercio(transaccionTicket.getHorasGratisComercio().toString());
                detalleTransaccionTicketDTO.setTarifaAplicada(transaccionTicket.getTarifaAplicada().toString());
                detalleTransaccionTicketDTO.setSubtotal(transaccionTicket.getSubtotal().toString());
                detalleTransaccionTicketDTO.setDescuento(transaccionTicket.getDescuento().toString());
                detalleTransaccionTicketDTO.setTotal(transaccionTicket.getTotal().toString());
                detalleTransaccionTicketDTO.setMetodoPago(transaccionTicket.getMetodoPago());
                detalleTransaccionTicketDTO.setNumeroTransaccion(transaccionTicket.getNumeroTransaccion());
                detalleTransaccionTicketDTO.setEstado(transaccionTicket.getEstado().toString());
                detalleTransaccionTicketDTO.setFechaTransaccion(transaccionTicket.getFechaTransaccion().toString());
                detallesDeIngresosPorTarifasYExcedentes.add(detalleTransaccionTicketDTO);
            }
            reporte.setDetallesDeIngresosPorTarifasYExcedentes(detallesDeIngresosPorTarifasYExcedentes);
            reportes.add(reporte);
        }
        return reportes;
    }

    /*
    Reporte de incidencias: casos de comprobante extraviado, acreditación de propiedad, evidencias
registradas y resolución del incidente.
     */

    public ReportesDeIncidenciasDTO generarReporteIncidencias(Long idUsuarioEmpresa) throws Exception {

        List<DetalleSucursalesIncidenciasDTO> detalleSucursalesIncidenciasDTOList = resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(idUsuarioEmpresa);
        ReportesDeIncidenciasDTO reporte = new ReportesDeIncidenciasDTO();
        reporte.setDetalleSucursalesIncidenciasDTO(detalleSucursalesIncidenciasDTOList);
        return reporte;

    }

    /*
    Reporte de flotas empresariales (nuevo módulo): consumo diferenciado por empresa, número de
placas registradas y descuentos aplicados bajo los planes corporativos.
     */
    public DetalleEmpresaFlotillaDTO generarReporteFlotasEmpresariales(Long idUsuarioEmpresa) throws Exception {
        return gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(idUsuarioEmpresa);
    }




    //Cálculo de horas utilizadas totales por suscripción
    private Integer calcularHorasUtilizadasTotales(Long idSuscripcion) {
        List<Ticket> tickets = ticketRepository.findBySuscripcion_Id(idSuscripcion);
        Integer totalHoras = 0;
        for (Ticket ticket : tickets) {
            TransaccionTicket transaccion = transaccionTicketRepository.findByTicket_Id(ticket.getId());
            if (transaccion != null) {
                totalHoras += transaccion.getHorasCobradas().intValue();
            }

        }
        return totalHoras;
    }

    //Cálculo de excedentes pagados por suscripción
    private Double calcularExcedentes (Long idSuscripcion){
        List<Ticket> tickets = ticketRepository.findBySuscripcion_Id(idSuscripcion);
        Double totalExcedentes = 0.0;
        for (Ticket ticket : tickets) {
            TransaccionTicket transaccion = transaccionTicketRepository.findByTicket_Id(ticket.getId());
            if (transaccion != null && transaccion.getTipoCobro() == TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION) {
                totalExcedentes += transaccion.getTotal().doubleValue();
            }
        }
        return totalExcedentes;

    }


}
