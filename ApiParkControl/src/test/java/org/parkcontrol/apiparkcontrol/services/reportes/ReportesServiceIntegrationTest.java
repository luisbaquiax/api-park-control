package org.parkcontrol.apiparkcontrol.services.reportes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.DetalleEmpresaFlotillaDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.DetalleSucursalesIncidenciasDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetalleTransaccionTicketDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetallesLiquidacionesDTO;
import org.parkcontrol.apiparkcontrol.dto.reportes.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.empresa_flotilla.GestionEmpresaFlotillaService;
import org.parkcontrol.apiparkcontrol.services.gestion_incidencias.ResolucionIncidenciasService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ReportesServiceIntegrationTest {

    @Autowired
    private ReportesService reportesService;

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private OcupacionSucursalRepository ocupacionSucursalRepository;
    @Autowired
    private TransaccionTicketRepository transaccionTicketRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Autowired
    private CorteCajaRepository corteCajaRepository;
    @Autowired
    private LiquidacionComercioRepository liquidacionComercioRepository;
    @Autowired
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;

    @MockBean
    private ResolucionIncidenciasService resolucionIncidenciasService;
    @MockBean
    private GestionEmpresaFlotillaService gestionEmpresaFlotillaService;

    private Usuario testUsuarioEmpresa;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TipoPlan testTipoPlan;
    private Suscripcion testSuscripcion;
    private ComercioAfiliado testComercio;
    private Vehiculo testVehiculo;
    private TarifaBase testTarifaBase;
    private Rol empresaRol;

    @BeforeEach
    void setUp() {
        // Clean database
        acreditacionHorasComercioRepository.deleteAll();
        liquidacionComercioRepository.deleteAll();
        corteCajaRepository.deleteAll();
        transaccionTicketRepository.deleteAll();
        ticketRepository.deleteAll();
        convenioComercioSucursalRepository.deleteAll();
        comercioAfiliadoRepository.deleteAll();
        suscripcionRepository.deleteAll();
        configuracionDescuentoPlanRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        vehiculoRepository.deleteAll();
        ocupacionSucursalRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario de empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Create test persona and usuario
        Persona testPersona = new Persona();
        testPersona.setNombre("Admin");
        testPersona.setApellido("Empresa");
        testPersona.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("admin@empresa.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Admin Address");
        testPersona.setCiudad("Guatemala");
        testPersona.setPais("Guatemala");
        testPersona.setCodigoPostal("01001");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        testUsuarioEmpresa = new Usuario();
        testUsuarioEmpresa.setPersona(testPersona);
        testUsuarioEmpresa.setRol(empresaRol);
        testUsuarioEmpresa.setNombreUsuario("adminempresa");
        testUsuarioEmpresa.setContraseniaHash("hashedPassword");
        testUsuarioEmpresa.setDobleFactorHabilitado(false);
        testUsuarioEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioEmpresa.setDebeCambiarContrasenia(false);
        testUsuarioEmpresa.setEsPrimeraVez(false);
        testUsuarioEmpresa.setIntentosFallidos(0);
        testUsuarioEmpresa = usuarioRepository.save(testUsuarioEmpresa);

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
        testVehiculo.setPropietario(testPersona);
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
        testSuscripcion.setUsuario(testUsuarioEmpresa);
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
    }

    @Test
    void testGenerarReporteOcupacionPorSucursal_Integration() {
        // Arrange - Create ocupacion data
        OcupacionSucursal ocupacion = new OcupacionSucursal();
        ocupacion.setSucursal(testSucursal);
        ocupacion.setFechaHora(LocalDateTime.now());
        ocupacion.setOcupacion2R(25);
        ocupacion.setCapacidad2R(50);
        ocupacion.setOcupacion4R(40);
        ocupacion.setCapacidad4R(100);
        // Establecer los porcentajes manualmente ya que son campos requeridos
        ocupacion.setPorcentajeOcupacion2R(BigDecimal.valueOf(50));
        ocupacion.setPorcentajeOcupacion4R(BigDecimal.valueOf(40));
        ocupacionSucursalRepository.save(ocupacion);

        // Act
        List<ReporteOcupacionDTO> result = reportesService.generarReporteOcupacionPorSucursal(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteOcupacionDTO reporte = result.get(0);
        assertEquals(testSucursal.getIdSucursal(), reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());
        assertEquals("Sucursal Address", reporte.getDireccionCompleta());
        assertEquals("Guatemala", reporte.getCiudad());
        assertEquals("Guatemala", reporte.getDepartamento());
        assertEquals("08:00", reporte.getHoraApertura());
        assertEquals("18:00", reporte.getHoraCierre());
        assertEquals(50, reporte.getCapacidad2Ruedas());
        assertEquals(100, reporte.getCapacidad4Ruedas());

        assertNotNull(reporte.getDetallesOcupacion());
        ReporteOcupacionDTO.DetalleOcupacionDTO detalle = reporte.getDetallesOcupacion();
        assertEquals(25, detalle.getOcupacion2R());
        assertEquals(50, detalle.getCapacidad2R());
        assertEquals(40, detalle.getOcupacion4R());
        assertEquals(100, detalle.getCapacidad4R());
        assertEquals("50.00%", detalle.getPorcentajeOcupacion2R());
        assertEquals("40.00%", detalle.getPorcentajeOcupacion4R());
    }

    @Test
    void testGenerarReporteFacturacionPorSucursal_Integration() {
        // Arrange - Create complete ticket and transaction
        Ticket ticket = new Ticket();
        ticket.setFolioNumerico("FOL123456");
        ticket.setSucursal(testSucursal);
        ticket.setVehiculo(testVehiculo);
        ticket.setSuscripcion(testSuscripcion);
        ticket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        ticket.setFechaHoraEntrada(LocalDateTime.now().minusHours(3));
        ticket.setFechaHoraSalida(LocalDateTime.now());
        ticket.setDuracionMinutos(180);
        ticket.setCodigoQr("QR123456");
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticket = ticketRepository.save(ticket);

        TransaccionTicket transaccion = new TransaccionTicket();
        transaccion.setTicket(ticket);
        transaccion.setTipoCobro(TransaccionTicket.TipoCobro.TARIFA_NORMAL);
        transaccion.setHorasCobradas(new BigDecimal("3.0"));
        transaccion.setHorasGratisComercio(new BigDecimal("0.0"));
        transaccion.setTarifaAplicada(new BigDecimal("15.00"));
        transaccion.setSubtotal(new BigDecimal("45.00"));
        transaccion.setDescuento(new BigDecimal("0.00"));
        transaccion.setTotal(new BigDecimal("45.00"));
        transaccion.setMetodoPago("EFECTIVO");
        transaccion.setNumeroTransaccion("TXN123456");
        transaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion = transaccionTicketRepository.save(transaccion);

        // Act
        List<ReportesFacturacionSucursalDTO> result = reportesService.generarReporteFacturacionPorSucursal(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReportesFacturacionSucursalDTO reporte = result.get(0);
        assertEquals(testSucursal.getIdSucursal(), reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());

        assertEquals(1, reporte.getDetallesFacturacion().size());
        DetalleTransaccionTicketDTO detalle = reporte.getDetallesFacturacion().get(0);
        assertEquals(transaccion.getIdTransaccion(), detalle.getIdTransaccion());
        assertEquals(ticket.getId(), detalle.getIdTicket());
        assertEquals("Admin", detalle.getNombreCliente());
        assertEquals("45.00", detalle.getTotal());
    }

    @Test
    void testGenerarReporteSuscripciones_Integration() {
        // Arrange - Create configuracion descuento
        ConfiguracionDescuentoPlan config = new ConfiguracionDescuentoPlan();
        config.setTipoPlan(testTipoPlan);
        config.setDescuentoMensual(new BigDecimal("15.00"));
        config.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        config.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        config.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        config.setCreadoPor(testUsuarioEmpresa);
        config.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(config);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteSuscripcionesDTO reporte = result.get(0);
        assertEquals(testTipoPlan.getId(), reporte.getIdTipoPlan());
        assertEquals("WORKWEEK", reporte.getNombrePlan());
        assertEquals("WW-001", reporte.getCodigoPlan());
        assertEquals(160, reporte.getHorasMensuales());

        assertNotNull(reporte.getConfiguracionDescuento());
        assertEquals(15.00, reporte.getConfiguracionDescuento().getDescuentoMensual());

        assertEquals(1, reporte.getDetallesSuscriptores().size());
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = reporte.getDetallesSuscriptores().get(0);
        assertEquals(testSuscripcion.getId(), suscriptor.getIdSuscripcion());
        assertEquals("Admin", suscriptor.getNombreSuscriptor());
        assertEquals("ABC123", suscriptor.getPlacaVehiculo());
        assertEquals("ACTIVA", suscriptor.getEstadoSuscripcion());
    }

    @Test
    void testGenerarReporteComercioAfiliado_Integration() {
        // Arrange - Create convenio
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(testComercio);
        convenio.setSucursal(testSucursal);
        convenio.setHorasGratisMaximo(new BigDecimal("10.00"));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        convenio.setTarifaPorHora(new BigDecimal("15.00"));
        convenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(1));
        convenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(11));
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(testUsuarioEmpresa);
        convenio.setFechaCreacion(LocalDateTime.now());
        convenio = convenioComercioSucursalRepository.save(convenio);

        // Act
        List<ReporteComercioAfiliadoDTO> result = reportesService.generarReporteComercioAfiliado(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteComercioAfiliadoDTO reporte = result.get(0);
        assertEquals(testComercio.getId(), reporte.getIdComercioAfiliado());
        assertEquals("Comercio Test", reporte.getNombreComercial());
        assertEquals("ACTIVO", reporte.getEstado());

        assertEquals(1, reporte.getDetallesConvenioComercio().size());
        ReporteComercioAfiliadoDTO.DetalleConvenioComercioDTO convenioDetalle = 
                reporte.getDetallesConvenioComercio().get(0);
        assertEquals(convenio.getId(), convenioDetalle.getIdConvenioComercio());
        assertEquals("Sucursal Test", convenioDetalle.getNombreSucursal());
    }

    @Test
    void testGenerarReporteCortesDeCaja_Integration() {
        // Arrange - Create corte de caja
        CorteCaja corteCaja = new CorteCaja();
        corteCaja.setSucursal(testSucursal);
        corteCaja.setPeriodo(CorteCaja.Periodo.MENSUAL);
        corteCaja.setFechaInicio(LocalDateTime.now().minusMonths(1));
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

        // Act
        List<ReporteCorteDeCajaDTO> result = reportesService.generarReporteCortesDeCaja(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteCorteDeCajaDTO reporte = result.get(0);
        assertEquals(testSucursal.getIdSucursal(), reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());

        assertEquals(1, reporte.getCortesDeCajas().size());
        DetallesLiquidacionesDTO.CortesDeCajaDTO corte = reporte.getCortesDeCajas().get(0);
        assertEquals(corteCaja.getIdCorteCaja(), corte.getIdCorteCaja());
        assertEquals("MENSUAL", corte.getPeriodo());
        assertEquals("PRELIMINAR", corte.getEstado());
    }

    @Test
    void testGenerarReporteIncidencias_Integration() throws Exception {
        // Arrange - Mock service call
        DetalleSucursalesIncidenciasDTO mockIncidencia = new DetalleSucursalesIncidenciasDTO();
        mockIncidencia.setIdSucursal(testSucursal.getIdSucursal());
        mockIncidencia.setNombreSucursal("Sucursal Test");

        when(resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(List.of(mockIncidencia));

        // Act
        ReportesDeIncidenciasDTO result = reportesService.generarReporteIncidencias(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getDetalleSucursalesIncidenciasDTO());
        assertEquals(1, result.getDetalleSucursalesIncidenciasDTO().size());
        assertEquals(testSucursal.getIdSucursal(), result.getDetalleSucursalesIncidenciasDTO().get(0).getIdSucursal());
    }

    @Test
    void testGenerarReporteFlotasEmpresariales_Integration() throws Exception {
        // Arrange - Mock service call
        DetalleEmpresaFlotillaDTO mockFlotilla = new DetalleEmpresaFlotillaDTO();
        mockFlotilla.setEmpresasFlotilla(List.of());

        when(gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(mockFlotilla);

        // Act
        DetalleEmpresaFlotillaDTO result = reportesService.generarReporteFlotasEmpresariales(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(mockFlotilla, result);
    }

    @Test
    void testCalculosPrivados_HorasYExcedentes_Integration() {
        // Arrange - Create ticket and transactions for calculations
        Ticket ticket = new Ticket();
        ticket.setFolioNumerico("FOL789");
        ticket.setSucursal(testSucursal);
        ticket.setVehiculo(testVehiculo);
        ticket.setSuscripcion(testSuscripcion);
        ticket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        ticket.setFechaHoraEntrada(LocalDateTime.now().minusHours(4));
        ticket.setFechaHoraSalida(LocalDateTime.now());
        ticket.setDuracionMinutos(240);
        ticket.setCodigoQr("QR789");
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticket = ticketRepository.save(ticket);

        // Solo crear una transacción que puede manejar el método findByTicket_Id
        TransaccionTicket transaccionCompleta = new TransaccionTicket();
        transaccionCompleta.setTicket(ticket);
        transaccionCompleta.setTipoCobro(TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION);
        transaccionCompleta.setHorasCobradas(new BigDecimal("4.0")); // Total de horas
        transaccionCompleta.setHorasGratisComercio(new BigDecimal("0.0"));
        transaccionCompleta.setTarifaAplicada(new BigDecimal("15.00"));
        transaccionCompleta.setSubtotal(new BigDecimal("60.00"));
        transaccionCompleta.setDescuento(new BigDecimal("0.00"));
        transaccionCompleta.setTotal(new BigDecimal("20.00")); // Solo se cobra excedente
        transaccionCompleta.setMetodoPago("TARJETA");
        transaccionCompleta.setNumeroTransaccion("TXN789E");
        transaccionCompleta.setEstado(TransaccionTicket.Estado.PAGADO);
        transaccionCompleta.setFechaTransaccion(LocalDateTime.now());
        transaccionTicketRepository.save(transaccionCompleta);

        // Act - Generate report to test private calculations
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = result.get(0).getDetallesSuscriptores().get(0);
        assertEquals(4, suscriptor.getTotalHorasUtilizadas()); // 4 horas totales
        assertEquals(20.0, suscriptor.getTotalExcedentePagado()); // Solo el total del excedente
    }

    @Test
    void testCompleteReportWorkflow_Integration() throws Exception {
        // Arrange - Create complete scenario with all data
        
        // 1. Ocupacion data con porcentajes calculados correctamente
        OcupacionSucursal ocupacion = new OcupacionSucursal();
        ocupacion.setSucursal(testSucursal);
        ocupacion.setFechaHora(LocalDateTime.now());
        ocupacion.setOcupacion2R(30);
        ocupacion.setCapacidad2R(50);
        ocupacion.setOcupacion4R(60);
        ocupacion.setCapacidad4R(100);
        // Calcular y establecer los porcentajes manualmente para que coincidan con la lógica del servicio
        // 30/50 * 100 = 60.00%
        ocupacion.setPorcentajeOcupacion2R(BigDecimal.valueOf(60.00));
        ocupacion.setPorcentajeOcupacion4R(BigDecimal.valueOf(60.00));
        ocupacionSucursalRepository.save(ocupacion);

        // 2. Configuracion descuento
        ConfiguracionDescuentoPlan config = new ConfiguracionDescuentoPlan();
        config.setTipoPlan(testTipoPlan);
        config.setDescuentoMensual(new BigDecimal("15.00"));
        config.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        config.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        config.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        config.setCreadoPor(testUsuarioEmpresa);
        config.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(config);

        // 3. Crear transacciones para tener datos en facturación y cortes
        Ticket ticket = new Ticket();
        ticket.setFolioNumerico("FOL-WORKFLOW");
        ticket.setSucursal(testSucursal);
        ticket.setVehiculo(testVehiculo);
        ticket.setSuscripcion(testSuscripcion);
        ticket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        ticket.setFechaHoraEntrada(LocalDateTime.now().minusHours(2));
        ticket.setFechaHoraSalida(LocalDateTime.now());
        ticket.setDuracionMinutos(120);
        ticket.setCodigoQr("QR-WORKFLOW");
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticket = ticketRepository.save(ticket);

        TransaccionTicket transaccion = new TransaccionTicket();
        transaccion.setTicket(ticket);
        transaccion.setTipoCobro(TransaccionTicket.TipoCobro.TARIFA_NORMAL);
        transaccion.setHorasCobradas(new BigDecimal("2.0"));
        transaccion.setHorasGratisComercio(new BigDecimal("0.0"));
        transaccion.setTarifaAplicada(new BigDecimal("15.00"));
        transaccion.setSubtotal(new BigDecimal("30.00"));
        transaccion.setDescuento(new BigDecimal("0.00"));
        transaccion.setTotal(new BigDecimal("30.00"));
        transaccion.setMetodoPago("EFECTIVO");
        transaccion.setNumeroTransaccion("TXN-WORKFLOW");
        transaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccionTicketRepository.save(transaccion);

        // 4. Crear convenio para comercio afiliado
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(testComercio);
        convenio.setSucursal(testSucursal);
        convenio.setHorasGratisMaximo(new BigDecimal("5.00"));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        convenio.setTarifaPorHora(new BigDecimal("15.00"));
        convenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(1));
        convenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(11));
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(testUsuarioEmpresa);
        convenio.setFechaCreacion(LocalDateTime.now());
        convenio = convenioComercioSucursalRepository.save(convenio);

        // 5. Crear corte de caja
        CorteCaja corteCaja = new CorteCaja();
        corteCaja.setSucursal(testSucursal);
        corteCaja.setPeriodo(CorteCaja.Periodo.MENSUAL);
        corteCaja.setFechaInicio(LocalDateTime.now().minusMonths(1));
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

        // 6. Mock external services
        DetalleSucursalesIncidenciasDTO mockIncidencia = new DetalleSucursalesIncidenciasDTO();
        mockIncidencia.setIdSucursal(testSucursal.getIdSucursal());
        when(resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(List.of(mockIncidencia));

        DetalleEmpresaFlotillaDTO mockFlotilla = new DetalleEmpresaFlotillaDTO();
        when(gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(mockFlotilla);

        // Act - Test all report methods
        List<ReporteOcupacionDTO> ocupacionResult = reportesService.generarReporteOcupacionPorSucursal(testUsuarioEmpresa.getIdUsuario());
        List<ReportesFacturacionSucursalDTO> facturacionResult = reportesService.generarReporteFacturacionPorSucursal(testUsuarioEmpresa.getIdUsuario());
        List<ReporteSuscripcionesDTO> suscripcionesResult = reportesService.generarReporteSuscripciones(testUsuarioEmpresa.getIdUsuario());
        List<ReporteComercioAfiliadoDTO> comercioResult = reportesService.generarReporteComercioAfiliado(testUsuarioEmpresa.getIdUsuario());
        List<ReporteCorteDeCajaDTO> corteCajaResult = reportesService.generarReporteCortesDeCaja(testUsuarioEmpresa.getIdUsuario());
        ReportesDeIncidenciasDTO incidenciasResult = reportesService.generarReporteIncidencias(testUsuarioEmpresa.getIdUsuario());
        DetalleEmpresaFlotillaDTO flotillaResult = reportesService.generarReporteFlotasEmpresariales(testUsuarioEmpresa.getIdUsuario());

        // Assert - Verify all reports work
        assertNotNull(ocupacionResult);
        assertEquals(1, ocupacionResult.size());
        assertNotNull(ocupacionResult.get(0).getDetallesOcupacion());
        assertEquals("60.00%", ocupacionResult.get(0).getDetallesOcupacion().getPorcentajeOcupacion2R());

        assertNotNull(facturacionResult);
        assertEquals(1, facturacionResult.size());
        assertTrue(facturacionResult.get(0).getDetallesFacturacion().size() >= 1); // Al menos una transacción

        assertNotNull(suscripcionesResult);
        assertEquals(1, suscripcionesResult.size());
        assertNotNull(suscripcionesResult.get(0).getConfiguracionDescuento());

        assertNotNull(comercioResult);
        assertEquals(1, comercioResult.size());
        assertTrue(comercioResult.get(0).getDetallesConvenioComercio().size() >= 1); // Al menos un convenio

        assertNotNull(corteCajaResult);
        assertEquals(1, corteCajaResult.size());
        assertTrue(corteCajaResult.get(0).getCortesDeCajas().size() >= 1); // Al menos un corte
        assertTrue(corteCajaResult.get(0).getDetallesDeIngresosPorTarifasYExcedentes().size() >= 1); // Al menos una transacción

        assertNotNull(incidenciasResult);
        assertEquals(1, incidenciasResult.getDetalleSucursalesIncidenciasDTO().size());

        assertNotNull(flotillaResult);
        assertEquals(mockFlotilla, flotillaResult);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
