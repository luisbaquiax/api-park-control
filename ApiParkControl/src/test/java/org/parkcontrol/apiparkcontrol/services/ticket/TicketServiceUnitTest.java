package org.parkcontrol.apiparkcontrol.services.ticket;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.ticket.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.parkcontrol.apiparkcontrol.utils.GeneradorCodigo;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceUnitTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private PermisoTemporalRepository permisoTemporalRepository;
    @Mock
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;
    @Mock
    private TarifaBaseRepository tarifaBaseRepository;
    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Mock
    private HistorialConsumoSuscripcionRepository historialConsumoSuscripcionRepository;
    @Mock
    private TarifaBaseService tarifaBaseService;
    @Mock
    private TransaccionTicketRepository transaccionRepository;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private TicketService ticketService;

    private Usuario mockUsuario;
    private Usuario mockCliente;
    private Sucursal mockSucursal;
    private Vehiculo mockVehiculo;
    private Empresa mockEmpresa;
    private Persona mockPersona;
    private Rol mockRol;
    private Suscripcion mockSuscripcion;
    private TipoPlan mockTipoPlan;
    private PermisoTemporal mockPermisoTemporal;
    private Ticket mockTicket;

    @BeforeEach
    void setUp() {
        setupMockEntities();
    }

    private void setupMockEntities() {
        mockRol = new Rol();
        mockRol.setIdRol(1L);
        mockRol.setNombreRol("SUCURSAL");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setRol(mockRol);

        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");

        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setNombre("Test Sucursal");
        mockSucursal.setUsuarioSucursal(mockUsuario);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(100);

        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Pérez");

        mockCliente = new Usuario();
        mockCliente.setIdUsuario(2L);
        mockCliente.setPersona(mockPersona);

        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        mockVehiculo.setPropietario(mockPersona);

        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setDiasAplicables("L-M-X-J-V");
        mockTipoPlan.setCoberturaHoraria("08:00 - 18:00");

        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setHorasMensualesIncluidas(160);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("50.0"));
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        mockPermisoTemporal = new PermisoTemporal();
        mockPermisoTemporal.setId(1L);
        mockPermisoTemporal.setPlacaTemporal("ABC123");
        mockPermisoTemporal.setUsosMaximos(10);
        mockPermisoTemporal.setUsosRealizados(5);
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);
        mockPermisoTemporal.setSuscripcion(mockSuscripcion);

        mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setFolioNumerico("12345");
        mockTicket.setVehiculo(mockVehiculo);
        mockTicket.setSucursal(mockSucursal);
        mockTicket.setSuscripcion(mockSuscripcion);
        mockTicket.setFechaHoraEntrada(LocalDateTime.now().minusHours(2));
        mockTicket.setEstado(Ticket.EstadoTicket.ACTIVO);
        mockTicket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
    }

    @Test
    void testSave_Success_WithoutSuscripcion() throws IOException, WriterException {
        // Arrange
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockCliente);
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.DOS_RUEDAS))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.CUATRO_RUEDAS))
                .thenReturn(Collections.emptyList());
        // Este método devuelve Optional, no List
        when(permisoTemporalRepository.findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                anyString(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(suscripcionRepository.findByVehiculo_IdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.findByVehiculo_IdAndEstado(1L, Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

        // Act
        TicketResponseDTO result = ticketService.save(ticketRequest);

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testSave_Success_WithSuscripcion() throws IOException, WriterException {
        // Arrange
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SUSCRIPTOR", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockCliente);
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(any(), any())).thenReturn(Collections.emptyList());
        // Este método devuelve Optional, no List
        when(permisoTemporalRepository.findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                anyString(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(suscripcionRepository.findByVehiculo_IdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList(mockSuscripcion));
        when(ticketRepository.findByVehiculo_IdAndEstado(1L, Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Collections.emptyList());

        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setFolioNumerico("12345");
        savedTicket.setVehiculo(mockVehiculo);
        savedTicket.setSucursal(mockSucursal);
        savedTicket.setSuscripcion(mockSuscripcion);
        savedTicket.setTipoCliente(Ticket.TipoCliente.SUSCRIPTOR);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // Act & Assert
        assertDoesNotThrow(() -> ticketService.save(ticketRequest));
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testSave_UserNotFound() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                999L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.save(ticketRequest));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testSave_UnauthorizedRole() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        Rol clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        Usuario clienteUser = new Usuario();
        clienteUser.setRol(clienteRol);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(clienteUser));

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.save(ticketRequest));
        assertEquals(404, exception.getStatus());
        assertEquals("Rol de usuario no autorizado para generar tickets",
                exception.getMessage());
    }

    @Test
    void testSave_SucursalNotFound() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(null);

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.save(ticketRequest));
        assertEquals(404, exception.getStatus());
        assertEquals("Sucursal no asignado para el usuario", exception.getMessage());
    }

    @Test
    void testSave_VehiculoNotFound() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 999L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L))
                .thenReturn(mockSucursal);
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.save(ticketRequest));
        assertEquals(404, exception.getStatus());
        assertEquals("Vehiculo no encontrado", exception.getMessage());
    }

    @Test
    void testSave_CapacityExceeded_TwoWheels() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        Vehiculo dosRuedasVehiculo = new Vehiculo();
        dosRuedasVehiculo.setId(1L);
        dosRuedasVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        dosRuedasVehiculo.setPropietario(mockPersona);

        List<Ticket> ticketsActivos = Arrays.asList(new Ticket[50]);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L))
                .thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(dosRuedasVehiculo));
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(
                Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.DOS_RUEDAS))
                .thenReturn(ticketsActivos);

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> ticketService.save(ticketRequest));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("Capacidad máxima"));
        assertTrue(exception.getMessage().contains("dos ruedas"));
    }

    @Test
    void testSave_CapacityExceeded_FourWheels() {
        // Arrange
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        // Crear lista con 100 tickets inicializados correctamente
        List<Ticket> ticketsActivos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Ticket t = new Ticket();
            t.setId((long) i);
            t.setEstado(Ticket.EstadoTicket.ACTIVO);
            t.setFolioNumerico("FOLIO-" + i);
            // Asignar vehículo para evitar NullPointerException
            Vehiculo v = new Vehiculo();
            v.setId((long) i);
            v.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
            v.setPlaca("TEST" + i);
            t.setVehiculo(v);
            t.setSucursal(mockSucursal);
            t.setFechaHoraEntrada(LocalDateTime.now());
            t.setTipoCliente(Ticket.TipoCliente.SIN_SUSCRIPCION);
            ticketsActivos.add(t);
        }

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(
                Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.DOS_RUEDAS))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(
                Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.CUATRO_RUEDAS))
                .thenReturn(ticketsActivos);

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> ticketService.save(ticketRequest));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("Capacidad máxima"));
        assertTrue(exception.getMessage().contains("cuatro ruedas"));
    }

    @Test
    void testSave_ClientWithoutUserAccount() {
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L))
                .thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(null);
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(any(), any()))
                .thenReturn(Collections.emptyList());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.save(ticketRequest));
        assertEquals(404, exception.getStatus());
        assertEquals("El propietario del vehiculo no tiene una cuenta de usuario asociada",
                exception.getMessage());
    }

    @Test
    void testSave_VehicleWithActiveTicket() {
        // Arrange
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockCliente);
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(any(), any())).thenReturn(Collections.emptyList());
        // Este método devuelve Optional
        when(permisoTemporalRepository.findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                anyString(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(suscripcionRepository.findByVehiculo_IdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.findByVehiculo_IdAndEstado(1L, Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Arrays.asList(mockTicket));

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> ticketService.save(ticketRequest));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya tiene un ticket activo"));
    }
/*
    @Test
    void testCobrarTicket_Success_WithSuscripcion() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                1L, "", "", "12345", "EFECTIVO"
        );

        mockTicket.setFechaHoraEntrada(LocalDateTime.now().minusHours(2));
        mockTicket.setSuscripcion(mockSuscripcion);

        when(ticketRepository.findByFolioNumericoAndEstado("12345", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.of(mockTicket));
        when(acreditacionHorasComercioRepository.findByTicket_Id(1L)).thenReturn(null);
        when(tarifaBaseService.tarifaVigenteSucursal(mockSucursal)).thenReturn(new BigDecimal("10.00"));
        
        // Usar lenient() para los mocks que pueden o no ser usados dependiendo del flujo
        lenient().when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        lenient().when(historialConsumoSuscripcionRepository.save(any(HistorialConsumoSuscripcion.class)))
                .thenReturn(new HistorialConsumoSuscripcion());
        
        when(transaccionRepository.save(any(TransaccionTicket.class))).thenReturn(new TransaccionTicket());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

        // Act
        CobroResultadoDTO result = ticketService.cobrarTicket(checkRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTotalAPagar());
        verify(ticketRepository).save(any(Ticket.class));
        verify(transaccionRepository).save(any(TransaccionTicket.class));
        // No verificamos suscripcionRepository y historialConsumoSuscripcionRepository 
        // porque pueden o no llamarse dependiendo del flujo de negocio
    }
*/
    @Test
    void testCobrarTicket_Success_WithoutSuscripcion() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                1L, "", "", "12345", "EFECTIVO"
        );

        mockTicket.setFechaHoraEntrada(LocalDateTime.now().minusHours(1));
        mockTicket.setSuscripcion(null); // Sin suscripción
        mockTicket.setTipoCliente(Ticket.TipoCliente.SIN_SUSCRIPCION);

        when(ticketRepository.findByFolioNumericoAndEstado("12345", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.of(mockTicket));
        when(acreditacionHorasComercioRepository.findByTicket_Id(1L)).thenReturn(null);
        when(tarifaBaseService.tarifaVigenteSucursal(mockSucursal)).thenReturn(new BigDecimal("10.00"));
        when(transaccionRepository.save(any(TransaccionTicket.class))).thenReturn(new TransaccionTicket());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

        // Act
        CobroResultadoDTO result = ticketService.cobrarTicket(checkRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalAPagar().compareTo(BigDecimal.ZERO) > 0);
        verify(ticketRepository).save(any(Ticket.class));
        verify(transaccionRepository).save(any(TransaccionTicket.class));
    }

    @Test
    void testCobrarTicket_ByPlaca() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                1L, "ABC123", "", "", "EFECTIVO"
        );

        mockTicket.setSuscripcion(null); // Sin suscripción

        when(ticketRepository.findByVehiculo_PlacaAndEstado("ABC123", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.of(mockTicket));
        when(acreditacionHorasComercioRepository.findByTicket_Id(1L)).thenReturn(null);
        when(tarifaBaseService.tarifaVigenteSucursal(mockSucursal)).thenReturn(new BigDecimal("10.00"));
        when(transaccionRepository.save(any(TransaccionTicket.class))).thenReturn(new TransaccionTicket());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

        // Act
        CobroResultadoDTO result = ticketService.cobrarTicket(checkRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testCobrarTicket_ByCodigoQr() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                1L, "", "qr-code-123", "", "EFECTIVO"
        );

        mockTicket.setSuscripcion(null); // Sin suscripción

        // findByCodigoQrAndEstado devuelve Optional, no Ticket directamente
        when(ticketRepository.findByCodigoQrAndEstado("qr-code-123", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.of(mockTicket));
        when(acreditacionHorasComercioRepository.findByTicket_Id(1L)).thenReturn(null);
        when(tarifaBaseService.tarifaVigenteSucursal(mockSucursal)).thenReturn(new BigDecimal("10.00"));
        when(transaccionRepository.save(any(TransaccionTicket.class))).thenReturn(new TransaccionTicket());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);

        // Act
        CobroResultadoDTO result = ticketService.cobrarTicket(checkRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testCobrarTicket_UnauthorizedUser() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                999L, // idUsuario - Usuario diferente
                "", // placa
                "", // codigoQr
                "12345", // folio
                "EFECTIVO" // metodoPago
        );

        when(ticketRepository.findByFolioNumericoAndEstado("12345", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.of(mockTicket));

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> ticketService.cobrarTicket(checkRequest));
        assertEquals(403, exception.getStatus());
        assertTrue(exception.getMessage().contains("no está autorizado"));
    }

    @Test
    void testCobrarTicket_TicketNotFound() {
        // Arrange
        CheckTicketRequestDTO checkRequest = new CheckTicketRequestDTO(
                1L, "", "", "99999", "EFECTIVO"
        );

        when(ticketRepository.findByFolioNumericoAndEstado("99999", Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> ticketService.cobrarTicket(checkRequest));
        assertEquals(404, exception.getStatus());
        // El mensaje real del servicio cuando busca por folio
        assertEquals("Ticket no encontrado para el folio proporcionado", exception.getMessage());
    }

    @Test
    void testSave_WithPermisoTemporal() {
        // Arrange
        TicketRequestDTO ticketRequest = new TicketRequestDTO(
                1L, 1L, "SIN_SUSCRIPCION", LocalDateTime.now()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockCliente);
        when(ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(any(), any())).thenReturn(Collections.emptyList());
        // Este método devuelve Optional con el permiso temporal
        when(permisoTemporalRepository.findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                anyString(), any(), any(), any(), any())).thenReturn(Optional.of(mockPermisoTemporal));
        when(ticketRepository.findByVehiculo_IdAndEstado(1L, Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenReturn(mockPermisoTemporal);

        // Act & Assert
        assertDoesNotThrow(() -> ticketService.save(ticketRequest));
        verify(permisoTemporalRepository).save(any(PermisoTemporal.class));
        assertEquals(6, mockPermisoTemporal.getUsosRealizados());
    }

    @Test
    void testGetTicketsByCliente_Success() {
        Long idCliente = 2L;
        Rol clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        mockCliente.setRol(clienteRol);

        when(usuarioRepository.findById(idCliente))
                .thenReturn(Optional.of(mockCliente));
        when(ticketRepository.findByVehiculo_Propietario_IdPersonaAndEstado(
                1L, Ticket.EstadoTicket.ACTIVO))
                .thenReturn(Arrays.asList(mockTicket));

        List<TicketResponseDTO> result = ticketService.getTicketsByCliente(idCliente);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("12345", result.get(0).getFolioNumerico());
    }

    @Test
    void testGetTicketsByCliente_UserNotFound() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.getTicketsByCliente(999L));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testGetTicketsByCliente_NotClientRole() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> ticketService.getTicketsByCliente(1L));
        assertEquals(400, exception.getStatus());
        assertEquals("El usuario no es un cliente", exception.getMessage());
    }
}