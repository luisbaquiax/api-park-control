package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.DetalleSolicitudesTemporalDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.SolicitarPermisoTemporalDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.PlanesSuscripcionDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudTemporalClienteServiceUnitTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private PermisoTemporalRepository permisoTemporalRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private SuscripcionClienteService suscripcionClienteService;

    @InjectMocks
    private SolicitudTemporalClienteService solicitudTemporalClienteService;

    private Suscripcion mockSuscripcion;
    private Vehiculo mockVehiculo;
    private PermisoTemporal mockPermisoTemporal;
    private Usuario mockCliente;
    private Empresa mockEmpresa;
    private TipoPlan mockTipoPlan;
    private TarifaBase mockTarifaBase;
    private Sucursal mockSucursal;

    @BeforeEach
    void setUp() {
        // Mock Cliente
        mockCliente = new Usuario();
        mockCliente.setIdUsuario(1L);
        mockCliente.setNombreUsuario("testclient");

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setNit("1234567-8");

        // Mock TipoPlan
        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setPrecioPlan(300.00);

        // Mock TarifaBase
        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));

        // Mock Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setDireccionCompleta("Test Address");

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setTarifaBaseReferencia(mockTarifaBase);
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        mockSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        mockSuscripcion.setDescuentoAplicado(new BigDecimal("10.00"));
        mockSuscripcion.setPrecioPlan(new BigDecimal("270.00"));
        mockSuscripcion.setHorasMensualesIncluidas(160);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("50.5"));
        mockSuscripcion.setFechaInicio(LocalDateTime.now().minusDays(30));
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusDays(330));
        mockSuscripcion.setFechaCompra(LocalDateTime.now().minusDays(30));

        // Mock Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("TMP789");
        mockVehiculo.setMarca("Honda");
        mockVehiculo.setModelo("Civic");
        mockVehiculo.setColor("Azul");
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);

        // Mock PermisoTemporal
        mockPermisoTemporal = new PermisoTemporal();
        mockPermisoTemporal.setId(1L);
        mockPermisoTemporal.setSuscripcion(mockSuscripcion);
        mockPermisoTemporal.setPlacaTemporal("TMP789");
        mockPermisoTemporal.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS);
        mockPermisoTemporal.setMotivo("Vehículo en reparación");
        mockPermisoTemporal.setUsosRealizados(0);
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.PENDIENTE);
    }

    @Test
    void testSolicitarPermisoTemporal_Success() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(1L);
        solicitudDTO.setPlacaTemporal("TMP789");
        solicitudDTO.setTipoVehiculoPermitido("CUATRO_RUEDAS");
        solicitudDTO.setMotivo("Vehículo en reparación");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findByPlaca("TMP789")).thenReturn(mockVehiculo);
        when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenAnswer(invocation -> {
            PermisoTemporal permiso = invocation.getArgument(0);
            permiso.setId(1L);
            return permiso;
        });

        // Act
        String result = solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

        // Assert
        assertEquals("Solicitud de permiso temporal creada exitosamente", result);
        verify(suscripcionRepository).findById(1L);
        verify(vehiculoRepository).findByPlaca("TMP789");
        verify(permisoTemporalRepository).save(any(PermisoTemporal.class));
    }

    @Test
    void testSolicitarPermisoTemporal_SuscripcionNotFound() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(999L);

        when(suscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("Suscripcion no encontrada", exception.getMessage());
        verify(suscripcionRepository).findById(999L);
    }

    @Test
    void testSolicitarPermisoTemporal_SuscripcionInactiva() {
        // Arrange
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
        
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(1L);

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("La suscripcion no esta activa", exception.getMessage());
    }

    @Test
    void testSolicitarPermisoTemporal_PlacaNoRegistrada() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(1L);
        solicitudDTO.setPlacaTemporal("NOEXISTE");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findByPlaca("NOEXISTE")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("La placa temporal no esta registrada en el sistema", exception.getMessage());
    }

    @Test
    void testSolicitarPermisoTemporal_AllTipoVehiculo() {
        // Test all vehicle types
        String[] tiposVehiculo = {"DOS_RUEDAS", "CUATRO_RUEDAS"};

        for (String tipoVehiculo : tiposVehiculo) {
            // Arrange
            SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
            solicitudDTO.setIdSuscripcion(1L);
            solicitudDTO.setPlacaTemporal("TMP789");
            solicitudDTO.setTipoVehiculoPermitido(tipoVehiculo);
            solicitudDTO.setMotivo("Test " + tipoVehiculo);

            when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
            when(vehiculoRepository.findByPlaca("TMP789")).thenReturn(mockVehiculo);
            when(permisoTemporalRepository.save(any(PermisoTemporal.class))).thenAnswer(invocation -> {
                PermisoTemporal permiso = invocation.getArgument(0);
                permiso.setId(1L);
                return permiso;
            });

            // Act
            String result = solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

            // Assert
            assertEquals("Solicitud de permiso temporal creada exitosamente", result);
        }
    }

    @Test
    void testObtenerDetallesPermisosTemporales_Success() {
        // Arrange
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockPermisoTemporal));
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        assertEquals(1L, detalleDTO.getIdPermisoTemporal());
        assertEquals("TMP789", detalleDTO.getPlacaTemporal());
        assertEquals("CUATRO_RUEDAS", detalleDTO.getTipoVehiculoPermitido());
        assertEquals("Vehículo en reparación", detalleDTO.getMotivo());
        assertEquals(0, detalleDTO.getUsosRealizados());
        assertEquals("PENDIENTE", detalleDTO.getEstado());

        // Verify suscripcion details
        assertNotNull(detalleDTO.getSuscripcionCliente());
        assertEquals(1L, detalleDTO.getSuscripcionCliente().getIdSuscripcion());
        assertEquals("MENSUAL", detalleDTO.getSuscripcionCliente().getPeriodoContratado());

        verify(suscripcionRepository).findByUsuario_IdUsuario(1L);
        verify(permisoTemporalRepository).findBySuscripcion_Id(1L);
        verify(suscripcionClienteService).obtenerEmpresaSuscripciones(mockEmpresa);
    }

    @Test
    void testObtenerDetallesPermisosTemporales_EmptyList() {
        // Arrange
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(suscripcionRepository).findByUsuario_IdUsuario(1L);
    }

    @Test
    void testObtenerDetallesPermisosTemporales_WithSucursalesValidas() {
        // Arrange
        mockPermisoTemporal.setSucursalesValidas("1,2,3");
        
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockPermisoTemporal));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(sucursalRepository.findById(2L)).thenReturn(Optional.empty()); // Test missing sucursal
        when(sucursalRepository.findById(3L)).thenReturn(Optional.of(createAdditionalSucursal()));
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        
        assertNotNull(detalleDTO.getSucursalesDisponiblesPermiso());
        assertEquals(2, detalleDTO.getSucursalesDisponiblesPermiso().size()); // Only 2 because one is missing
        
        // Verify first sucursal
        assertEquals(1L, detalleDTO.getSucursalesDisponiblesPermiso().get(0).getIdSucursal());
        assertEquals("Sucursal Test", detalleDTO.getSucursalesDisponiblesPermiso().get(0).getNombre());
        
        // Verify third sucursal
        assertEquals(3L, detalleDTO.getSucursalesDisponiblesPermiso().get(1).getIdSucursal());
        assertEquals("Sucursal Adicional", detalleDTO.getSucursalesDisponiblesPermiso().get(1).getNombre());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_WithFechas() {
        // Arrange
        mockPermisoTemporal.setFechaInicio(LocalDateTime.now().minusDays(1));
        mockPermisoTemporal.setFechaFin(LocalDateTime.now().plusDays(7));
        mockPermisoTemporal.setFechaAprobacion(LocalDateTime.now().minusDays(1));
        mockPermisoTemporal.setUsosMaximos(10);
        mockPermisoTemporal.setUsosRealizados(3);
        mockPermisoTemporal.setObservaciones("Permiso aprobado con condiciones");
        mockPermisoTemporal.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);

        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockPermisoTemporal));
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        
        assertNotNull(detalleDTO.getFechaInicio());
        assertNotNull(detalleDTO.getFechaFin());
        assertNotNull(detalleDTO.getFechaAprobacion());
        assertEquals(10, detalleDTO.getUsosMaximos());
        assertEquals(3, detalleDTO.getUsosRealizados());
        assertEquals("Permiso aprobado con condiciones", detalleDTO.getObservaciones());
        assertEquals("ACTIVO", detalleDTO.getEstado());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_WithNullFechas() {
        // Arrange - Test when dates are null
        mockPermisoTemporal.setFechaInicio(null);
        mockPermisoTemporal.setFechaFin(null);
        mockPermisoTemporal.setFechaAprobacion(null);

        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockPermisoTemporal));
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        
        assertNull(detalleDTO.getFechaInicio());
        assertNull(detalleDTO.getFechaFin());
        assertNull(detalleDTO.getFechaAprobacion());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_MultipleSuscripciones() {
        // Arrange - Multiple suscripciones with permisos
        Suscripcion suscripcion2 = new Suscripcion();
        suscripcion2.setId(2L);
        suscripcion2.setEmpresa(mockEmpresa);
        suscripcion2.setUsuario(mockCliente);
        suscripcion2.setTipoPlan(mockTipoPlan);
        suscripcion2.setTarifaBaseReferencia(mockTarifaBase);
        suscripcion2.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion2.setPeriodoContratado(Suscripcion.PeriodoContratado.ANUAL);
        suscripcion2.setDescuentoAplicado(new BigDecimal("15.00"));
        suscripcion2.setPrecioPlan(new BigDecimal("3000.00"));
        suscripcion2.setHorasMensualesIncluidas(200);
        suscripcion2.setHorasConsumidas(new BigDecimal("75.0"));
        suscripcion2.setFechaInicio(LocalDateTime.now().minusDays(60));
        suscripcion2.setFechaFin(LocalDateTime.now().plusDays(300));
        suscripcion2.setFechaCompra(LocalDateTime.now().minusDays(60));

        PermisoTemporal permiso2 = new PermisoTemporal();
        permiso2.setId(2L);
        permiso2.setSuscripcion(suscripcion2);
        permiso2.setPlacaTemporal("TMP456");
        permiso2.setTipoVehiculoPermitido(PermisoTemporal.TipoVehiculo.DOS_RUEDAS);
        permiso2.setMotivo("Moto de remplazo");
        permiso2.setUsosRealizados(1);
        permiso2.setEstado(PermisoTemporal.EstadoPermiso.ACTIVO);

        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion, suscripcion2));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList(mockPermisoTemporal));
        when(permisoTemporalRepository.findBySuscripcion_Id(2L)).thenReturn(Arrays.asList(permiso2));
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertEquals(2, result.size());
        
        // Verify first permiso
        DetalleSolicitudesTemporalDTO detalle1 = result.get(0);
        assertEquals("TMP789", detalle1.getPlacaTemporal());
        assertEquals("CUATRO_RUEDAS", detalle1.getTipoVehiculoPermitido());
        assertEquals("MENSUAL", detalle1.getSuscripcionCliente().getPeriodoContratado());
        
        // Verify second permiso
        DetalleSolicitudesTemporalDTO detalle2 = result.get(1);
        assertEquals("TMP456", detalle2.getPlacaTemporal());
        assertEquals("DOS_RUEDAS", detalle2.getTipoVehiculoPermitido());
        assertEquals("ANUAL", detalle2.getSuscripcionCliente().getPeriodoContratado());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_SinPermisosEnSuscripcion() {
        // Arrange - Suscripcion exists but no permisos
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList(mockSuscripcion));
        when(permisoTemporalRepository.findBySuscripcion_Id(1L)).thenReturn(Arrays.asList());

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(suscripcionRepository).findByUsuario_IdUsuario(1L);
        verify(permisoTemporalRepository).findBySuscripcion_Id(1L);
    }

    // Helper methods
    private PlanesSuscripcionDTO.EmpresaSuscripcionesDTO createMockEmpresaSuscripcionesDTO() {
        return PlanesSuscripcionDTO.EmpresaSuscripcionesDTO.builder()
                .idEmpresa(mockEmpresa.getIdEmpresa())
                .nombreComercial(mockEmpresa.getNombreComercial())
                .nit(mockEmpresa.getNit())
                .razonSocial("Test Company S.A.")
                .telefonoContacto("12345678")
                .direccionFiscal("Test Address")
                .sucursales(Arrays.asList())
                .suscripciones(Arrays.asList())
                .build();
    }

    private Sucursal createAdditionalSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setIdSucursal(3L);
        sucursal.setNombre("Sucursal Adicional");
        sucursal.setDireccionCompleta("Additional Address");
        return sucursal;
    }
}
