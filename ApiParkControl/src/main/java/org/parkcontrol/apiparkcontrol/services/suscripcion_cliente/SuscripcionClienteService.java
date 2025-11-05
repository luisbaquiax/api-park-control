package org.parkcontrol.apiparkcontrol.services.suscripcion_cliente;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
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
public class SuscripcionClienteService {
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



    //Planes de suscripcion para clientes
    public PlanesSuscripcionDTO obtenerPlanesSuscripcion() {

        //Obtenemos todas las empresas
        List<Empresa> empresas = empresaRepository.findAll();
        List<PlanesSuscripcionDTO.EmpresaSuscripcionesDTO> empresasSuscripcionesDTOS = new ArrayList<>();

        //Iteramos sobre las empresas para construir el DTO
        for (Empresa empresa : empresas) {
            empresasSuscripcionesDTOS.add(obtenerEmpresaSuscripciones(empresa));
        }



        return new PlanesSuscripcionDTO(empresasSuscripcionesDTOS);
    }

    public PlanesSuscripcionDTO.EmpresaSuscripcionesDTO obtenerEmpresaSuscripciones(Empresa empresa) {
        //Obtenemos las sucursales de la empresa
        PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaSuscripcionesDTO;
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());
        List<ObtenerSucursalesEmpresaDTO.SucursalDTO> sucursalDTOS = new ArrayList<>();
        for (Sucursal sucursal : sucursales) {
            ObtenerSucursalesEmpresaDTO.SucursalDTO sucursalDTO = new ObtenerSucursalesEmpresaDTO.SucursalDTO(
                    sucursal.getIdSucursal(),
                    sucursal.getNombre(),
                    sucursal.getDireccionCompleta(),
                    sucursal.getCiudad(),
                    sucursal.getDepartamento(),
                    sucursal.getHoraApertura().toString(),
                    sucursal.getHoraCierre().toString(),
                    sucursal.getCapacidad2Ruedas(),
                    sucursal.getCapacidad4Ruedas(),
                    sucursal.getEstado().toString(),
                    null // UsuarioSucursalDTO no se llena en este contexto
            );
            sucursalDTOS.add(sucursalDTO);
        }
        //Obtenemos los tipos de plan de la empresa
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
                    tipoPlan.getPrecioPlan(),
                    tipoPlan.getHorasDia(),
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
        empresaSuscripcionesDTO = new PlanesSuscripcionDTO.EmpresaSuscripcionesDTO(
                empresa.getIdEmpresa(),
                empresa.getNombreComercial(),
                empresa.getNit(),
                empresa.getRazonSocial(),
                empresa.getTelefonoPrincipal(),
                empresa.getDireccionFiscal(),
                sucursalDTOS,
                detalleTipoPlanDTOS
        );
        return empresaSuscripcionesDTO;
    }

    //Planes de suscripcion por cliente
    public ClientePlanesSuscripcionDTO obtenerPlanesSuscripcionPorCliente(Long idCliente) {
        Usuario cliente = usuarioRepository.findByIdUsuarioAndEstado(idCliente, Usuario.EstadoUsuario.ACTIVO);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado o inactivo");
        }
        // Obtenemos las suscripciones del cliente
        List<ClientePlanesSuscripcionDTO.SuscripcionClienteDTO> suscripcionClienteDTOS = new ArrayList<>();
        List<Suscripcion>  suscripciones = suscripcionRepository.findByUsuario_IdUsuario(idCliente);
        for (Suscripcion suscripcion : suscripciones) {

            PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaSuscripcionesDTO = obtenerEmpresaSuscripciones(suscripcion.getEmpresa());
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
                    new VehiculoClienteDTO(
                            suscripcion.getVehiculo().getId(),
                            suscripcion.getVehiculo().getPlaca(),
                            suscripcion.getVehiculo().getMarca(),
                            suscripcion.getVehiculo().getModelo(),
                            suscripcion.getVehiculo().getColor(),
                            suscripcion.getVehiculo().getTipoVehiculo().toString()
                    ),
                    empresaSuscripcionesDTO.getSuscripciones().stream()
                            .filter(plan -> plan.getId().equals(suscripcion.getTipoPlan().getId()))
                            .findFirst()
                            .orElse(null),
                    empresaSuscripcionesDTO.getSucursales()
            );
            suscripcionClienteDTOS.add(suscripcionClienteDTO);
        }
        return new ClientePlanesSuscripcionDTO(
                cliente.getIdUsuario(),
                cliente.getNombreUsuario(),
                suscripcionClienteDTOS
        );
    }

    //Obtenemos los vehiculos del cliente
    public List<VehiculoClienteDTO> obtenerVehiculosCliente(Long idCliente) {
        Usuario cliente = usuarioRepository.findByIdUsuarioAndEstado(idCliente, Usuario.EstadoUsuario.ACTIVO);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado o inactivo");
        }

        List<Vehiculo> vehiculos = vehiculoRepository.findByPropietario_IdPersonaAndEstado(cliente.getPersona().getIdPersona(), Vehiculo.EstadoVehiculo.ACTIVO);
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

    //Agregar una nueva suscripcion para el cliente
    @Transactional
    public String nuevaSuscripcionCliente(NuevaSuscripcionDTO nuevaSuscripcionDTO) {
        //Validar que el cliente exista
        Usuario cliente = usuarioRepository.findByIdUsuarioAndEstado(nuevaSuscripcionDTO.getIdCliente(), Usuario.EstadoUsuario.ACTIVO);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado o inactivo");
        }

        //Validar que el vehiculo exista y pertenezca al cliente
        Vehiculo vehiculo = vehiculoRepository.findById(nuevaSuscripcionDTO.getIdVehiculo()).orElse(null);
        if (vehiculo == null || !vehiculo.getPropietario().getIdPersona().equals(cliente.getPersona().getIdPersona())) {
            throw new IllegalArgumentException("Vehículo no encontrado o no pertenece al cliente");
        }

        //Validar que la empresa exista
        Empresa empresa = empresaRepository.findById(nuevaSuscripcionDTO.getIdEmpresa()).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa no encontrada");
        }

        //Validar que el tipo de plan exista y esté activo
        TipoPlan tipoPlan = tipoPlanRepository.findById(nuevaSuscripcionDTO.getIdTipoPlanSuscripcion()).orElse(null);
        if (tipoPlan == null || tipoPlan.getActivo() != TipoPlan.EstadoConfiguracion.VIGENTE) {
            throw new IllegalArgumentException("Tipo de plan no encontrado o inactivo");
        }

        //Validar que el vehiculo no tenga una suscripción activa with el mismo tipo de plan en la misma empresa
        List<Suscripcion> suscripcionesExistentes = suscripcionRepository.findByUsuario_IdUsuario(nuevaSuscripcionDTO.getIdCliente());
        for (Suscripcion suscripcion : suscripcionesExistentes) {
            if (suscripcion.getEmpresa().getIdEmpresa().equals(empresa.getIdEmpresa()) &&
                suscripcion.getTipoPlan().getId().equals(tipoPlan.getId()) &&
                suscripcion.getEstado() == Suscripcion.EstadoSuscripcion.ACTIVA &&
                vehiculo.getId().equals(suscripcion.getVehiculo().getId())
            ) {
                throw new IllegalArgumentException("El vehículo ya tiene una suscripción activa con el mismo tipo de plan en esta empresa");
            }
        }

        //Crear la nueva suscripción
        Suscripcion nuevaSuscripcion = new Suscripcion();
        nuevaSuscripcion.setEmpresa(empresa);
        nuevaSuscripcion.setUsuario(cliente);
        nuevaSuscripcion.setVehiculo(vehiculo);
        nuevaSuscripcion.setTipoPlan(tipoPlan);
        //Obtenemos la tarifa base activa de la empresa
        TarifaBase tarifaBase = tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(empresa.getIdEmpresa(), TarifaBase.EstadoTarifaBase.VIGENTE);
        if (tarifaBase == null) {
            throw new IllegalArgumentException("No hay una tarifa base vigente para la empresa seleccionada");
        }
        nuevaSuscripcion.setTarifaBaseReferencia(tarifaBase);

        // Validar periodo contratado antes de asignar
       /* try {
            nuevaSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.valueOf(nuevaSuscripcionDTO.getPeriodoContratado()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Periodo contratado no válido");
        }*/
        nuevaSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.valueOf(nuevaSuscripcionDTO.getPeriodoContratado()));
        nuevaSuscripcion.setDescuentoAplicado(BigDecimal.valueOf(0));
        //Calculamos el precio del plan según el periodo contratado
        BigDecimal precioPlan = BigDecimal.valueOf(tipoPlan.getPrecioPlan());
        if (nuevaSuscripcionDTO.getPeriodoContratado().equals("MENSUAL")) {
            nuevaSuscripcion.setPrecioPlan(precioPlan);
        } else if (nuevaSuscripcionDTO.getPeriodoContratado().equals("ANUAL")) {
            //Aplicamos un descuento del 10% por contratación anual
            BigDecimal descuentoAnual = precioPlan.multiply(BigDecimal.valueOf(0.10));
            BigDecimal precioAnualConDescuento = precioPlan.multiply(BigDecimal.valueOf(12)).subtract(descuentoAnual);
            nuevaSuscripcion.setPrecioPlan(precioAnualConDescuento);
            nuevaSuscripcion.setDescuentoAplicado(descuentoAnual);
        } /*else {
            throw new IllegalArgumentException("Periodo contratado no válido");
        }*/
        nuevaSuscripcion.setHorasMensualesIncluidas(tipoPlan.getHorasMensuales());
        nuevaSuscripcion.setHorasConsumidas(BigDecimal.valueOf(0));
        nuevaSuscripcion.setFechaInicio(LocalDateTime.now());
        //Calculamos la fecha fin según el periodo contratado
        if (nuevaSuscripcionDTO.getPeriodoContratado().equals("MENSUAL")) {
            nuevaSuscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        } else if (nuevaSuscripcionDTO.getPeriodoContratado().equals("ANUAL")) {
            nuevaSuscripcion.setFechaFin(LocalDateTime.now().plusYears(1));
        }
        nuevaSuscripcion.setFechaCompra(LocalDateTime.now());
        nuevaSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        nuevaSuscripcion.setMetodoPago(nuevaSuscripcionDTO.getMetodoPago());
        nuevaSuscripcion.setNumeroTransaccion(nuevaSuscripcionDTO.getNumeroTransaccion());

        Suscripcion suscripcionG= suscripcionRepository.save(nuevaSuscripcion);
        //Crear el historial de pago para la nueva suscripción
        HistorialPagoSuscripcion historialPago = new HistorialPagoSuscripcion();
        historialPago.setSuscripcion(suscripcionG);
        historialPago.setFechaPago(LocalDateTime.now());
        historialPago.setMontoPagado(nuevaSuscripcion.getPrecioPlan());
        historialPago.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.valueOf(nuevaSuscripcionDTO.getMetodoPago()));
        historialPago.setNumeroTransaccion(nuevaSuscripcionDTO.getNumeroTransaccion());
        historialPago.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        historialPago.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.COMPRA_INICIAL);
        historialPagoSuscripcionRepository.save(historialPago);
        return "Nueva suscripción creada con éxito";
    }

    //Renovar suscripcion
    @Transactional
    public String renovarSuscripcionCliente(RenovacionSuscripcionDTO renovarSuscripcionDTO) {
        //Validar que la suscripcion exista
        Suscripcion suscripcion = suscripcionRepository.findById(renovarSuscripcionDTO.getIdSuscripcion()).orElse(null);
        if (suscripcion == null) {
            throw new IllegalArgumentException("Suscripción no encontrada");
        }

        // Validar periodo contratado antes de procesar
        if (!renovarSuscripcionDTO.getNuevoPeriodoContratado().equals("MENSUAL") && 
            !renovarSuscripcionDTO.getNuevoPeriodoContratado().equals("ANUAL")) {
            throw new IllegalArgumentException("Periodo contratado no válido");
        }

        //Actualizar la fecha fin según el nuevo periodo contratado
        if (renovarSuscripcionDTO.getNuevoPeriodoContratado().equals("MENSUAL")) {
            suscripcion.setFechaFin(suscripcion.getFechaFin().plusMonths(1));
        } else if (renovarSuscripcionDTO.getNuevoPeriodoContratado().equals("ANUAL")) {
            suscripcion.setFechaFin(suscripcion.getFechaFin().plusYears(1));
        }

        suscripcionRepository.save(suscripcion);

        //Crear el historial de pago para la renovación de la suscripción
        HistorialPagoSuscripcion historialPago = new HistorialPagoSuscripcion();
        historialPago.setSuscripcion(suscripcion);
        historialPago.setFechaPago(LocalDateTime.now());
        historialPago.setMontoPagado(suscripcion.getPrecioPlan());
        historialPago.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.valueOf(renovarSuscripcionDTO.getMetodoPago()));
        historialPago.setNumeroTransaccion(renovarSuscripcionDTO.getNumeroTransaccion());
        historialPago.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        historialPago.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.RENOVACION);
        historialPagoSuscripcionRepository.save(historialPago);

        return "Suscripción renovada con éxito";
    }


}
