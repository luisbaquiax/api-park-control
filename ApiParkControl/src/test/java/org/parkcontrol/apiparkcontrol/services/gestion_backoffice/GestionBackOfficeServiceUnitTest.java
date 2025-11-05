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
class GestionBackOfficeServiceUnitTest {

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

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        // Mock SolicitudCambioPlaca
        mockSolicitudCambioPlaca = new SolicitudCambioPlaca();
        mockSolicitudCambioPlaca.setId(1L);
        mockSolicitudCambioPlaca.setSuscripcion(mockSuscripcion);
        mockSolicitudCambioPlaca.setVehiculoActual(mockVehiculo);
        mockSolicitudCambioPlaca.setPlacaNueva("XYZ789");
        mockSolicitudCambioPlaca.setMotivo(SolicitudCambioPlaca.Motivo.VENTA);
        mockSolicitudCambioPlaca.setDescripcionMotivo("Venta del vehículo");
        mockSolicitudCambioPlaca.setEstado(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE);
        mockSolicitudCambioPlaca.setFechaSolicitud(LocalDateTime.now());

        // Mock PermisoTemporal
        mockPermisoTemporal = new PermisoTemporal();
        mockPermisoTemporal.setId(1L);
        mockPermisoTemporal.setSuscripcion(mockSuscripcion);
        mockPermisoTemporal.setPlacaTemporal("TMP123");
        mockPermisoTemporal.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS);
        mockPermisoTemporal.setMotivo("Vehículo en reparación");
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE);
        mockPermisoTemporal.setUsosRealizados(0);
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
    void testAprobarSolicitudPermisoTemporal_Success() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada");
        resolverDTO.setFechaInicio("2024-07-01");
        resolverDTO.setFechaFin("2024-07-10");
        resolverDTO.setUsosMaximos(10);
        resolverDTO.setSucursalesAsignadas("1,2");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(sucursalRepository.findById(2L)).thenReturn(Optional.of(createAdditionalSucursal()));
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
    void testAprobarSolicitudPermisoTemporal_FechaFinAnterior() {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setFechaInicio("2024-07-10");
        resolverDTO.setFechaFin("2024-07-01"); // Fecha fin anterior a inicio

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);
        });

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getMessage());
    }

    @Test
    void testRechazarSolicitudPermisoTemporal_Success() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Documentación insuficiente");

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
    void testRevocarSolicitudPermisoTemporal_Success() throws Exception {
        // Arrange
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);
        
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setObservaciones("Uso indebido del permiso");

        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);
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
    void testRevocarSolicitudPermisoTemporal_PermisoNoActivo() {
        // Arrange
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE);
        
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);

        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.revocarSolicitudPermisoTemporal(resolverDTO);
        });

        assertEquals("El permiso temporal no está activo y no puede ser cancelado", exception.getMessage());
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_EmailError() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada");
        resolverDTO.setFechaInicio("2024-07-01");
        resolverDTO.setFechaFin("2024-07-10");
        resolverDTO.setUsosMaximos(10);
        resolverDTO.setSucursalesAsignadas("1");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);
        doThrow(new RuntimeException("Email service error")).when(emailService)
                .enviarEmailGenerico(anyString(), anyString(), anyString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);
        });

        assertTrue(exception.getMessage().contains("Error al enviar correo de notificación"));
    }

    @Test
    void testObtenerTodasSolicitudesCambioPlaca_EmptyList() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(backofficeRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockBackoffice));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<BackOfficeDetalleSolicitudes> result = gestionBackOfficeService.obtenerTodasSolicitudesCambioPlaca(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerTodasSolicitudesPermisoTemporal_EmptyList() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(backofficeRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockBackoffice));
        when(permisoTemporalRepository.findBySuscripcion_Empresa_IdEmpresa(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<BackOfficeDetalleSolicitudesTemporalDTO> result = gestionBackOfficeService.obtenerTodasSolicitudesPermisoTemporal(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAprobarSolicitudPermisoTemporal_SinSucursales() throws Exception {
        // Arrange
        ResolverSolicitudTemporalDTO resolverDTO = new ResolverSolicitudTemporalDTO();
        resolverDTO.setIdSolicitudTemporal(1L);
        resolverDTO.setAprobadoPor(1L);
        resolverDTO.setObservaciones("Solicitud aprobada");
        resolverDTO.setFechaInicio("2024-07-01");
        resolverDTO.setFechaFin("2024-07-10");
        resolverDTO.setUsosMaximos(5);
        resolverDTO.setSucursalesAsignadas(""); // Empty sucursales

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioBackOffice));
        when(permisoTemporalRepository.findById(1L)).thenReturn(Optional.of(mockPermisoTemporal));
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);
        doNothing().when(emailService).enviarEmailGenerico(anyString(), anyString(), anyString());

        // Act
        String result = gestionBackOfficeService.aprobarSolicitudPermisoTemporal(resolverDTO);

        // Assert
        assertEquals("La solicitud de permiso temporal ha sido activo exitosamente.", result);
        verify(sucursalRepository, never()).findById(any());
    }

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

    private Sucursal createAdditionalSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setIdSucursal(2L);
        sucursal.setNombre("Sucursal 2");
        sucursal.setEmpresa(mockEmpresa);
        return sucursal;
    }
}
