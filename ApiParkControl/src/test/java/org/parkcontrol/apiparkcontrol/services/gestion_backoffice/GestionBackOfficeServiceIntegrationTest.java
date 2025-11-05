package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionBackOfficeServiceIntegrationTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BackofficeRepository backofficeRepository;
    @Mock
    private SolicitudCambioPlacaRepository solicitudCambioPlacaRepository;
    @Mock
    private PermisoTemporalRepository permisoTemporalRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private GestionCambioPlacaClienteService gestionCambioPlacaClienteService;
    @Mock
    private SolicitudTemporalClienteService solicitudTemporalClienteService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private GestionBackOfficeService gestionBackOfficeService;

    private Usuario mockUsuarioBackOffice;
    private Usuario mockCliente;
    private Empresa mockEmpresa;
    private Backoffice mockBackoffice;
    private SolicitudCambioPlaca mockSolicitudCambioPlaca;
    private PermisoTemporal mockPermisoTemporal;
    private Suscripcion mockSuscripcion;
    private Vehiculo mockVehiculo;
    private Sucursal mockSucursal;
    private Persona mockPersonaCliente;
    private Persona mockPersonaBackOffice;
    
    // Add missing fields
    private TipoPlan mockTipoPlan;
    private TarifaBase mockTarifaBase;
    private PermisoTemporal testPermisoTemporal;
    private SolicitudCambioPlaca testSolicitudCambioPlaca;
    private Suscripcion testSuscripcion;
    private Vehiculo testVehiculoActual;
    private Vehiculo testVehiculoNuevo;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private Usuario testUsuarioBackOffice;
    private Usuario testCliente;
    private TipoPlan testTipoPlan;
    private TarifaBase testTarifaBase;

    @BeforeEach
    void setUp() {
        // Mock Persona BackOffice
        mockPersonaBackOffice = new Persona();
        mockPersonaBackOffice.setIdPersona(1L);
        mockPersonaBackOffice.setNombre("Admin");
        mockPersonaBackOffice.setApellido("BackOffice");
        mockPersonaBackOffice.setCorreo("admin@backoffice.com");
        mockPersonaBackOffice.setTelefono("12345678");
        mockPersonaBackOffice.setDpi("1234567890123");
        mockPersonaBackOffice.setDireccionCompleta("Admin Address");

        // Mock Usuario BackOffice
        mockUsuarioBackOffice = new Usuario();
        mockUsuarioBackOffice.setIdUsuario(1L);
        mockUsuarioBackOffice.setPersona(mockPersonaBackOffice);
        mockUsuarioBackOffice.setNombreUsuario("adminbackoffice");

        // Mock Persona Cliente
        mockPersonaCliente = new Persona();
        mockPersonaCliente.setIdPersona(2L);
        mockPersonaCliente.setNombre("Juan");
        mockPersonaCliente.setApellido("Cliente");
        mockPersonaCliente.setCorreo("juan.cliente@test.com");
        mockPersonaCliente.setTelefono("87654321");
        mockPersonaCliente.setDpi("9876543210987");
        mockPersonaCliente.setDireccionCompleta("Cliente Address");

        // Mock Cliente
        mockCliente = new Usuario();
        mockCliente.setIdUsuario(2L);
        mockCliente.setPersona(mockPersonaCliente);
        mockCliente.setNombreUsuario("juancliente");

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setNit("1234567-8");

        // Mock Backoffice
        mockBackoffice = new Backoffice();
        mockBackoffice.setIdBackoffice(1L);
        mockBackoffice.setUsuario(mockUsuarioBackOffice);
        mockBackoffice.setEmpresa(mockEmpresa);

        // Mock Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setMarca("Toyota");
        mockVehiculo.setModelo("Corolla");
        mockVehiculo.setColor("Blanco");
        mockVehiculo.setPropietario(mockPersonaCliente);

        // Mock Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setEmpresa(mockEmpresa);

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

        // Mock TarifaBase
        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setEmpresa(mockEmpresa);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        mockTarifaBase.setMoneda("GTQ");
        mockTarifaBase.setFechaVigenciaInicio(LocalDate.now().minusDays(30));
        mockTarifaBase.setFechaVigenciaFin(LocalDate.now().plusDays(330));
        mockTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setTarifaBaseReferencia(mockTarifaBase);
        mockSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.ANUAL);
        mockSuscripcion.setDescuentoAplicado(new BigDecimal("15.00"));
        mockSuscripcion.setPrecioPlan(new BigDecimal("3000.00"));
        mockSuscripcion.setHorasMensualesIncluidas(200);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("30.0"));
        mockSuscripcion.setFechaInicio(LocalDateTime.now().minusDays(60));
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusDays(300));
        mockSuscripcion.setFechaCompra(LocalDateTime.now().minusDays(60));
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        mockSuscripcion.setMetodoPago("TRANSFERENCIA");
        mockSuscripcion.setNumeroTransaccion("TXN789012");

        // Mock SolicitudCambioPlaca
        mockSolicitudCambioPlaca = new SolicitudCambioPlaca();
        mockSolicitudCambioPlaca.setId(1L);
        mockSolicitudCambioPlaca.setSuscripcion(mockSuscripcion);
        mockSolicitudCambioPlaca.setVehiculoActual(mockVehiculo);
        mockSolicitudCambioPlaca.setPlacaNueva("XYZ789");
        mockSolicitudCambioPlaca.setMotivo(SolicitudCambioPlaca.Motivo.VENTA);
        mockSolicitudCambioPlaca.setDescripcionMotivo("Venta del vehículo por necesidades económicas");
        mockSolicitudCambioPlaca.setEstado(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE);
        mockSolicitudCambioPlaca.setFechaSolicitud(LocalDateTime.now());

        // Mock PermisoTemporal
        mockPermisoTemporal = new PermisoTemporal();
        mockPermisoTemporal.setId(1L);
        mockPermisoTemporal.setSuscripcion(mockSuscripcion);
        mockPermisoTemporal.setPlacaTemporal("TMP123");
        mockPermisoTemporal.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS);
        mockPermisoTemporal.setMotivo("Vehículo principal en taller mecánico");
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE);
        mockPermisoTemporal.setUsosRealizados(0);

        // Initialize test entities (these should reference the actual mocks)
        testUsuarioBackOffice = mockUsuarioBackOffice;
        testCliente = mockCliente;
        testEmpresa = mockEmpresa;
        testSucursal = mockSucursal;
        testSuscripcion = mockSuscripcion;
        testVehiculoActual = mockVehiculo;
        testTipoPlan = mockTipoPlan;
        testTarifaBase = mockTarifaBase;
        
        // Create testVehiculoNuevo
        testVehiculoNuevo = new Vehiculo();
        testVehiculoNuevo.setId(2L);
        testVehiculoNuevo.setPlaca("XYZ789");
        testVehiculoNuevo.setMarca("Honda");
        testVehiculoNuevo.setModelo("Civic");
        testVehiculoNuevo.setColor("Negro");
        testVehiculoNuevo.setPropietario(mockPersonaCliente);

        // Initialize test entities that will be created in tests
        testPermisoTemporal = null; // Will be created in individual tests
        testSolicitudCambioPlaca = null; // Will be created in individual tests
    }

    @Test
    void testObtenerTodasSolicitudesCambioPlaca_Success() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(backofficeRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockBackoffice));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockSolicitudCambioPlaca));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockCliente));
        when(gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(2L))
                .thenReturn(Arrays.asList(createMockDetalleSolicitudCambioPlaca()));

        // Act
        List<BackOfficeDetalleSolicitudes> result = gestionBackOfficeService.obtenerTodasSolicitudesCambioPlaca(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BackOfficeDetalleSolicitudes detalle = result.get(0);
        assertEquals(2L, detalle.getIdUsuario());
        assertEquals("Juan Cliente", detalle.getNombreCompleto());
        assertEquals("juan.cliente@test.com", detalle.getEmail());
        assertEquals("87654321", detalle.getTelefono());
        assertEquals("9876543210987", detalle.getCui());
        assertEquals("Cliente Address", detalle.getDireccion());
        assertNotNull(detalle.getDetalleSolicitudesCambioPlaca());
        assertEquals(1, detalle.getDetalleSolicitudesCambioPlaca().size());

        verify(usuarioRepository).findById(1L);
        verify(backofficeRepository).findByUsuario_IdUsuario(1L);
        verify(solicitudCambioPlacaRepository).findBySuscripcion_Empresa_IdEmpresa(1L);
        verify(gestionCambioPlacaClienteService).obtenerSolicitudesCambioPlacaCliente(2L);
    }

    @Test
    void testObtenerTodasSolicitudesCambioPlaca_UsuarioNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.obtenerTodasSolicitudesCambioPlaca(999L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testRevisarSolicitudCambioPlaca_Aprobada_Success() throws Exception {
        // Arrange
        ResolverSolicitudCambioDTO resolverDTO = new ResolverSolicitudCambioDTO();
        resolverDTO.setIdUsuarioBackoffice(1L);
        resolverDTO.setIdSolicitudCambio(1L);
        resolverDTO.setEstado("APROBADA");
        resolverDTO.setObservacionesRevision("Solicitud aprobada correctamente");

        Vehiculo vehiculoNuevo = new Vehiculo();
        vehiculoNuevo.setId(2L);
        vehiculoNuevo.setPlaca("XYZ789");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(solicitudCambioPlacaRepository.findById(1L)).thenReturn(Optional.of(mockSolicitudCambioPlaca));
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenReturn(mockSolicitudCambioPlaca);
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(vehiculoNuevo);
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);

        // Act
        String result = gestionBackOfficeService.revisarSolicitudCambioPlaca(resolverDTO);

        // Assert
        assertEquals("La solicitud de cambio de placa ha sido aprobada exitosamente.", result);
        verify(usuarioRepository).findById(1L);
        verify(solicitudCambioPlacaRepository).findById(1L);
        verify(solicitudCambioPlacaRepository).save(any(SolicitudCambioPlaca.class));
        verify(vehiculoRepository).findByPlaca("XYZ789");
        verify(suscripcionRepository).save(any(Suscripcion.class));
    }

    @Test
    void testRevisarSolicitudCambioPlaca_Rechazada_Success() throws Exception {
        // Arrange
        ResolverSolicitudCambioDTO resolverDTO = new ResolverSolicitudCambioDTO();
        resolverDTO.setIdUsuarioBackoffice(1L);
        resolverDTO.setIdSolicitudCambio(1L);
        resolverDTO.setEstado("RECHAZADA");
        resolverDTO.setObservacionesRevision("Documentación insuficiente");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(solicitudCambioPlacaRepository.findById(1L)).thenReturn(Optional.of(mockSolicitudCambioPlaca));
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenReturn(mockSolicitudCambioPlaca);

        // Act
        String result = gestionBackOfficeService.revisarSolicitudCambioPlaca(resolverDTO);

        // Assert
        assertEquals("La solicitud de cambio de placa ha sido rechazada exitosamente.", result);
        verify(usuarioRepository).findById(1L);
        verify(solicitudCambioPlacaRepository).findById(1L);
        verify(solicitudCambioPlacaRepository).save(any(SolicitudCambioPlaca.class));
        verify(vehiculoRepository, never()).findByPlaca(any());
        verify(suscripcionRepository, never()).save(any());
    }

    @Test
    void testRevisarSolicitudCambioPlaca_SolicitudYaRevisada() {
        // Arrange
        mockSolicitudCambioPlaca.setEstado(SolicitudCambioPlaca.EstadoSolicitud.APROBADA);
        
        ResolverSolicitudCambioDTO resolverDTO = new ResolverSolicitudCambioDTO();
        resolverDTO.setIdUsuarioBackoffice(1L);
        resolverDTO.setIdSolicitudCambio(1L);
        resolverDTO.setEstado("APROBADA");
        resolverDTO.setObservacionesRevision("Solicitud aprobada");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(solicitudCambioPlacaRepository.findById(1L)).thenReturn(Optional.of(mockSolicitudCambioPlaca));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.revisarSolicitudCambioPlaca(resolverDTO);
        });

        assertEquals("La solicitud de cambio de placa ya ha sido revisada", exception.getMessage());
    }

    @Test
    void testObtenerTodasSolicitudesPermisoTemporal_Success() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(backofficeRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockBackoffice));
        when(permisoTemporalRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList(mockPermisoTemporal));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockCliente));
        when(solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(2L))
                .thenReturn(Arrays.asList(createMockDetalleSolicitudTemporal()));

        // Act
        List<BackOfficeDetalleSolicitudesTemporalDTO> result = gestionBackOfficeService.obtenerTodasSolicitudesPermisoTemporal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BackOfficeDetalleSolicitudesTemporalDTO detalle = result.get(0);
        assertEquals(2L, detalle.getIdUsuario());
        assertEquals("Juan Cliente", detalle.getNombreCompleto());
        assertEquals("juan.cliente@test.com", detalle.getEmail());
        assertEquals("87654321", detalle.getTelefono());
        assertEquals("9876543210987", detalle.getCui());
        assertEquals("Cliente Address", detalle.getDireccion());
        assertNotNull(detalle.getDetalleSolicitudesTemporal());
        assertEquals(1, detalle.getDetalleSolicitudesTemporal().size());

        verify(usuarioRepository).findById(1L);
        verify(backofficeRepository).findByUsuario_IdUsuario(1L);
        verify(permisoTemporalRepository).findBySuscripcion_Empresa_IdEmpresa(1L);
        verify(solicitudTemporalClienteService).obtenerDetallesPermisosTemporales(2L);
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_Integration() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada - vehiculo en taller");
        resolverDTO.setFechaInicio("2024-07-01");
        resolverDTO.setFechaFin("2024-07-10");
        resolverDTO.setUsosMaximos(5);
        resolverDTO.setSucursalesAsignadas("1");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);
        doNothing().when(emailService).enviarEmailGenerico(anyString(), anyString(), anyString());

        // Act
        String result = gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);

        // Assert
        assertEquals("La solicitud de permiso temporal ha sido activo exitosamente.", result);
        verify(usuarioRepository).findById(1L);
        verify(permisoTemporalRepository).findById(1L);
        verify(permisoTemporalRepository).save(any(PermisoTemporal.class));
        verify(emailService).enviarEmailGenerico(anyString(), eq("Permiso Temporal Aprobado"), anyString());
    }

    @Test
    void testRechazarSolicitudPermisoTemporal_Integration() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Documentación incompleta");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);
        doNothing().when(emailService).enviarEmailGenerico(anyString(), anyString(), anyString());

        // Act
        String result = gestionBackOfficeService.rechazarSolicitudPermisoTemporal(resolverDTO);

        // Assert
        assertEquals("La solicitud de permiso temporal ha sido rechazada exitosamente.", result);
        verify(usuarioRepository).findById(1L);
        verify(permisoTemporalRepository).findById(1L);
        verify(permisoTemporalRepository).save(any(PermisoTemporal.class));
        verify(emailService).enviarEmailGenerico(anyString(), eq("Permiso Temporal Rechazado"), anyString());
    }

    @Test
    void testRevocarSolicitudPermisoTemporal_Integration() throws Exception {
        // Arrange
        PermisoTemporal permisoActivo = new PermisoTemporal();
        permisoActivo.setId(1L);
        permisoActivo.setSuscripcion(mockSuscripcion);
        permisoActivo.setPlacaTemporal("TMP123");
        permisoActivo.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS);
        permisoActivo.setMotivo("Vehículo en reparación");
        permisoActivo.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);
        permisoActivo.setUsosRealizados(2);
        permisoActivo.setUsosMaximos(5);

        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setObservaciones("Uso indebido del permiso - múltiples infracciones");

        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(permisoActivo));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(permisoActivo);
        doNothing().when(emailService).enviarEmailGenerico(anyString(), anyString(), anyString());

        // Act
        String result = gestionBackOfficeService.revocarSolicitudPermisoTemporal(resolverDTO);

        // Assert
        assertEquals("La solicitud de permiso temporal ha sido cancelada exitosamente.", result);
        verify(permisoTemporalRepository).findById(1L);
        verify(permisoTemporalRepository).save(any(PermisoTemporal.class));
        verify(emailService).enviarEmailGenerico(anyString(), eq("Permiso Temporal Cancelado"), anyString());
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_ValidacionesFechas_Integration() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada con fechas válidas");
        resolverDTO.setFechaInicio("2024-08-01");
        resolverDTO.setFechaFin("2024-07-25"); // Fecha fin anterior a inicio
        resolverDTO.setUsosMaximos(3);
        resolverDTO.setSucursalesAsignadas("");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);
        });

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getMessage());
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_SucursalNoExiste_Integration() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada");
        resolverDTO.setFechaInicio("2024-07-01");
        resolverDTO.setFechaFin("2024-07-10");
        resolverDTO.setUsosMaximos(5);
        resolverDTO.setSucursalesAsignadas("999"); // Sucursal inexistente

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(sucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);
        });

        assertTrue(exception.getMessage().contains("Sucursal con ID 999 no encontrada"));
    }

    @Test
    void testRevisarSolicitudCambioPlaca_SolicitudNotFound_Integration() {
        // Arrange
        ResolverSolicitudCambioDTO resolverDTO = new ResolverSolicitudCambioDTO();
        resolverDTO.setIdUsuarioBackoffice(1L);
        resolverDTO.setIdSolicitudCambio(999L); // Non-existent
        resolverDTO.setEstado("APROBADA");
        resolverDTO.setObservacionesRevision("Solicitud aprobada");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(solicitudCambioPlacaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.revisarSolicitudCambioPlaca(resolverDTO);
        });

        assertEquals("Solicitud de cambio de placa no encontrada", exception.getMessage());
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_PermisoNotFound_Integration() {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(999L); // Non-existent
        resolverDTO.setAprobadoPor(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);
        });

        assertEquals("Permiso temporal no encontrado", exception.getMessage());
    }

    @Test
    void testRevocarSolicitudPermisoTemporal_PermisoNoActivo_Integration() throws Exception {
        // Arrange
        PermisoTemporal permisoNoActivo = new PermisoTemporal();
        permisoNoActivo.setId(1L);
        permisoNoActivo.setSuscripcion(mockSuscripcion);
        permisoNoActivo.setPlacaTemporal("TMP123");
        permisoNoActivo.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS);
        permisoNoActivo.setMotivo("Vehículo en reparación");
        permisoNoActivo.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE); // Not active
        permisoNoActivo.setUsosRealizados(0);

        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setObservaciones("Intento de revocación");

        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(permisoNoActivo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.revocarSolicitudPermisoTemporal(resolverDTO);
        });

        assertEquals("El permiso temporal no está activo y no puede ser cancelado", exception.getMessage());
    }

    // Remove the problematic integration tests that were mixing mock and integration approaches
    // Keep only the unit test style tests with proper mocking

    // Helper methods
    private DetalleSolicitudesCambioPlacaDTO createMockDetalleSolicitudCambioPlaca() {
        DetalleSolicitudesCambioPlacaDTO dto = new DetalleSolicitudesCambioPlacaDTO();
        dto.setIdSolicitudCambio(1L);
        dto.setPlacaNueva("XYZ789");
        dto.setMotivo("VENTA");
        dto.setDescripcionMotivo("Venta del vehículo");
        dto.setEstado("PENDIENTE");
        return dto;
    }

    private DetalleSolicitudesTemporalDTO createMockDetalleSolicitudTemporal() {
        DetalleSolicitudesTemporalDTO dto = new DetalleSolicitudesTemporalDTO();
        dto.setIdPermisoTemporal(1L);
        dto.setPlacaTemporal("TMP123");
        dto.setTipoVehiculoPermitido("CUATRO_RUEDAS");
        dto.setMotivo("Vehículo en reparación");
        dto.setEstado("PENDIENTE");
        return dto;
    }
}