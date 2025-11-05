package org.parkcontrol.apiparkcontrol.services.liquidaciones;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import  org.parkcontrol.apiparkcontrol.dto.liquidaciones.*;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.UsuarioSucursalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.*;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GestionLiquidacionService {
    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;

    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;

    @Autowired
    SucursalRepository sucursalRepository;

    @Autowired
    TarifaSucursalRepository tarifaSucursalRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    private CorteCajaRepository corteCajaRepository;
    @Autowired
    private LiquidacionComercioRepository liquidacionComercioRepository;
    @Autowired
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private TransaccionTicketRepository transaccionTicketRepository;

    //Obtener detalles de liquidaciones de las sucursales de una empresa
    public DetallesLiquidacionesDTO obtenerDetallesLiquidacionesPorEmpresa(Long idUsuarioEmpresa) {
        //Verificar que el usuario pertenece a una empresa
        Usuario usuarioEmpresa = usuarioRepository.findById(idUsuarioEmpresa).orElseThrow(() ->
                new RuntimeException("Usuario no encontrado"));

        //Obtenemos la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();

        //verificamos que la empresa exista
        if (empresa == null) {
            throw new RuntimeException("La empresa no existe");
        }

        //Obtenemos las sucursales de la empresa
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());

        DetallesLiquidacionesDTO detallesLiquidacionesDTO = new DetallesLiquidacionesDTO();
        List<DetallesLiquidacionesDTO.CortesDeCajaDTO> cortesDeCajaDTOList = new ArrayList<>();
        for (Sucursal sucursal : sucursales) {
            //Obtenemos los cortes de caja de la sucursal
            List<CorteCaja> cortesDeCaja = corteCajaRepository.findBySucursal_IdSucursal(sucursal.getIdSucursal());
            for (CorteCaja corteCaja : cortesDeCaja) {
                DetallesLiquidacionesDTO.CortesDeCajaDTO cortesDeCajaDTO = new DetallesLiquidacionesDTO.CortesDeCajaDTO();
                cortesDeCajaDTO.setIdCorteCaja(corteCaja.getIdCorteCaja());
                cortesDeCajaDTO.setSucursalNombre(sucursal.getNombre());
                cortesDeCajaDTO.setPeriodo(corteCaja.getPeriodo().toString());
                cortesDeCajaDTO.setFechaInicio(corteCaja.getFechaInicio().toString());
                cortesDeCajaDTO.setFechaFin(corteCaja.getFechaFin().toString());
                //cortesDeCajaDTO.setTotalIngresosTarifas(corteCaja.getTotalIngresosTarifas().toString());
                //cortesDeCajaDTO.setTotalIngresosExcedentes(corteCaja.getTotalIngresosExcedentes().toString());
                cortesDeCajaDTO.setTotalHorasComercio(corteCaja.getTotalHorasComercio().toString());
                cortesDeCajaDTO.setTotalLiquidacionComercios(corteCaja.getTotalLiquidacionComercios().toString());
                cortesDeCajaDTO.setTotalNeto(corteCaja.getTotalNeto().toString());
                cortesDeCajaDTO.setGeneradoPorNombreUsuario(corteCaja.getGeneradoPor().getNombreUsuario());
                cortesDeCajaDTO.setFechaGeneracion(corteCaja.getFechaGeneracion().toString());
                cortesDeCajaDTO.setEstado(corteCaja.getEstado().toString());

                //Obtenemos las liquidaciones de comercio asociadas al corte de caja
                List<LiquidacionComercio> liquidacionesComercio = liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(corteCaja.getIdCorteCaja());
                List<DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO> detalleComercioLiquidacionDTOList = new ArrayList<>();
                for (LiquidacionComercio liquidacionComercio : liquidacionesComercio) {
                    DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalleComercioLiquidacionDTO = new DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO();
                    detalleComercioLiquidacionDTO.setIdLiquidacion(liquidacionComercio.getIdLiquidacion());
                    detalleComercioLiquidacionDTO.setComercioNombre(liquidacionComercio.getComercio().getNombreComercial());
                    detalleComercioLiquidacionDTO.setTotalHorasOtorgadas(liquidacionComercio.getTotalHorasOtorgadas().toString());
                    detalleComercioLiquidacionDTO.setTarifaPorHora(liquidacionComercio.getTarifaPorHora().toString());
                    detalleComercioLiquidacionDTO.setMontoTotal(liquidacionComercio.getMontoTotal().toString());
                    detalleComercioLiquidacionDTO.setEstado(liquidacionComercio.getEstado().toString());
                    detalleComercioLiquidacionDTO.setFechaFacturacion(liquidacionComercio.getFechaFacturacion() != null ? liquidacionComercio.getFechaFacturacion().toString() : null);
                    detalleComercioLiquidacionDTO.setFechaPago(liquidacionComercio.getFechaPago() != null ? liquidacionComercio.getFechaPago().toString() : null);
                    detalleComercioLiquidacionDTO.setObservaciones(liquidacionComercio.getObservaciones());
                    detalleComercioLiquidacionDTOList.add(detalleComercioLiquidacionDTO);
                }
                cortesDeCajaDTO.setDetallesComercios(detalleComercioLiquidacionDTOList);
                cortesDeCajaDTOList.add(cortesDeCajaDTO);
            }
        }
        detallesLiquidacionesDTO.setCortesDeCaja(cortesDeCajaDTOList);
        return detallesLiquidacionesDTO;
    }

    //Obtener detalles de las suscripciones
    public List<DetallePagosSuscripcionDTO> obtenerDetallesPagosSuscripcion(Long idUsuarioEmpresa) {
        //Verificar que el usuario pertenece a una empresa
        Usuario usuarioEmpresa = usuarioRepository.findById(idUsuarioEmpresa).orElseThrow(() ->
                new RuntimeException("Usuario no encontrado"));

        //Obtenemos la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();


        //verificamos que la empresa exista
        if (empresa == null) {
            throw new RuntimeException("La empresa no existe");
        }
        //Verificamos y actualizamos el estado de las suscripciones vencidas
        verificarVencimientoSuscripcion(empresa);

        //Obtenemos los historiales de pago de suscripcion de la empresa
        List<HistorialPagoSuscripcion> historialPagoSuscripcions = historialPagoSuscripcionRepository.findBySuscripcion_Empresa_IdEmpresa(empresa.getIdEmpresa());

        List<DetallePagosSuscripcionDTO> detallePagosSuscripcionDTOList = new ArrayList<>();
        for (HistorialPagoSuscripcion historialPagoSuscripcion : historialPagoSuscripcions) {
            DetallePagosSuscripcionDTO detallePagosSuscripcionDTO = new DetallePagosSuscripcionDTO();
            detallePagosSuscripcionDTO.setIdHistorialPago(historialPagoSuscripcion.getIdHistorialPagoSuscripcion());
            detallePagosSuscripcionDTO.setIdSuscripcion(historialPagoSuscripcion.getSuscripcion().getId());
            detallePagosSuscripcionDTO.setNombreCliente(historialPagoSuscripcion.getSuscripcion().getUsuario().getPersona().getNombre());
            detallePagosSuscripcionDTO.setFechaPago(historialPagoSuscripcion.getFechaPago().toString());
            detallePagosSuscripcionDTO.setMontoPagado(historialPagoSuscripcion.getMontoPagado().toString());
            detallePagosSuscripcionDTO.setMetodoPago(historialPagoSuscripcion.getMetodoPago().toString());
            detallePagosSuscripcionDTO.setNumeroTransaccion(historialPagoSuscripcion.getNumeroTransaccion());
            detallePagosSuscripcionDTO.setEstadoPago(historialPagoSuscripcion.getEstadoPago().toString());
            detallePagosSuscripcionDTO.setMotivoPago(historialPagoSuscripcion.getMotivoPago().toString());
            detallePagosSuscripcionDTOList.add(detallePagosSuscripcionDTO);
        }
        return detallePagosSuscripcionDTOList;
    }

    private void verificarVencimientoSuscripcion(Empresa empresa) {
        //Obtener todas las suscripciones de la empresa activas
        List<Suscripcion> suscripciones = suscripcionRepository.getSuscripcionByEmpresa_IdEmpresaAndEstado(empresa.getIdEmpresa(), Suscripcion.EstadoSuscripcion.ACTIVA);
        for (Suscripcion suscripcion : suscripciones) {
            //Verificar si la suscripcion ha vencido
            if (suscripcion.getFechaFin().isBefore(LocalDate.now().atStartOfDay())) {
                //Cambiar el estado de la suscripcion a vencida
                suscripcion.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
                suscripcionRepository.save(suscripcion);
            }
        }

    }

    //Obtener el detalle de las transacciones de tickets
    public List<DetalleTransaccionTicketDTO> obtenerDetalleTransaccionesTicket(Long idUsuarioEmpresa) {
        //Verificar que el usuario pertenece a una empresa
        Usuario usuarioEmpresa = usuarioRepository.findById(idUsuarioEmpresa).orElseThrow(() ->
                new RuntimeException("Usuario no encontrado"));

        //Obtenemos la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();

        //verificamos que la empresa exista
        if (empresa == null) {
            throw new RuntimeException("La empresa no existe");
        }
        //Obtenemos las transacciones de tickets asociadas a la empresa
        List<TransaccionTicket> transaccionTickets = transaccionTicketRepository.findByTicket_Sucursal_Empresa_IdEmpresa(idUsuarioEmpresa);
        List<DetalleTransaccionTicketDTO> detalleTransaccionTicketDTOList = new ArrayList<>();
        for (TransaccionTicket transaccionTicket : transaccionTickets) {
            DetalleTransaccionTicketDTO detalleTransaccionTicketDTO = new DetalleTransaccionTicketDTO();
            detalleTransaccionTicketDTO.setIdTransaccion(transaccionTicket.getIdTransaccion());
            detalleTransaccionTicketDTO.setIdTicket(transaccionTicket.getTicket().getId());
            detalleTransaccionTicketDTO.setNombreCliente(transaccionTicket.getTicket().getSuscripcion().getUsuario().getPersona().getNombre());
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
            detalleTransaccionTicketDTOList.add(detalleTransaccionTicketDTO);
        }
        return detalleTransaccionTicketDTOList;

    }

    //Actualizar cortes de caja
    @Transactional
    public String actualizarPeriodosCortesDeCaja(Long idUsuarioEmpresa) {
        //Verificar que el usuario pertenece a una empresa
        Usuario usuarioEmpresa = usuarioRepository.findById(idUsuarioEmpresa).orElseThrow(() ->
                new RuntimeException("Usuario no encontrado"));

        //Obtenemos la empresa del usuario
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioEmpresa).getFirst();

        //verificamos que la empresa exista
        if (empresa == null) {
            throw new RuntimeException("La empresa no existe");
        }

        //Obtenemos los convenios de comercio sucursal asociados a la empresa
        List<ConvenioComercioSucursal> convenios = convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(empresa.getIdEmpresa(), ConvenioComercioSucursal.Estado.ACTIVO);
        if (convenios.isEmpty()) {
            return "No hay convenios activos para actualizar.";
        }

        // Agrupar convenios por sucursal y periodo (clave compuesta)
        Map<String, List<ConvenioComercioSucursal>> conveniosAgrupados = convenios.stream()
                .collect(Collectors.groupingBy(c ->
                        c.getSucursal().getIdSucursal() + "_" + c.getPeriodoCorte().name()
                ));

        // Iterar sobre los grupos (cada sucursal-periodo = un posible corte)
        for (Map.Entry<String, List<ConvenioComercioSucursal>> entry : conveniosAgrupados.entrySet()) {
            List<ConvenioComercioSucursal> grupo = entry.getValue();

            // Obtener datos comunes del grupo
            Sucursal sucursal = grupo.get(0).getSucursal();
            ConvenioComercioSucursal.PeriodoCorte periodoCorte = grupo.get(0).getPeriodoCorte();

            // Obtener último corte de esa sucursal y periodo
            CorteCaja ultimoCorte = corteCajaRepository
                    .findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(
                            sucursal.getIdSucursal(),
                            CorteCaja.Periodo.valueOf(periodoCorte.name())
                    );

            // Validar si ya toca generar el nuevo corte
            if (!esMomentoCorte(grupo.get(0), ultimoCorte)) {
                continue;
            }

            // Generar nuevo corte
            CorteCaja nuevoCorte = generarNuevoCorteCaja(grupo.get(0), ultimoCorte, usuarioEmpresa);
            corteCajaRepository.save(nuevoCorte);

            // Generar liquidaciones para todos los convenios del grupo
            for (ConvenioComercioSucursal convenio : grupo) {
                LiquidacionComercio liquidacion = new LiquidacionComercio();
                liquidacion.setCorteCaja(nuevoCorte);
                liquidacion.setConvenio(convenio);
                liquidacion.setComercio(convenio.getComercioAfiliado());
                liquidacion.setTotalHorasOtorgadas(convenio.getHorasGratisMaximo());
                liquidacion.setTarifaPorHora(convenio.getTarifaPorHora());

                //Hay que calcular el monto total a partir de las horas gratis maximo y la tarifa por hora ademas del periodo de corte, si es diario, semanal, mensual o anual hay que dividir o multiplicar segun corresponda
                BigDecimal monto = convenio.getHorasGratisMaximo()
                        .multiply(convenio.getTarifaPorHora())
                        .multiply(calcularMontoPorPeriodo(convenio.getPeriodoCorte()));

                liquidacion.setMontoTotal(monto);
                liquidacion.setEstado(LiquidacionComercio.EstadoLiquidacion.PENDIENTE);
                liquidacion.setObservaciones("Liquidación generada automáticamente.");
                liquidacionComercioRepository.save(liquidacion);
            }
        }

        return "Periodos de cortes de caja actualizados correctamente.";

    }

    private boolean esMomentoCorte(ConvenioComercioSucursal convenio, CorteCaja ultimoCorteCaja) {
        LocalDateTime fechaInicio;
        if (ultimoCorteCaja == null) {
            fechaInicio = convenio.getFechaInicioConvenio();
        } else {
            fechaInicio = ultimoCorteCaja.getFechaFin();
        }
        LocalDateTime fechaActual = LocalDateTime.now();

        switch (convenio.getPeriodoCorte()) {
            case DIARIO:
                return fechaActual.isAfter(fechaInicio.plusDays(1));
            case SEMANAL:
                return fechaActual.isAfter(fechaInicio.plusWeeks(1));
            case MENSUAL:
                return fechaActual.isAfter(fechaInicio.plusMonths(1));
            case ANUAL:
                return fechaActual.isAfter(fechaInicio.plusYears(1));
            default:
                return false;
        }
    }

    private CorteCaja generarNuevoCorteCaja(ConvenioComercioSucursal convenio, CorteCaja ultimoCorteCaja, Usuario usuarioEmpresa) {
        LocalDateTime fechaInicio;
        if (ultimoCorteCaja == null) {
            fechaInicio = convenio.getFechaInicioConvenio();
        } else {
            fechaInicio = ultimoCorteCaja.getFechaFin();
        }
        LocalDateTime fechaFin = calcularFechaFin(fechaInicio, convenio.getPeriodoCorte());

        //Calcular totales para el corte de caja
        CorteCaja nuevoCorteCaja = new CorteCaja();
        nuevoCorteCaja.setSucursal(convenio.getSucursal());
        nuevoCorteCaja.setPeriodo(CorteCaja.Periodo.valueOf(convenio.getPeriodoCorte().name()));
        nuevoCorteCaja.setFechaInicio(fechaInicio);
        nuevoCorteCaja.setFechaFin(fechaFin);
        nuevoCorteCaja.setTotalIngresosTarifas(BigDecimal.ZERO); // Lógica para calcular este valor
        nuevoCorteCaja.setTotalIngresosExcedentes(BigDecimal.ZERO);
        nuevoCorteCaja.setTotalHorasComercio(convenio.getHorasGratisMaximo()); // Lógica para calcular este valor
        //Calculo de la liquidacion a comercios
        double calculoLiquidacion = convenio.getHorasGratisMaximo().doubleValue() * convenio.getTarifaPorHora().doubleValue();
        nuevoCorteCaja.setTotalLiquidacionComercios(BigDecimal.valueOf(calculoLiquidacion));
        nuevoCorteCaja.setTotalNeto(nuevoCorteCaja.getTotalIngresosTarifas()
                .add(nuevoCorteCaja.getTotalIngresosExcedentes())
                .add(nuevoCorteCaja.getTotalLiquidacionComercios()));
        nuevoCorteCaja.setGeneradoPor(usuarioEmpresa);
        nuevoCorteCaja.setFechaGeneracion(LocalDateTime.now());
        nuevoCorteCaja.setEstado(CorteCaja.Estado.PRELIMINAR);
        return nuevoCorteCaja;


    }

    private LocalDateTime calcularFechaFin(LocalDateTime fechaInicio, ConvenioComercioSucursal.PeriodoCorte periodoCorte) {
        switch (periodoCorte) {
            case DIARIO:
                return fechaInicio.plusDays(1);
            case SEMANAL:
                return fechaInicio.plusWeeks(1);
            case MENSUAL:
                return fechaInicio.plusMonths(1);
            case ANUAL:
                return fechaInicio.plusYears(1);
            default:
                throw new IllegalArgumentException("Periodo de corte no válido");
        }
    }

    private BigDecimal calcularMontoPorPeriodo(ConvenioComercioSucursal.PeriodoCorte periodoCorte) {
        //El monto esta basado en un mes. si es diario se divide entre 30, si es semanal entre 4, si es mensual no se modifica, si es anual se multiplica por 12
        switch (periodoCorte) {
            case DIARIO:
                return BigDecimal.valueOf(1.0 / 30.0);
            case SEMANAL:
                return BigDecimal.valueOf(1.0 / 4.0);
            case MENSUAL:
                return BigDecimal.ONE;
            case ANUAL:
                return BigDecimal.valueOf(12);
            default:
                throw new IllegalArgumentException("Periodo de corte no válido");
        }
    }
}