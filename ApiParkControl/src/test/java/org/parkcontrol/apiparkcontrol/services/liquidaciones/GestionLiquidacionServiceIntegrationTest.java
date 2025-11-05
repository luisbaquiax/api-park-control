package org.parkcontrol.apiparkcontrol.services.liquidaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class GestionLiquidacionServiceIntegrationTest {

    @Autowired
    private GestionLiquidacionService gestionLiquidacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
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
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;

    private Usuario testUsuarioEmpresa;
    private Usuario testCliente;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private ComercioAfiliado testComercio;
    private ConvenioComercioSucursal testConvenio;
    private Suscripcion testSuscripcion;
    private Vehiculo testVehiculo;
    private TipoPlan testTipoPlan;
    private TarifaBase testTarifaBase;
    private Rol empresaRol;
    private Rol clienteRol;

    @BeforeEach
    void setUp() {
        // Clean up database
        transaccionTicketRepository.deleteAll();
        ticketRepository.deleteAll();
        historialPagoSuscripcionRepository.deleteAll();
        liquidacionComercioRepository.deleteAll();
        corteCajaRepository.deleteAll();
        convenioComercioSucursalRepository.deleteAll();
        suscripcionRepository.deleteAll();
        comercioAfiliadoRepository.deleteAll();
        sucursalRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        vehiculoRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test roles
        empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario de empresa");
        empresaRol = rolRepository.save(empresaRol);

        clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Cliente del sistema");
        clienteRol = rolRepository.save(clienteRol);

        // Create test personas
        Persona personaEmpresa = new Persona();
        personaEmpresa.setNombre("Admin");
        personaEmpresa.setApellido("Empresa");
        personaEmpresa.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        personaEmpresa.setDpi(generateUniqueDpi());
        personaEmpresa.setCorreo("admin@empresa.com");
        personaEmpresa.setTelefono("12345678");
        personaEmpresa.setDireccionCompleta("Admin Address");
        personaEmpresa.setCiudad("Guatemala");
        personaEmpresa.setPais("Guatemala");
        personaEmpresa.setCodigoPostal("01001");
        personaEmpresa.setEstado(Persona.Estado.ACTIVO);
        personaEmpresa = personaRepository.save(personaEmpresa);

        Persona personaCliente = new Persona();
        personaCliente.setNombre("Juan");
        personaCliente.setApellido("Cliente");
        personaCliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        personaCliente.setDpi(generateUniqueDpi());
        personaCliente.setCorreo("juan@cliente.com");
        personaCliente.setTelefono("87654321");
        personaCliente.setDireccionCompleta("Cliente Address");
        personaCliente.setCiudad("Guatemala");
        personaCliente.setPais("Guatemala");
        personaCliente.setCodigoPostal("01002");
        personaCliente.setEstado(Persona.Estado.ACTIVO);
        personaCliente = personaRepository.save(personaCliente);

        // Create test usuarios
        testUsuarioEmpresa = new Usuario();
        testUsuarioEmpresa.setPersona(personaEmpresa);
        testUsuarioEmpresa.setRol(empresaRol);
        testUsuarioEmpresa.setNombreUsuario("adminempresa");
        testUsuarioEmpresa.setContraseniaHash("hashedPassword");
        testUsuarioEmpresa.setDobleFactorHabilitado(false);
        testUsuarioEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioEmpresa.setDebeCambiarContrasenia(false);
        testUsuarioEmpresa.setEsPrimeraVez(false);
        testUsuarioEmpresa.setIntentosFallidos(0);
        testUsuarioEmpresa = usuarioRepository.save(testUsuarioEmpresa);

        testCliente = new Usuario();
        testCliente.setPersona(personaCliente);
        testCliente.setRol(clienteRol);
        testCliente.setNombreUsuario("juancliente");
        testCliente.setContraseniaHash("hashedPassword");
        testCliente.setDobleFactorHabilitado(false);
        testCliente.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testCliente.setDebeCambiarContrasenia(false);
        testCliente.setEsPrimeraVez(false);
        testCliente.setIntentosFallidos(0);
        testCliente = usuarioRepository.save(testCliente);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testUsuarioEmpresa);
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Fiscal Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuarioEmpresa);
        testSucursal.setNombre("Sucursal Test");
        testSucursal.setDireccionCompleta("Sucursal Address");
        testSucursal.setCiudad("Guatemala");
        testSucursal.setDepartamento("Guatemala");
        testSucursal.setLatitud(new BigDecimal("14.6349"));
        testSucursal.setLongitud(new BigDecimal("-90.5069"));
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(100);
        testSucursal.setTelefonoContacto("12345678");
        testSucursal.setCorreoContacto("sucursal@test.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);

        // Create test comercio afiliado
        testComercio = new ComercioAfiliado();
        testComercio.setNombreComercial("Comercio Test");
        testComercio.setRazonSocial("Comercio Test S.A.");
        testComercio.setNit("9876543-2");
        testComercio.setTipoComercio("Restaurante");
        testComercio.setTelefono("87654321");
        testComercio.setCorreoContacto("comercio@test.com");
        testComercio.setEstado(ComercioAfiliado.Estado.ACTIVO);
        testComercio = comercioAfiliadoRepository.save(testComercio);

        // Create test vehiculo
        testVehiculo = new Vehiculo();
        testVehiculo.setPropietario(personaCliente);
        testVehiculo.setPlaca("ABC123");
        testVehiculo.setMarca("Toyota");
        testVehiculo.setModelo("Corolla");
        testVehiculo.setColor("Blanco");
        testVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculo = vehiculoRepository.save(testVehiculo);

        // Create test tarifa base
        testTarifaBase = new TarifaBase();
        testTarifaBase.setEmpresa(testEmpresa);
        testTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        testTarifaBase.setMoneda("GTQ");
        testTarifaBase.setFechaVigenciaInicio(LocalDate.now().minusDays(30));
        testTarifaBase.setFechaVigenciaFin(LocalDate.now().plusDays(330));
        testTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        testTarifaBase = tarifaBaseRepository.save(testTarifaBase);

        // Create test tipo plan
        testTipoPlan = new TipoPlan();
        testTipoPlan.setEmpresa(testEmpresa);
        testTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        testTipoPlan.setCodigoPlan("WW-001");
        testTipoPlan.setDescripcion("Plan Workweek");
        testTipoPlan.setPrecioPlan(300.00);
        testTipoPlan.setHorasDia(8);
        testTipoPlan.setHorasMensuales(160);
        testTipoPlan.setDiasAplicables("L-M-X-J-V");
        testTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        testTipoPlan.setOrdenBeneficio(2);
        testTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        testTipoPlan = tipoPlanRepository.save(testTipoPlan);

        // Create test suscripcion
        testSuscripcion = new Suscripcion();
        testSuscripcion.setEmpresa(testEmpresa);
        testSuscripcion.setUsuario(testCliente);
        testSuscripcion.setVehiculo(testVehiculo);
        testSuscripcion.setTipoPlan(testTipoPlan);
        testSuscripcion.setTarifaBaseReferencia(testTarifaBase);
        testSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.ANUAL);
        testSuscripcion.setDescuentoAplicado(new BigDecimal("15.00"));
        testSuscripcion.setPrecioPlan(new BigDecimal("3000.00"));
        testSuscripcion.setHorasMensualesIncluidas(200);
        testSuscripcion.setHorasConsumidas(new BigDecimal("30.0"));
        testSuscripcion.setFechaInicio(LocalDateTime.now().minusDays(60));
        testSuscripcion.setFechaFin(LocalDateTime.now().plusDays(300));
        testSuscripcion.setFechaCompra(LocalDateTime.now().minusDays(60));
        testSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        testSuscripcion.setMetodoPago("TRANSFERENCIA");
        testSuscripcion.setNumeroTransaccion("TXN789012");
        testSuscripcion = suscripcionRepository.save(testSuscripcion);

        // Create test convenio
        testConvenio = new ConvenioComercioSucursal();
        testConvenio.setComercioAfiliado(testComercio);
        testConvenio.setSucursal(testSucursal);
        testConvenio.setHorasGratisMaximo(new BigDecimal("10.00"));
        testConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        testConvenio.setTarifaPorHora(new BigDecimal("15.00"));
        testConvenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(2));
        testConvenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(10));
        testConvenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        testConvenio.setCreadoPor(testUsuarioEmpresa);
        testConvenio = convenioComercioSucursalRepository.save(testConvenio);
    }

    @Test
    void testObtenerDetallesLiquidacionesPorEmpresa_Integration() {
        // Arrange - Create corte caja and liquidacion
        CorteCaja corteCaja = new CorteCaja();
        corteCaja.setSucursal(testSucursal);
        corteCaja.setPeriodo(CorteCaja.Periodo.MENSUAL);
        corteCaja.setFechaInicio(LocalDateTime.now().minusDays(30));
        corteCaja.setFechaFin(LocalDateTime.now());
        corteCaja.setTotalIngresosTarifas(new BigDecimal("1000.00"));
        corteCaja.setTotalIngresosExcedentes(new BigDecimal("200.00"));
        corteCaja.setTotalHorasComercio(new BigDecimal("50.00"));
        corteCaja.setTotalLiquidacionComercios(new BigDecimal("750.00"));
        corteCaja.setTotalNeto(new BigDecimal("1950.00"));
        corteCaja.setGeneradoPor(testUsuarioEmpresa);
        corteCaja.setFechaGeneracion(LocalDateTime.now());
        corteCaja.setEstado(CorteCaja.Estado.PRELIMINAR);
        corteCaja = corteCajaRepository.save(corteCaja);

        LiquidacionComercio liquidacion = new LiquidacionComercio();
        liquidacion.setCorteCaja(corteCaja);
        liquidacion.setComercio(testComercio);
        liquidacion.setConvenio(testConvenio);
        liquidacion.setTotalHorasOtorgadas(new BigDecimal("10.00"));
        liquidacion.setTarifaPorHora(new BigDecimal("15.00"));
        liquidacion.setMontoTotal(new BigDecimal("150.00"));
        liquidacion.setEstado(LiquidacionComercio.EstadoLiquidacion.PENDIENTE);
        liquidacion.setObservaciones("Liquidación de prueba");
        liquidacionComercioRepository.save(liquidacion);

        // Act
        DetallesLiquidacionesDTO result = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCortesDeCaja());
        assertEquals(1, result.getCortesDeCaja().size());

        DetallesLiquidacionesDTO.CortesDeCajaDTO corte = result.getCortesDeCaja().get(0);
        assertEquals(corteCaja.getIdCorteCaja(), corte.getIdCorteCaja());
        assertEquals("Sucursal Test", corte.getSucursalNombre());
        assertEquals("MENSUAL", corte.getPeriodo());
        assertEquals("50.00", corte.getTotalHorasComercio());
        assertEquals("750.00", corte.getTotalLiquidacionComercios());
        assertEquals("1950.00", corte.getTotalNeto());
        assertEquals("adminempresa", corte.getGeneradoPorNombreUsuario());
        assertEquals("PRELIMINAR", corte.getEstado());

        assertEquals(1, corte.getDetallesComercios().size());
        DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalle = corte.getDetallesComercios().get(0);
        assertEquals("Comercio Test", detalle.getComercioNombre());
        assertEquals("10.00", detalle.getTotalHorasOtorgadas());
        assertEquals("15.00", detalle.getTarifaPorHora());
        assertEquals("150.00", detalle.getMontoTotal());
        assertEquals("PENDIENTE", detalle.getEstado());
        assertEquals("Liquidación de prueba", detalle.getObservaciones());
    }

    @Test
    void testObtenerDetallesPagosSuscripcion_Integration() {
        // Arrange - Create historial pago
        HistorialPagoSuscripcion historialPago = new HistorialPagoSuscripcion();
        historialPago.setSuscripcion(testSuscripcion);
        historialPago.setFechaPago(LocalDateTime.now());
        historialPago.setMontoPagado(new BigDecimal("3000.00"));
        historialPago.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.TRANSFERENCIA_BANCARIA);
        historialPago.setNumeroTransaccion("TXN123456789");
        historialPago.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        historialPago.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.RENOVACION);
        historialPagoSuscripcionRepository.save(historialPago);

        // Act
        List<DetallePagosSuscripcionDTO> result = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetallePagosSuscripcionDTO pago = result.get(0);
        assertEquals(historialPago.getIdHistorialPagoSuscripcion(), pago.getIdHistorialPago());
        assertEquals(testSuscripcion.getId(), pago.getIdSuscripcion());
        assertEquals("Juan", pago.getNombreCliente());
        assertEquals("3000.00", pago.getMontoPagado());
        assertEquals("TRANSFERENCIA_BANCARIA", pago.getMetodoPago());
        assertEquals("TXN123456789", pago.getNumeroTransaccion());
        assertEquals("COMPLETADO", pago.getEstadoPago());
        assertEquals("RENOVACION", pago.getMotivoPago());
    }


    @Test
    void testActualizarPeriodosCortesDeCaja_Integration() {
        // Arrange - Create active convenio
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(testComercio);
        convenio.setSucursal(testSucursal);
        convenio.setHorasGratisMaximo(new BigDecimal("20.00"));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        convenio.setTarifaPorHora(new BigDecimal("12.00"));
        convenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(2)); // 2 months ago to trigger update
        convenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(10));
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(testUsuarioEmpresa);
        convenio = convenioComercioSucursalRepository.save(convenio);
        
        // Remove the testConvenio that was created in setUp to avoid duplicates
        convenioComercioSucursalRepository.delete(testConvenio);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);

        // Verify corte caja was created
        List<CorteCaja> cortesCaja = corteCajaRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertFalse(cortesCaja.isEmpty());

        CorteCaja corteCaja = cortesCaja.get(0);
        assertEquals(CorteCaja.Periodo.MENSUAL, corteCaja.getPeriodo());
        assertEquals(CorteCaja.Estado.PRELIMINAR, corteCaja.getEstado());
        assertEquals(testUsuarioEmpresa.getIdUsuario(), corteCaja.getGeneradoPor().getIdUsuario());

        // Verify liquidacion was created
        List<LiquidacionComercio> liquidaciones = liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(corteCaja.getIdCorteCaja());
        assertEquals(1, liquidaciones.size());

        LiquidacionComercio liquidacion = liquidaciones.get(0);
        assertEquals(LiquidacionComercio.EstadoLiquidacion.PENDIENTE, liquidacion.getEstado());
        assertEquals(testComercio.getId(), liquidacion.getComercio().getId());
        assertEquals(convenio.getId(), liquidacion.getConvenio().getId());
        assertEquals(0, new BigDecimal("20.00").compareTo(liquidacion.getTotalHorasOtorgadas()));
        assertEquals(0, new BigDecimal("12.00").compareTo(liquidacion.getTarifaPorHora()));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_SinConveniosActivos_Integration() {
        // Arrange - Delete the testConvenio to have no active convenios
        convenioComercioSucursalRepository.delete(testConvenio);
        
        // Act - No active convenios
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertEquals("No hay convenios activos para actualizar.", result);

        // Verify no cortes were created
        List<CorteCaja> cortesCaja = corteCajaRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertTrue(cortesCaja.isEmpty());
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_DiferentisPeriodos_Integration() {
        // Remove existing convenio
        convenioComercioSucursalRepository.delete(testConvenio);
        
        // Test different periods
        ConvenioComercioSucursal convenioSemanal = new ConvenioComercioSucursal();
        convenioSemanal.setComercioAfiliado(testComercio);
        convenioSemanal.setSucursal(testSucursal);
        convenioSemanal.setHorasGratisMaximo(new BigDecimal("5.00"));
        convenioSemanal.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.SEMANAL);
        convenioSemanal.setTarifaPorHora(new BigDecimal("18.00"));
        convenioSemanal.setFechaInicioConvenio(LocalDateTime.now().minusWeeks(3)); // 3 weeks ago
        convenioSemanal.setFechaFinConvenio(LocalDateTime.now().plusWeeks(10));
        convenioSemanal.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenioSemanal.setCreadoPor(testUsuarioEmpresa);
        convenioComercioSucursalRepository.save(convenioSemanal);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);

        // Verify semanal corte was created
        List<CorteCaja> cortesCaja = corteCajaRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, cortesCaja.size());
        assertEquals(CorteCaja.Periodo.SEMANAL, cortesCaja.get(0).getPeriodo());
    }

    @Test
    void testObtenerDetalleTransaccionesTicket_Integration() {
        // Arrange - Create ticket and transaction
        Ticket ticket = new Ticket();
        ticket.setFolioNumerico("FOL987654");
        ticket.setSucursal(testSucursal);
        ticket.setVehiculo(testVehiculo);
        ticket.setSuscripcion(testSuscripcion);
        ticket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        ticket.setFechaHoraEntrada(LocalDateTime.now().minusHours(5));
        ticket.setFechaHoraSalida(LocalDateTime.now().minusHours(1));
        ticket.setDuracionMinutos(240);
        ticket.setCodigoQr("QR987654");
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticket = ticketRepository.save(ticket);

        TransaccionTicket transaccion = new TransaccionTicket();
        transaccion.setTicket(ticket);
        transaccion.setTipoCobro(TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION);
        transaccion.setHorasCobradas(new BigDecimal("4.0"));
        transaccion.setHorasGratisComercio(new BigDecimal("0.5"));
        transaccion.setTarifaAplicada(new BigDecimal("15.00"));
        transaccion.setSubtotal(new BigDecimal("60.00"));
        transaccion.setDescuento(new BigDecimal("0.00"));
        transaccion.setTotal(new BigDecimal("60.00"));
        transaccion.setMetodoPago("TARJETA");
        transaccion.setNumeroTransaccion("PAY987654");
        transaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        transaccion.setFechaTransaccion(LocalDateTime.now().minusHours(1));
        transaccionTicketRepository.save(transaccion);

        // Act
        List<DetalleTransaccionTicketDTO> result = gestionLiquidacionService.obtenerDetalleTransaccionesTicket(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleTransaccionTicketDTO detalle = result.get(0);
        assertEquals(transaccion.getIdTransaccion(), detalle.getIdTransaccion());
        assertEquals(ticket.getId(), detalle.getIdTicket());
        assertEquals("Juan", detalle.getNombreCliente());
        assertEquals("EXCEDENTE_SUSCRIPCION", detalle.getTipoCobro());
        assertEquals("4.0", detalle.getHorasCobradas());
        assertEquals("0.5", detalle.getHorasGratisComercio());
        assertEquals("15.00", detalle.getTarifaAplicada());
        assertEquals("60.00", detalle.getSubtotal());
        assertEquals("0.00", detalle.getDescuento());
        assertEquals("60.00", detalle.getTotal());
        assertEquals("TARJETA", detalle.getMetodoPago());
        assertEquals("PAY987654", detalle.getNumeroTransaccion());
        assertEquals("PAGADO", detalle.getEstado());
    }

    @Test
    void testIntegrationCompleteWorkflow_Integration() {
        // Arrange - Complete workflow test
        // 1. Create subscription with payment
        HistorialPagoSuscripcion historialPago = new HistorialPagoSuscripcion();
        historialPago.setSuscripcion(testSuscripcion);
        historialPago.setFechaPago(LocalDateTime.now().minusDays(5));
        historialPago.setMontoPagado(new BigDecimal("2500.00"));
        historialPago.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.TRANSFERENCIA_BANCARIA);
        historialPago.setNumeroTransaccion("TXN987654321");
        historialPago.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        historialPago.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.RENOVACION);
        historialPagoSuscripcionRepository.save(historialPago);

        // 2. Create ticket and transaction
        Ticket ticket = new Ticket();
        ticket.setFolioNumerico("FOL987654");
        ticket.setSucursal(testSucursal);
        ticket.setVehiculo(testVehiculo);
        ticket.setSuscripcion(testSuscripcion);
        ticket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        ticket.setFechaHoraEntrada(LocalDateTime.now().minusHours(5));
        ticket.setFechaHoraSalida(LocalDateTime.now().minusHours(1));
        ticket.setDuracionMinutos(240);
        ticket.setCodigoQr("QR987654");
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticket = ticketRepository.save(ticket);

        TransaccionTicket transaccion = new TransaccionTicket();
        transaccion.setTicket(ticket);
        transaccion.setTipoCobro(TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION);
        transaccion.setHorasCobradas(new BigDecimal("4.0"));
        transaccion.setHorasGratisComercio(new BigDecimal("0.5"));
        transaccion.setTarifaAplicada(new BigDecimal("15.00"));
        transaccion.setSubtotal(new BigDecimal("60.00"));
        transaccion.setDescuento(new BigDecimal("0.00"));
        transaccion.setTotal(new BigDecimal("60.00"));
        transaccion.setMetodoPago("TARJETA");
        transaccion.setNumeroTransaccion("PAY987654");
        transaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        transaccion.setFechaTransaccion(LocalDateTime.now().minusHours(1));
        transaccionTicketRepository.save(transaccion);

        // 3. Remove existing convenio and create new one for the test
        convenioComercioSucursalRepository.delete(testConvenio);
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(testComercio);
        convenio.setSucursal(testSucursal);
        convenio.setHorasGratisMaximo(new BigDecimal("15.00"));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.DIARIO);
        convenio.setTarifaPorHora(new BigDecimal("20.00"));
        convenio.setFechaInicioConvenio(LocalDateTime.now().minusDays(3)); // 3 days ago
        convenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(12));
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(testUsuarioEmpresa);
        convenioComercioSucursalRepository.save(convenio);

        // Act - Test all methods
        String cortesResult = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(testUsuarioEmpresa.getIdUsuario());
        DetallesLiquidacionesDTO liquidacionesResult = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(testUsuarioEmpresa.getIdUsuario());
        List<DetallePagosSuscripcionDTO> pagosResult = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(testUsuarioEmpresa.getIdUsuario());
        List<DetalleTransaccionTicketDTO> transaccionesResult = gestionLiquidacionService.obtenerDetalleTransaccionesTicket(testUsuarioEmpresa.getIdUsuario());

        // Assert
        // Verify cortes de caja
        assertEquals("Periodos de cortes de caja actualizados correctamente.", cortesResult);
        assertNotNull(liquidacionesResult);
        assertFalse(liquidacionesResult.getCortesDeCaja().isEmpty());

        // Verify pagos suscripcion
        assertNotNull(pagosResult);
        assertEquals(1, pagosResult.size());
        DetallePagosSuscripcionDTO pago = pagosResult.get(0);
        assertEquals("Juan", pago.getNombreCliente());
        assertEquals("2500.00", pago.getMontoPagado());
        assertEquals("TRANSFERENCIA_BANCARIA", pago.getMetodoPago());
        assertEquals("RENOVACION", pago.getMotivoPago());

        // Verify transacciones
        assertNotNull(transaccionesResult);
        assertEquals(1, transaccionesResult.size());
        DetalleTransaccionTicketDTO transaccionResult = transaccionesResult.get(0);
        assertEquals("Juan", transaccionResult.getNombreCliente());
        assertEquals("EXCEDENTE_SUSCRIPCION", transaccionResult.getTipoCobro());
        assertEquals("4.0", transaccionResult.getHorasCobradas());
        assertEquals("PAGADO", transaccionResult.getEstado());
    }

    @Test
    void testErrorHandling_EmpresaNotFound_Integration() {
        // Arrange - Create user without empresa
        Persona personaSinEmpresa = new Persona();
        personaSinEmpresa.setNombre("Usuario");
        personaSinEmpresa.setApellido("Sin Empresa");
        personaSinEmpresa.setFechaNacimiento(LocalDate.of(1985, 6, 15));
        personaSinEmpresa.setDpi(generateUniqueDpi());
        personaSinEmpresa.setCorreo("sin.empresa@test.com");
        personaSinEmpresa.setTelefono("11111111");
        personaSinEmpresa.setDireccionCompleta("Sin Empresa Address");
        personaSinEmpresa.setCiudad("Guatemala");
        personaSinEmpresa.setPais("Guatemala");
        personaSinEmpresa.setCodigoPostal("01003");
        personaSinEmpresa.setEstado(Persona.Estado.ACTIVO);
        personaSinEmpresa = personaRepository.save(personaSinEmpresa);

        Usuario usuarioSinEmpresa = new Usuario();
        usuarioSinEmpresa.setPersona(personaSinEmpresa);
        usuarioSinEmpresa.setRol(empresaRol);
        usuarioSinEmpresa.setNombreUsuario("sin.empresa");
        usuarioSinEmpresa.setContraseniaHash("hashedPassword");
        usuarioSinEmpresa.setDobleFactorHabilitado(false);
        usuarioSinEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioSinEmpresa.setDebeCambiarContrasenia(false);
        usuarioSinEmpresa.setEsPrimeraVez(false);
        usuarioSinEmpresa.setIntentosFallidos(0);
        usuarioSinEmpresa = usuarioRepository.save(usuarioSinEmpresa);

        // Act & Assert
        Usuario finalUsuarioSinEmpresa = usuarioSinEmpresa;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(finalUsuarioSinEmpresa.getIdUsuario());
        });

        assertEquals("La empresa no existe", exception.getMessage());
    }

    private String generateUniqueDpi() {
        // Usar UUID combinado con timestamp para garantizar unicidad
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
