package org.parkcontrol.apiparkcontrol.services.liquidaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionLiquidacionServiceUnitTest {

    @Mock
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Mock
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private CorteCajaRepository corteCajaRepository;
    @Mock
    private LiquidacionComercioRepository liquidacionComercioRepository;
    @Mock
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private TransaccionTicketRepository transaccionTicketRepository;

    @InjectMocks
    private GestionLiquidacionService gestionLiquidacionService;

    private Usuario mockUsuarioEmpresa;
    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private CorteCaja mockCorteCaja;
    private ComercioAfiliado mockComercio;
    private ConvenioComercioSucursal mockConvenio;
    private LiquidacionComercio mockLiquidacion;
    private HistorialPagoSuscripcion mockHistorialPago;
    private Suscripcion mockSuscripcion;
    private TransaccionTicket mockTransaccion;
    private Ticket mockTicket;
    private Persona mockPersona;

    @BeforeEach
    void setUp() {
        // Mock Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Cliente");
        mockPersona.setCorreo("juan@test.com");

        // Mock Usuario Empresa
        mockUsuarioEmpresa = new Usuario();
        mockUsuarioEmpresa.setIdUsuario(1L);
        mockUsuarioEmpresa.setNombreUsuario("empresauser");
        mockUsuarioEmpresa.setPersona(mockPersona);

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setNit("1234567-8");
        mockEmpresa.setUsuarioEmpresa(mockUsuarioEmpresa);

        // Mock Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setEmpresa(mockEmpresa);

        // Mock ComercioAfiliado
        mockComercio = new ComercioAfiliado();
        mockComercio.setId(1L);
        mockComercio.setNombreComercial("Comercio Test");
        mockComercio.setRazonSocial("Comercio Test S.A.");
        mockComercio.setNit("9876543-2");

        // Mock ConvenioComercioSucursal
        mockConvenio = new ConvenioComercioSucursal();
        mockConvenio.setId(1L);
        mockConvenio.setComercioAfiliado(mockComercio);
        mockConvenio.setSucursal(mockSucursal);
        mockConvenio.setHorasGratisMaximo(new BigDecimal("10.00"));
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        mockConvenio.setTarifaPorHora(new BigDecimal("15.00"));
        mockConvenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusDays(30));

        // Mock CorteCaja
        mockCorteCaja = new CorteCaja();
        mockCorteCaja.setIdCorteCaja(1L);
        mockCorteCaja.setSucursal(mockSucursal);
        mockCorteCaja.setPeriodo(CorteCaja.Periodo.MENSUAL);
        mockCorteCaja.setFechaInicio(LocalDateTime.now().minusDays(30));
        mockCorteCaja.setFechaFin(LocalDateTime.now());
        mockCorteCaja.setTotalIngresosTarifas(new BigDecimal("1000.00"));
        mockCorteCaja.setTotalIngresosExcedentes(new BigDecimal("200.00"));
        mockCorteCaja.setTotalHorasComercio(new BigDecimal("50.00"));
        mockCorteCaja.setTotalLiquidacionComercios(new BigDecimal("750.00"));
        mockCorteCaja.setTotalNeto(new BigDecimal("1950.00"));
        mockCorteCaja.setGeneradoPor(mockUsuarioEmpresa);
        mockCorteCaja.setFechaGeneracion(LocalDateTime.now());
        mockCorteCaja.setEstado(CorteCaja.Estado.PRELIMINAR);

        // Mock LiquidacionComercio
        mockLiquidacion = new LiquidacionComercio();
        mockLiquidacion.setIdLiquidacion(1L);
        mockLiquidacion.setCorteCaja(mockCorteCaja);
        mockLiquidacion.setComercio(mockComercio);
        mockLiquidacion.setConvenio(mockConvenio);
        mockLiquidacion.setTotalHorasOtorgadas(new BigDecimal("10.00"));
        mockLiquidacion.setTarifaPorHora(new BigDecimal("15.00"));
        mockLiquidacion.setMontoTotal(new BigDecimal("150.00"));
        mockLiquidacion.setEstado(LiquidacionComercio.EstadoLiquidacion.PENDIENTE);

        // Mock Usuario Cliente
        Usuario mockCliente = new Usuario();
        mockCliente.setIdUsuario(2L);
        mockCliente.setPersona(mockPersona);

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusDays(30));

        // Mock HistorialPagoSuscripcion
        mockHistorialPago = new HistorialPagoSuscripcion();
        mockHistorialPago.setIdHistorialPagoSuscripcion(1L);
        mockHistorialPago.setSuscripcion(mockSuscripcion);
        mockHistorialPago.setFechaPago(LocalDateTime.now());
        mockHistorialPago.setMontoPagado(new BigDecimal("300.00"));
        mockHistorialPago.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.TARJETA_CREDITO);
        mockHistorialPago.setNumeroTransaccion("TXN123456");
        mockHistorialPago.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        mockHistorialPago.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.RENOVACION);

        // Mock Ticket
        mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setSuscripcion(mockSuscripcion);
        mockTicket.setSucursal(mockSucursal);

        // Mock TransaccionTicket
        mockTransaccion = new TransaccionTicket();
        mockTransaccion.setIdTransaccion(1L);
        mockTransaccion.setTicket(mockTicket);
        mockTransaccion.setTipoCobro(TransaccionTicket.TipoCobro.TARIFA_NORMAL);
        mockTransaccion.setHorasCobradas(new BigDecimal("2.5"));
        mockTransaccion.setHorasGratisComercio(new BigDecimal("1.0"));
        mockTransaccion.setTarifaAplicada(new BigDecimal("15.00"));
        mockTransaccion.setSubtotal(new BigDecimal("37.50"));
        mockTransaccion.setDescuento(new BigDecimal("5.00"));
        mockTransaccion.setTotal(new BigDecimal("32.50"));
        mockTransaccion.setMetodoPago("EFECTIVO");
        mockTransaccion.setNumeroTransaccion("PAY123456");
        mockTransaccion.setEstado(TransaccionTicket.Estado.PAGADO);
        mockTransaccion.setFechaTransaccion(LocalDateTime.now());
    }

    @Test
    void testObtenerDetallesLiquidacionesPorEmpresa_Success() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(corteCajaRepository.findBySucursal_IdSucursal(1L)).thenReturn(Arrays.asList(mockCorteCaja));
        when(liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(1L)).thenReturn(Arrays.asList(mockLiquidacion));

        // Act
        DetallesLiquidacionesDTO result = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCortesDeCaja());
        assertEquals(1, result.getCortesDeCaja().size());

        DetallesLiquidacionesDTO.CortesDeCajaDTO corte = result.getCortesDeCaja().get(0);
        assertEquals(1L, corte.getIdCorteCaja());
        assertEquals("Sucursal Test", corte.getSucursalNombre());
        assertEquals("MENSUAL", corte.getPeriodo());
        assertEquals("50.00", corte.getTotalHorasComercio());
        assertEquals("750.00", corte.getTotalLiquidacionComercios());
        assertEquals("1950.00", corte.getTotalNeto());
        assertEquals("empresauser", corte.getGeneradoPorNombreUsuario());
        assertEquals("PRELIMINAR", corte.getEstado());

        assertEquals(1, corte.getDetallesComercios().size());
        DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalle = corte.getDetallesComercios().get(0);
        assertEquals(1L, detalle.getIdLiquidacion());
        assertEquals("Comercio Test", detalle.getComercioNombre());
        assertEquals("10.00", detalle.getTotalHorasOtorgadas());
        assertEquals("15.00", detalle.getTarifaPorHora());
        assertEquals("150.00", detalle.getMontoTotal());
        assertEquals("PENDIENTE", detalle.getEstado());

        verify(usuarioRepository).findById(1L);
        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(corteCajaRepository).findBySucursal_IdSucursal(1L);
        verify(liquidacionComercioRepository).findByCorteCaja_IdCorteCaja(1L);
    }

    @Test
    void testObtenerDetallesLiquidacionesPorEmpresa_UsuarioNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(999L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testObtenerDetallesLiquidacionesPorEmpresa_EmpresaNotFound() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(1L);
        });

        assertEquals("La empresa no existe", exception.getMessage());
    }

    @Test
    void testObtenerDetallesPagosSuscripcion_Success() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(suscripcionRepository.getSuscripcionByEmpresa_IdEmpresaAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList(mockSuscripcion));
        when(historialPagoSuscripcionRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockHistorialPago));

        // Act
        List<DetallePagosSuscripcionDTO> result = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetallePagosSuscripcionDTO pago = result.get(0);
        assertEquals(1L, pago.getIdHistorialPago());
        assertEquals(1L, pago.getIdSuscripcion());
        assertEquals("Juan", pago.getNombreCliente());
        assertEquals("300.00", pago.getMontoPagado());
        assertEquals("TARJETA_CREDITO", pago.getMetodoPago());
        assertEquals("TXN123456", pago.getNumeroTransaccion());
        assertEquals("COMPLETADO", pago.getEstadoPago());
        assertEquals("RENOVACION", pago.getMotivoPago());

        verify(suscripcionRepository).getSuscripcionByEmpresa_IdEmpresaAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA);
        verify(historialPagoSuscripcionRepository).findBySuscripcion_Empresa_IdEmpresa(1L);
    }

    @Test
    void testObtenerDetallesPagosSuscripcion_WithExpiredSubscriptions() {
        // Arrange - Suscripción vencida
        mockSuscripcion.setFechaFin(LocalDateTime.now().minusDays(1));
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(suscripcionRepository.getSuscripcionByEmpresa_IdEmpresaAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList(mockSuscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        when(historialPagoSuscripcionRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockHistorialPago));

        // Act
        List<DetallePagosSuscripcionDTO> result = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(1L);

        // Assert
        assertNotNull(result);
        verify(suscripcionRepository).save(mockSuscripcion);
        assertEquals(Suscripcion.EstadoSuscripcion.VENCIDA, mockSuscripcion.getEstado());
    }

    @Test
    void testObtenerDetalleTransaccionesTicket_Success() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(transaccionTicketRepository.findByTicket_Sucursal_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockTransaccion));

        // Act
        List<DetalleTransaccionTicketDTO> result = gestionLiquidacionService.obtenerDetalleTransaccionesTicket(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleTransaccionTicketDTO transaccion = result.get(0);
        assertEquals(1L, transaccion.getIdTransaccion());
        assertEquals(1L, transaccion.getIdTicket());
        assertEquals("Juan", transaccion.getNombreCliente());
        assertEquals("TARIFA_NORMAL", transaccion.getTipoCobro());
        assertEquals("2.5", transaccion.getHorasCobradas());
        assertEquals("1.0", transaccion.getHorasGratisComercio());
        assertEquals("15.00", transaccion.getTarifaAplicada());
        assertEquals("37.50", transaccion.getSubtotal());
        assertEquals("5.00", transaccion.getDescuento());
        assertEquals("32.50", transaccion.getTotal());
        assertEquals("EFECTIVO", transaccion.getMetodoPago());
        assertEquals("PAY123456", transaccion.getNumeroTransaccion());
        assertEquals("PAGADO", transaccion.getEstado());

        verify(transaccionTicketRepository).findByTicket_Sucursal_Empresa_IdEmpresa(1L);
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_NoConvenios() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList());

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("No hay convenios activos para actualizar.", result);
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_ConConveniosMensual() {
        // Arrange
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusMonths(2)); // 2 months ago
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.MENSUAL))
                .thenReturn(null);
        when(corteCajaRepository.save(any(CorteCaja.class))).thenReturn(mockCorteCaja);
        when(liquidacionComercioRepository.save(any(LiquidacionComercio.class))).thenReturn(mockLiquidacion);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository).save(any(LiquidacionComercio.class));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_ConveniosDiarios() {
        // Arrange
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.DIARIO);
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusDays(2)); // 2 days ago
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.DIARIO))
                .thenReturn(null);
        when(corteCajaRepository.save(any(CorteCaja.class))).thenReturn(mockCorteCaja);
        when(liquidacionComercioRepository.save(any(LiquidacionComercio.class))).thenReturn(mockLiquidacion);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository).save(any(LiquidacionComercio.class));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_ConveniosSemanales() {
        // Arrange
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.SEMANAL);
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusWeeks(2)); // 2 weeks ago
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.SEMANAL))
                .thenReturn(null);
        when(corteCajaRepository.save(any(CorteCaja.class))).thenReturn(mockCorteCaja);
        when(liquidacionComercioRepository.save(any(LiquidacionComercio.class))).thenReturn(mockLiquidacion);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository).save(any(LiquidacionComercio.class));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_ConveniosAnuales() {
        // Arrange
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.ANUAL);
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusYears(2)); // 2 years ago
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.ANUAL))
                .thenReturn(null);
        when(corteCajaRepository.save(any(CorteCaja.class))).thenReturn(mockCorteCaja);
        when(liquidacionComercioRepository.save(any(LiquidacionComercio.class))).thenReturn(mockLiquidacion);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository).save(any(LiquidacionComercio.class));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_ConUltimoCorteCaja() {
        // Arrange
        CorteCaja ultimoCorte = new CorteCaja();
        ultimoCorte.setFechaFin(LocalDateTime.now().minusMonths(2)); // 2 months ago
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.MENSUAL))
                .thenReturn(ultimoCorte);
        when(corteCajaRepository.save(any(CorteCaja.class))).thenReturn(mockCorteCaja);
        when(liquidacionComercioRepository.save(any(LiquidacionComercio.class))).thenReturn(mockLiquidacion);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository).save(any(LiquidacionComercio.class));
    }

    @Test
    void testActualizarPeriodosCortesDeCaja_NoEsMomentoCorte() {
        // Arrange - Convenio muy reciente, no toca corte aún
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now().minusDays(5)); // 5 days ago for monthly
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(convenioComercioSucursalRepository.findBySucursal_Empresa_IdEmpresaAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList(mockConvenio));
        when(corteCajaRepository.findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(1L, CorteCaja.Periodo.MENSUAL))
                .thenReturn(null);

        // Act
        String result = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(1L);

        // Assert
        assertEquals("Periodos de cortes de caja actualizados correctamente.", result);
        verify(corteCajaRepository, never()).save(any(CorteCaja.class));
        verify(liquidacionComercioRepository, never()).save(any(LiquidacionComercio.class));
    }

    @Test
    void testObtenerDetallesPagosSuscripcion_ConLiquidacionConFechas() {
        // Arrange - Liquidación con fechas de facturación y pago
        mockLiquidacion.setFechaFacturacion(LocalDateTime.now().minusDays(5));
        mockLiquidacion.setFechaPago(LocalDateTime.now().minusDays(2));
        mockLiquidacion.setObservaciones("Pago procesado correctamente");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList(mockSucursal));
        when(corteCajaRepository.findBySucursal_IdSucursal(1L)).thenReturn(Arrays.asList(mockCorteCaja));
        when(liquidacionComercioRepository.findByCorteCaja_IdCorteCaja(1L)).thenReturn(Arrays.asList(mockLiquidacion));

        // Act
        DetallesLiquidacionesDTO result = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO detalle = 
                result.getCortesDeCaja().get(0).getDetallesComercios().get(0);
        assertNotNull(detalle.getFechaFacturacion());
        assertNotNull(detalle.getFechaPago());
        assertEquals("Pago procesado correctamente", detalle.getObservaciones());
    }

    @Test
    void testObtenerDetallesLiquidacionesPorEmpresa_EmptyResults() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(Arrays.asList());

        // Act
        DetallesLiquidacionesDTO result = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCortesDeCaja());
        assertTrue(result.getCortesDeCaja().isEmpty());
    }

    @Test
    void testObtenerDetallesPagosSuscripcion_EmptyResults() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(suscripcionRepository.getSuscripcionByEmpresa_IdEmpresaAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(historialPagoSuscripcionRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<DetallePagosSuscripcionDTO> result = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerDetalleTransaccionesTicket_EmptyResults() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(transaccionTicketRepository.findByTicket_Sucursal_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<DetalleTransaccionTicketDTO> result = gestionLiquidacionService.obtenerDetalleTransaccionesTicket(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
