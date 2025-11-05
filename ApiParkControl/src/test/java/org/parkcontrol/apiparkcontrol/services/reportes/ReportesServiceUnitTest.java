package org.parkcontrol.apiparkcontrol.services.reportes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.DetalleEmpresaFlotillaDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.DetalleSucursalesIncidenciasDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetalleTransaccionTicketDTO;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;
import org.parkcontrol.apiparkcontrol.dto.reportes.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.empresa_flotilla.GestionEmpresaFlotillaService;
import org.parkcontrol.apiparkcontrol.services.gestion_incidencias.ResolucionIncidenciasService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportesServiceUnitTest {

    @Mock
    private TipoPlanRepository tipoPlanRepository;
    @Mock
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private OcupacionSucursalRepository ocupacionSucursalRepository;
    @Mock
    private TransaccionTicketRepository transaccionTicketRepository;
    @Mock
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Mock
    private CorteCajaRepository corteCajaRepository;
    @Mock
    private LiquidacionComercioRepository liquidacionComercioRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;
    @Mock
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private ResolucionIncidenciasService resolucionIncidenciasService;
    @Mock
    private GestionEmpresaFlotillaService gestionEmpresaFlotillaService;

    @InjectMocks
    private ReportesService reportesService;

    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private Usuario mockUsuario;
    private Persona mockPersona;
    private TipoPlan mockTipoPlan;
    private Suscripcion mockSuscripcion;
    private ComercioAfiliado mockComercio;
    private ConvenioComercioSucursal mockConvenio;
    private OcupacionSucursal mockOcupacion;
    private TransaccionTicket mockTransaccion;
    private Ticket mockTicket;
    private Vehiculo mockVehiculo;
    private TarifaBase mockTarifaBase;

    @BeforeEach
    void setUp() {
        // Mock Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Cliente");
        mockPersona.setCorreo("juan@test.com");

        // Mock Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setPersona(mockPersona);
        mockUsuario.setNombreUsuario("testuser");

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setUsuarioEmpresa(mockUsuario);

        // Mock Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setDireccionCompleta("Test Address");
        mockSucursal.setCiudad("Test City");
        mockSucursal.setDepartamento("Test Dept");
        mockSucursal.setHoraApertura(LocalTime.of(8, 0));
        mockSucursal.setHoraCierre(LocalTime.of(18, 0));
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(100);

        // Mock OcupacionSucursal
        mockOcupacion = new OcupacionSucursal();
        mockOcupacion.setIdOcupacion(1L);
        mockOcupacion.setSucursal(mockSucursal);
        mockOcupacion.setFechaHora(LocalDateTime.now());
        mockOcupacion.setOcupacion2R(20);
        mockOcupacion.setCapacidad2R(50);
        mockOcupacion.setOcupacion4R(30);
        mockOcupacion.setCapacidad4R(100);

        // Mock TarifaBase
        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));

        // Mock TipoPlan
        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setEmpresa(mockEmpresa);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setCodigoPlan("WW-001");
        mockTipoPlan.setDescripcion("Plan Workweek");
        mockTipoPlan.setPrecioPlan(300.00);
        mockTipoPlan.setHorasDia(8);
        mockTipoPlan.setHorasMensuales(160);
        mockTipoPlan.setDiasAplicables("L-M-X-J-V");
        mockTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        mockTipoPlan.setOrdenBeneficio(2);
        mockTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        mockTipoPlan.setFechaCreacion(LocalDateTime.now());

        // Mock Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setPropietario(mockPersona);

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockUsuario);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setTarifaBaseReferencia(mockTarifaBase);
        mockSuscripcion.setHorasMensualesIncluidas(160);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("50.0"));
        mockSuscripcion.setFechaInicio(LocalDateTime.now().minusMonths(1));
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusMonths(11));
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        // Mock Ticket
        mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setSucursal(mockSucursal);
        mockTicket.setVehiculo(mockVehiculo);
        mockTicket.setSuscripcion(mockSuscripcion);
        mockTicket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        mockTicket.setEstado(Ticket.EstadoTicket.FINALIZADO);

        // Mock TransaccionTicket
        mockTransaccion = new TransaccionTicket();
        mockTransaccion.setIdTransaccion(1L);
        mockTransaccion.setTicket(mockTicket);
        mockTransaccion.setTipoCobro(TransaccionTicket.TipoCobro.TARIFA_NORMAL);
        mockTransaccion.setHorasCobradas(new BigDecimal("4.0"));
        mockTransaccion.setHorasGratisComercio(new BigDecimal("0.5"));
        mockTransaccion.setTarifaAplicada(new BigDecimal("15.00"));
        mockTransaccion.setSubtotal(new BigDecimal("60.00"));
        mockTransaccion.setDescuento(new BigDecimal("5.00"));
        mockTransaccion.setTotal(new BigDecimal("55.00"));
        mockTransaccion.setMetodoPago("EFECTIVO");
        mockTransaccion.setNumeroTransaccion("TXN123456");
        mockTransaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        mockTransaccion.setFechaTransaccion(LocalDateTime.now());

        // Mock ComercioAfiliado
        mockComercio = new ComercioAfiliado();
        mockComercio.setId(1L);
        mockComercio.setNombreComercial("Comercio Test");
        mockComercio.setRazonSocial("Comercio Test S.A.");
        mockComercio.setNit("9876543-2");
        mockComercio.setTipoComercio("Restaurante");
        mockComercio.setCorreoContacto("comercio@test.com");
        mockComercio.setEstado(ComercioAfiliado.Estado.ACTIVO);

        // Mock ConvenioComercioSucursal
        mockConvenio = new ConvenioComercioSucursal();
        mockConvenio.setId(1L);
        mockConvenio.setComercioAfiliado(mockComercio);
        mockConvenio.setSucursal(mockSucursal);
        mockConvenio.setHorasGratisMaximo(new BigDecimal("10.00"));
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        mockConvenio.setTarifaPorHora(new BigDecimal("15.00"));
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(1));
        mockConvenio.setFechaFinConvenio(LocalDateTime.now().plusMonths(11));
        mockConvenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        mockConvenio.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void testGenerarReporteOcupacionPorSucursal_Success() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(ocupacionSucursalRepository.findTopBySucursalOrderByFechaHoraDesc(mockSucursal))
                .thenReturn(mockOcupacion);

        // Act
        List<ReporteOcupacionDTO> result = reportesService.generarReporteOcupacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteOcupacionDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());
        assertEquals("Test Address", reporte.getDireccionCompleta());
        assertEquals("Test City", reporte.getCiudad());
        assertEquals("Test Dept", reporte.getDepartamento());
        assertEquals("08:00", reporte.getHoraApertura());
        assertEquals("18:00", reporte.getHoraCierre());
        assertEquals(50, reporte.getCapacidad2Ruedas());
        assertEquals(100, reporte.getCapacidad4Ruedas());

        ReporteOcupacionDTO.DetalleOcupacionDTO detalle = reporte.getDetallesOcupacion();
        assertNotNull(detalle);
        assertEquals(20, detalle.getOcupacion2R());
        assertEquals(50, detalle.getCapacidad2R());
        assertEquals(30, detalle.getOcupacion4R());
        assertEquals(100, detalle.getCapacidad4R());
        assertEquals("40.00%", detalle.getPorcentajeOcupacion2R());
        assertEquals("30.00%", detalle.getPorcentajeOcupacion4R());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(ocupacionSucursalRepository).findTopBySucursalOrderByFechaHoraDesc(mockSucursal);
    }

    @Test
    void testGenerarReporteOcupacionPorSucursal_SinOcupacion() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(ocupacionSucursalRepository.findTopBySucursalOrderByFechaHoraDesc(mockSucursal))
                .thenReturn(null);

        // Act
        List<ReporteOcupacionDTO> result = reportesService.generarReporteOcupacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteOcupacionDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdSucursal());
        assertNull(reporte.getDetallesOcupacion());
    }

    @Test
    void testGenerarReporteOcupacionPorSucursal_CapacidadCero() {
        // Arrange
        mockOcupacion.setCapacidad2R(0);
        mockOcupacion.setCapacidad4R(0);
        
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(ocupacionSucursalRepository.findTopBySucursalOrderByFechaHoraDesc(mockSucursal))
                .thenReturn(mockOcupacion);

        // Act
        List<ReporteOcupacionDTO> result = reportesService.generarReporteOcupacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteOcupacionDTO.DetalleOcupacionDTO detalle = result.get(0).getDetallesOcupacion();
        assertNotNull(detalle);
        assertEquals("0%", detalle.getPorcentajeOcupacion2R());
        assertEquals("0%", detalle.getPorcentajeOcupacion4R());
    }

    @Test
    void testGenerarReporteFacturacionPorSucursal_Success() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(transaccionTicketRepository.findByTicket_Sucursal_IdSucursal(1L))
                .thenReturn(Arrays.asList(mockTransaccion));

        // Act
        List<ReportesFacturacionSucursalDTO> result = reportesService.generarReporteFacturacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReportesFacturacionSucursalDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());

        assertEquals(1, reporte.getDetallesFacturacion().size());
        DetalleTransaccionTicketDTO detalle = reporte.getDetallesFacturacion().get(0);
        assertEquals(1L, detalle.getIdTransaccion());
        assertEquals(1L, detalle.getIdTicket());
        assertEquals("Juan", detalle.getNombreCliente());
        assertEquals("TARIFA_NORMAL", detalle.getTipoCobro());
        assertEquals("55.00", detalle.getTotal());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(transaccionTicketRepository).findByTicket_Sucursal_IdSucursal(1L);
    }

    @Test
    void testGenerarReporteFacturacionPorSucursal_ClienteOcasional() {
        // Arrange - Ticket sin suscripción
        mockTicket.setSuscripcion(null);
        
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(transaccionTicketRepository.findByTicket_Sucursal_IdSucursal(1L))
                .thenReturn(Arrays.asList(mockTransaccion));

        // Act
        List<ReportesFacturacionSucursalDTO> result = reportesService.generarReporteFacturacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleTransaccionTicketDTO detalle = result.get(0).getDetallesFacturacion().get(0);
        assertEquals("Cliente ocasional", detalle.getNombreCliente());
    }

    @Test
    void testGenerarReporteSuscripciones_Success() {
        // Arrange
        ConfiguracionDescuentoPlan mockConfig = new ConfiguracionDescuentoPlan();
        mockConfig.setId(1L);
        mockConfig.setDescuentoMensual(new BigDecimal("15.00"));
        mockConfig.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        mockConfig.setFechaCreacion(LocalDateTime.now());
        mockConfig.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        mockConfig.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        mockConfig.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(mockConfig);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(mockTransaccion);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteSuscripcionesDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdTipoPlan());
        assertEquals("WORKWEEK", reporte.getNombrePlan());
        assertEquals("WW-001", reporte.getCodigoPlan());
        assertEquals("Plan Workweek", reporte.getDescripcion());
        assertEquals(300.00, reporte.getPrecioPlan());
        assertEquals(160, reporte.getHorasMensuales());

        assertNotNull(reporte.getConfiguracionDescuento());
        assertEquals(15.00, reporte.getConfiguracionDescuento().getDescuentoMensual());
        assertEquals(5.00, reporte.getConfiguracionDescuento().getDescuentoAnualAdicional());

        assertEquals(1, reporte.getDetallesSuscriptores().size());
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = reporte.getDetallesSuscriptores().get(0);
        assertEquals(1L, suscriptor.getIdSuscripcion());
        assertEquals("Juan", suscriptor.getNombreSuscriptor());
        assertEquals("ABC123", suscriptor.getPlacaVehiculo());
        assertEquals(160, suscriptor.getHorasMensualesIncluidas());
        assertEquals(50, suscriptor.getHorasUtilizadasMes());
        assertEquals("ACTIVA", suscriptor.getEstadoSuscripcion());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(tipoPlanRepository).findByEmpresa_IdEmpresa(1L);
        verify(suscripcionRepository).findByTipoPlan_Id(1L);
    }

    @Test
    void testGenerarReporteSuscripciones_SinConfiguracion() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(null);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(mockTransaccion);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteSuscripcionesDTO reporte = result.get(0);
        assertNull(reporte.getConfiguracionDescuento());
    }

    @Test
    void testGenerarReporteComercioAfiliado_Success() {
        // Arrange
        LiquidacionComercio mockLiquidacion = new LiquidacionComercio();
        mockLiquidacion.setIdLiquidacion(1L);
        mockLiquidacion.setComercio(mockComercio);
        mockLiquidacion.setTotalHorasOtorgadas(new BigDecimal("10.00"));
        mockLiquidacion.setTarifaPorHora(new BigDecimal("15.00"));
        mockLiquidacion.setMontoTotal(new BigDecimal("150.00"));
        mockLiquidacion.setEstado(LiquidacionComercio.EstadoLiquidacion.PENDIENTE);
        mockLiquidacion.setObservaciones("Test liquidación");

        AcreditacionHorasComercio mockAcreditacion = new AcreditacionHorasComercio();
        mockAcreditacion.setIdAcreditacion(1L);
        mockAcreditacion.setTicket(mockTicket);
        mockAcreditacion.setConvenio(mockConvenio);
        mockAcreditacion.setHorasOtorgadas(new BigDecimal("2.0"));
        mockAcreditacion.setFechaAcreditacion(LocalDateTime.now());

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockConvenio));
        when(convenioComercioSucursalRepository.findByComercioAfiliado_IdAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(liquidacionComercioRepository.findByComercio_Id(1L))
                .thenReturn(Arrays.asList(mockLiquidacion));
        when(acreditacionHorasComercioRepository.findByConvenio_ComercioAfiliado_Id(1L))
                .thenReturn(Arrays.asList(mockAcreditacion));

        // Act
        List<ReporteComercioAfiliadoDTO> result = reportesService.generarReporteComercioAfiliado(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteComercioAfiliadoDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdComercioAfiliado());
        assertEquals("Comercio Test", reporte.getNombreComercial());
        assertEquals("Comercio Test S.A.", reporte.getRazonSocial());
        assertEquals("9876543-2", reporte.getNit());
        assertEquals("Restaurante", reporte.getTipoComercio());
        assertEquals("comercio@test.com", reporte.getCorreoContacto());
        assertEquals("ACTIVO", reporte.getEstado());

        assertEquals(1, reporte.getDetallesConvenioComercio().size());
        ReporteComercioAfiliadoDTO.DetalleConvenioComercioDTO convenioDetalle = 
                reporte.getDetallesConvenioComercio().get(0);
        assertEquals(1L, convenioDetalle.getIdConvenioComercio());
        assertEquals("Sucursal Test", convenioDetalle.getNombreSucursal());
        assertEquals("10.00", convenioDetalle.getHorasGratisMaximo());

        assertEquals(1, reporte.getDetallesCorteCaja().size());
        assertEquals(1, reporte.getClientesBeneficiados().size());

        ReporteComercioAfiliadoDTO.ClientesBeneficiadosDTO cliente = 
                reporte.getClientesBeneficiados().get(0);
        assertEquals(1L, cliente.getIdCliente());
        assertEquals("Juan", cliente.getNombreCliente());
        assertEquals("2.0", cliente.getHorasGratisOtorgadas());
        assertEquals("Sucursal Test", cliente.getSucursalNombre());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(convenioComercioSucursalRepository).findBySucursal_Empresa_IdEmpresa(1L);
        verify(liquidacionComercioRepository).findByComercio_Id(1L);
        verify(acreditacionHorasComercioRepository).findByConvenio_ComercioAfiliado_Id(1L);
    }

    @Test
    void testGenerarReporteCortesDeCaja_Success() {
        // Arrange
        CorteCaja mockCorte = new CorteCaja();
        mockCorte.setIdCorteCaja(1L);
        mockCorte.setSucursal(mockSucursal);
        mockCorte.setPeriodo(CorteCaja.Periodo.MENSUAL);
        mockCorte.setFechaInicio(LocalDateTime.now().minusMonths(1));
        mockCorte.setFechaFin(LocalDateTime.now());
        mockCorte.setTotalHorasComercio(new BigDecimal("100.00"));
        mockCorte.setTotalLiquidacionComercios(new BigDecimal("1500.00"));
        mockCorte.setTotalNeto(new BigDecimal("3000.00"));
        mockCorte.setGeneradoPor(mockUsuario);
        mockCorte.setFechaGeneracion(LocalDateTime.now());
        mockCorte.setEstado(CorteCaja.Estado.PRELIMINAR);

        LiquidacionComercio mockLiquidacion = new LiquidacionComercio();
        mockLiquidacion.setIdLiquidacion(1L);
        mockLiquidacion.setComercio(mockComercio);
        mockLiquidacion.setTotalHorasOtorgadas(new BigDecimal("10.00"));
        mockLiquidacion.setTarifaPorHora(new BigDecimal("15.00"));
        mockLiquidacion.setMontoTotal(new BigDecimal("150.00"));
        mockLiquidacion.setEstado(LiquidacionComercio.EstadoLiquidacion.PENDIENTE);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(corteCajaRepository.findBySucursal_IdSucursal(1L)).thenReturn(Arrays.asList(mockCorte));
        when(liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(1L))
                .thenReturn(Arrays.asList(mockLiquidacion));
        when(transaccionTicketRepository.findByTicket_Sucursal_IdSucursal(1L))
                .thenReturn(Arrays.asList(mockTransaccion));

        // Act
        List<ReporteCorteDeCajaDTO> result = reportesService.generarReporteCortesDeCaja(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ReporteCorteDeCajaDTO reporte = result.get(0);
        assertEquals(1L, reporte.getIdSucursal());
        assertEquals("Sucursal Test", reporte.getNombreSucursal());

        assertEquals(1, reporte.getCortesDeCajas().size());
        assertEquals(1, reporte.getDetallesDeIngresosPorTarifasYExcedentes().size());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(corteCajaRepository).findBySucursal_IdSucursal(1L);
        verify(liquidacionComercioRepository).findByCorteCaja_IdCorteCaja(1L);
        verify(transaccionTicketRepository).findByTicket_Sucursal_IdSucursal(1L);
    }

    @Test
    void testGenerarReporteIncidencias_Success() throws Exception {
        // Arrange
        DetalleSucursalesIncidenciasDTO mockIncidencia = new DetalleSucursalesIncidenciasDTO();
        mockIncidencia.setIdSucursal(1L);
        mockIncidencia.setNombreSucursal("Sucursal Test");

        when(resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(1L))
                .thenReturn(Arrays.asList(mockIncidencia));

        // Act
        ReportesDeIncidenciasDTO result = reportesService.generarReporteIncidencias(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getDetalleSucursalesIncidenciasDTO());
        assertEquals(1, result.getDetalleSucursalesIncidenciasDTO().size());
        assertEquals(1L, result.getDetalleSucursalesIncidenciasDTO().get(0).getIdSucursal());

        verify(resolucionIncidenciasService).obtenerDetalleIncidenciasPorEmpresa(1L);
    }

    @Test
    void testGenerarReporteFlotasEmpresariales_Success() throws Exception {
        // Arrange
        DetalleEmpresaFlotillaDTO mockFlotilla = new DetalleEmpresaFlotillaDTO();

        when(gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(1L))
                .thenReturn(mockFlotilla);

        // Act
        DetalleEmpresaFlotillaDTO result = reportesService.generarReporteFlotasEmpresariales(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockFlotilla, result);

        verify(gestionEmpresaFlotillaService).obtenerDetalleEmpresasFlotilla(1L);
    }

    @Test
    void testCalcularHorasUtilizadasTotales_ConTransacciones() {
        // Este método es privado, se prueba indirectamente a través de generarReporteSuscripciones
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(null);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(mockTransaccion);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = result.get(0).getDetallesSuscriptores().get(0);
        assertEquals(4, suscriptor.getTotalHorasUtilizadas()); // 4 horas cobradas del mock

        // Verificar que se llamó dos veces: una para horas utilizadas y otra para excedentes
        verify(ticketRepository, times(2)).findBySuscripcion_Id(1L);
        verify(transaccionTicketRepository, times(2)).findByTicket_Id(1L);
    }

    @Test
    void testCalcularExcedentes_ConExcedentes() {
        // Arrange - Transacción con excedente
        mockTransaccion.setTipoCobro(TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION);
        mockTransaccion.setTotal(new BigDecimal("25.50"));

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(null);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(mockTransaccion);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = result.get(0).getDetallesSuscriptores().get(0);
        assertEquals(25.50, suscriptor.getTotalExcedentePagado()); // Total del excedente
    }

    @Test
    void testCalcularExcedentes_SinExcedentes() {
        // Arrange - Transacción normal sin excedente
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(null);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(mockTransaccion);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = result.get(0).getDetallesSuscriptores().get(0);
        assertEquals(0.0, suscriptor.getTotalExcedentePagado()); // No hay excedentes
    }

    @Test
    void testGenerarReporteSuscripciones_SinTransacciones() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresa(1L)).thenReturn(Arrays.asList(mockTipoPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_Id(1L)).thenReturn(null);
        when(suscripcionRepository.findByTipoPlan_Id(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockTicket));
        when(transaccionTicketRepository.findByTicket_Id(1L)).thenReturn(null);

        // Act
        List<ReporteSuscripcionesDTO> result = reportesService.generarReporteSuscripciones(1L);

        // Assert
        assertNotNull(result);
        ReporteSuscripcionesDTO.DetalleSuscriptoresDTO suscriptor = result.get(0).getDetallesSuscriptores().get(0);
        assertEquals(0, suscriptor.getTotalHorasUtilizadas());
        assertEquals(0.0, suscriptor.getTotalExcedentePagado());
    }

    @Test
    void testGenerarReporteOcupacionPorSucursal_MultipleSucursales() {
        // Arrange
        Sucursal sucursal2 = new Sucursal();
        sucursal2.setIdSucursal(2L);
        sucursal2.setNombre("Sucursal 2");
        sucursal2.setDireccionCompleta("Address 2");
        sucursal2.setCiudad("City 2");
        sucursal2.setDepartamento("Dept 2");
        sucursal2.setHoraApertura(LocalTime.of(9, 0));
        sucursal2.setHoraCierre(LocalTime.of(17, 0));
        sucursal2.setCapacidad2Ruedas(30);
        sucursal2.setCapacidad4Ruedas(60);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal, sucursal2));
        when(ocupacionSucursalRepository.findTopBySucursalOrderByFechaHoraDesc(mockSucursal))
                .thenReturn(mockOcupacion);
        when(ocupacionSucursalRepository.findTopBySucursalOrderByFechaHoraDesc(sucursal2))
                .thenReturn(null);

        // Act
        List<ReporteOcupacionDTO> result = reportesService.generarReporteOcupacionPorSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Primera sucursal con ocupación
        assertEquals(1L, result.get(0).getIdSucursal());
        assertNotNull(result.get(0).getDetallesOcupacion());
        
        // Segunda sucursal sin ocupación
        assertEquals(2L, result.get(1).getIdSucursal());
        assertNull(result.get(1).getDetallesOcupacion());
    }
}
