package org.parkcontrol.apiparkcontrol.services.empresa_flotilla;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.*;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.VehiculoClienteDTO;
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
class GestionEmpresaFlotillaServiceUnitTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private EmpresaFlotillaRepository empresaFlotillaRepository;
    @Mock
    private TipoPlanRepository tipoPlanRepository;
    @Mock
    private PlanCorporativoRepository planCorporativoRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private SuscripcionFlotillaRepository suscripcionFlotillaRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;

    @InjectMocks
    private GestionEmpresaFlotillaService gestionEmpresaFlotillaService;

    private EmpresaFlotilla mockEmpresaFlotilla;
    private TipoPlan mockTipoPlan;
    private Usuario mockUsuario;
    private PlanCorporativo mockPlanCorporativo;
    private Vehiculo mockVehiculo;
    private SuscripcionFlotilla mockSuscripcionFlotilla;
    private Suscripcion mockSuscripcion;
    private Empresa mockEmpresa;
    private Persona mockPersona;

    @BeforeEach
    void setUp() {
        // Mock Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Test");
        mockPersona.setApellido("User");
        mockPersona.setCorreo("test@example.com");

        // Mock Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("testuser");
        mockUsuario.setPersona(mockPersona);

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setUsuarioEmpresa(mockUsuario);

        // Mock EmpresaFlotilla
        mockEmpresaFlotilla = new EmpresaFlotilla();
        mockEmpresaFlotilla.setIdEmpresaFlotilla(1L);
        mockEmpresaFlotilla.setNombreEmpresa("Test Fleet Company");
        mockEmpresaFlotilla.setRazonSocial("Test Fleet Company S.A.");
        mockEmpresaFlotilla.setNit("1234567-8");
        mockEmpresaFlotilla.setTelefono("12345678");
        mockEmpresaFlotilla.setCorreoContacto("fleet@test.com");
        mockEmpresaFlotilla.setDireccion("Test Address");
        mockEmpresaFlotilla.setEstado(EmpresaFlotilla.EstadoEmpresaFlotilla.ACTIVA);
        mockEmpresaFlotilla.setFechaRegistro(LocalDateTime.now());

        // Mock TipoPlan
        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setEmpresa(mockEmpresa);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setCodigoPlan("WW-001");

        // Mock PlanCorporativo
        mockPlanCorporativo = new PlanCorporativo();
        mockPlanCorporativo.setIdPlanCorporativo(1L);
        mockPlanCorporativo.setEmpresaFlotilla(mockEmpresaFlotilla);
        mockPlanCorporativo.setTipoPlan(mockTipoPlan);
        mockPlanCorporativo.setNombrePlanCorporativo("Corporate Plan Test");
        mockPlanCorporativo.setNumeroPlacasContratadas(50);
        mockPlanCorporativo.setDescuentoCorporativoAdicional(new BigDecimal("15.00"));
        mockPlanCorporativo.setPrecioPlanCorporativo(new BigDecimal("15000.00"));
        mockPlanCorporativo.setFechaInicio(LocalDateTime.now().minusDays(30));
        mockPlanCorporativo.setFechaFin(LocalDateTime.now().plusDays(330));
        mockPlanCorporativo.setCreadoPor(mockUsuario);
        mockPlanCorporativo.setFechaCreacion(LocalDateTime.now());
        mockPlanCorporativo.setEstado(PlanCorporativo.EstadoPlanCorporativo.ACTIVO);

        // Mock Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setMarca("Toyota");
        mockVehiculo.setModelo("Corolla");
        mockVehiculo.setColor("Blanco");
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        mockVehiculo.setPropietario(mockPersona);

        // Mock SuscripcionFlotilla
        mockSuscripcionFlotilla = new SuscripcionFlotilla();
        mockSuscripcionFlotilla.setIdSuscripcionFlotilla(1L);
        mockSuscripcionFlotilla.setPlanCorporativo(mockPlanCorporativo);
        mockSuscripcionFlotilla.setVehiculo(mockVehiculo);
        mockSuscripcionFlotilla.setFechaAsignacion(LocalDateTime.now());
        mockSuscripcionFlotilla.setEstado(SuscripcionFlotilla.EstadoSuscripcion.ACTIVA);

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockUsuario);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
    }

    @Test
    void testNuevaEmpresaFlotilla_Success() {
        // Arrange
        NuevaEmpresaFlotillaDTO dto = new NuevaEmpresaFlotillaDTO();
        dto.setNombreEmpresa("Test Fleet Company");
        dto.setRazonSocial("Test Fleet Company S.A.");
        dto.setNit("1234567-8");
        dto.setTelefono("12345678");
        dto.setCorreoContacto("fleet@test.com");
        dto.setDireccion("Test Address");

        when(empresaFlotillaRepository.save(any(EmpresaFlotilla.class))).thenReturn(mockEmpresaFlotilla);

        // Act
        String result = gestionEmpresaFlotillaService.nuevaEmpresaFlotilla(dto);

        // Assert
        assertEquals("Nueva empresa de flotilla creada con éxito.", result);
        verify(empresaFlotillaRepository).save(any(EmpresaFlotilla.class));
    }

    @Test
    void testNuevoPlanCorporativo_Success() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(1L);
        dto.setIdTipoPlan(1L);
        dto.setNombrePlanCorporativo("Corporate Plan Test");
        dto.setNumeroPlacasContratadas(50);
        dto.setDescuentoCorporativoAdicional(15.00);
        dto.setPrecioPlanCorporativo(15000.00);
        dto.setFechaInicio("2024-01-01");
        dto.setFechaFin("2024-12-31");
        dto.setIdCreadoPor(1L);

        when(empresaFlotillaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresaFlotilla));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(1L, PlanCorporativo.EstadoPlanCorporativo.ACTIVO))
                .thenReturn(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(planCorporativoRepository.save(any(PlanCorporativo.class))).thenReturn(mockPlanCorporativo);

        // Act
        String result = gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);

        // Assert
        assertEquals("Nuevo plan corporativo creado con éxito.", result);
        verify(empresaFlotillaRepository).findById(1L);
        verify(tipoPlanRepository).findById(1L);
        verify(planCorporativoRepository).findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(1L, PlanCorporativo.EstadoPlanCorporativo.ACTIVO);
        verify(usuarioRepository).findById(1L);
        verify(planCorporativoRepository).save(any(PlanCorporativo.class));
    }

    @Test
    void testNuevoPlanCorporativo_EmpresaFlotillaNotFound() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(999L);

        when(empresaFlotillaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Empresa de flotilla no encontrada.", exception.getMessage());
        verify(empresaFlotillaRepository).findById(999L);
    }

    @Test
    void testNuevoPlanCorporativo_TipoPlanNotFound() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(1L);
        dto.setIdTipoPlan(999L);

        when(empresaFlotillaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresaFlotilla));
        when(tipoPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Tipo de plan no encontrado.", exception.getMessage());
        verify(tipoPlanRepository).findById(999L);
    }

    @Test
    void testNuevoPlanCorporativo_PlanAlreadyExists() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(1L);
        dto.setIdTipoPlan(1L);

        when(empresaFlotillaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresaFlotilla));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(1L, PlanCorporativo.EstadoPlanCorporativo.ACTIVO))
                .thenReturn(mockPlanCorporativo);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Ya existe un plan corporativo activo para esta empresa de flotilla.", exception.getMessage());
    }

    @Test
    void testNuevoPlanCorporativo_UsuarioNotFound() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(1L);
        dto.setIdTipoPlan(1L);
        dto.setNombrePlanCorporativo("Test Plan");
        dto.setNumeroPlacasContratadas(25);
        dto.setDescuentoCorporativoAdicional(10.00); // Agregar este campo
        dto.setPrecioPlanCorporativo(10000.00); // Agregar este campo
        dto.setFechaInicio("2024-01-01");
        dto.setFechaFin("2024-12-31");
        dto.setIdCreadoPor(999L); // Usuario inexistente

        when(empresaFlotillaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresaFlotilla));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(1L, PlanCorporativo.EstadoPlanCorporativo.ACTIVO))
                .thenReturn(null);
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Usuario creador no encontrado.", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testActivarPlanCorporativo_Success() {
        // Arrange
        PlanCorporativo planInactivo = new PlanCorporativo();
        planInactivo.setIdPlanCorporativo(1L);
        planInactivo.setEmpresaFlotilla(mockEmpresaFlotilla);
        planInactivo.setEstado(PlanCorporativo.EstadoPlanCorporativo.CANCELADO);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(planInactivo));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(
                mockEmpresaFlotilla.getIdEmpresaFlotilla(), PlanCorporativo.EstadoPlanCorporativo.ACTIVO))
                .thenReturn(null);
        when(planCorporativoRepository.save(any(PlanCorporativo.class))).thenReturn(planInactivo);

        // Act
        String result = gestionEmpresaFlotillaService.activarPlanCorporativo(1L);

        // Assert
        assertEquals("Plan corporativo activado con éxito.", result);
        verify(planCorporativoRepository).save(any(PlanCorporativo.class));
    }

    @Test
    void testActivarPlanCorporativo_PlanNotFound() {
        // Arrange
        when(planCorporativoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.activarPlanCorporativo(999L);
        });

        assertEquals("Plan corporativo no encontrado.", exception.getMessage());
    }

    @Test
    void testActivarPlanCorporativo_AlreadyActive() {
        // Arrange
        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.activarPlanCorporativo(1L);
        });

        assertEquals("El plan corporativo ya está activo.", exception.getMessage());
    }

    @Test
    void testActivarPlanCorporativo_OtherPlanAlreadyActive() {
        // Arrange
        PlanCorporativo planInactivo = new PlanCorporativo();
        planInactivo.setIdPlanCorporativo(1L);
        planInactivo.setEmpresaFlotilla(mockEmpresaFlotilla);
        planInactivo.setEstado(PlanCorporativo.EstadoPlanCorporativo.CANCELADO);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(planInactivo));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(
                mockEmpresaFlotilla.getIdEmpresaFlotilla(), PlanCorporativo.EstadoPlanCorporativo.ACTIVO))
                .thenReturn(mockPlanCorporativo);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.activarPlanCorporativo(1L);
        });

        assertEquals("Ya existe un plan corporativo activo para esta empresa de flotilla.", exception.getMessage());
    }

    @Test
    void testDesactivarPlanCorporativo_Success() {
        // Arrange
        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));
        when(planCorporativoRepository.save(any(PlanCorporativo.class))).thenReturn(mockPlanCorporativo);

        // Act
        String result = gestionEmpresaFlotillaService.desactivarPlanCorporativo(1L);

        // Assert
        assertEquals("Plan corporativo desactivado con éxito.", result);
        verify(planCorporativoRepository).save(any(PlanCorporativo.class));
    }

    @Test
    void testDesactivarPlanCorporativo_PlanNotFound() {
        // Arrange
        when(planCorporativoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.desactivarPlanCorporativo(999L);
        });

        assertEquals("Plan corporativo no encontrado.", exception.getMessage());
    }

    @Test
    void testDesactivarPlanCorporativo_AlreadyInactive() {
        // Arrange
        PlanCorporativo planInactivo = new PlanCorporativo();
        planInactivo.setEstado(PlanCorporativo.EstadoPlanCorporativo.CANCELADO);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(planInactivo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.desactivarPlanCorporativo(1L);
        });

        assertEquals("El plan corporativo ya está inactivo.", exception.getMessage());
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_Success() {
        // Arrange
        SuscripcionFlotillaDTO dto = new SuscripcionFlotillaDTO();
        dto.setIdPlanCorporativo(1L);
        dto.setIdVehiculo(1L);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(suscripcionFlotillaRepository.findByVehiculo_IdAndEstado(1L, SuscripcionFlotilla.EstadoSuscripcion.ACTIVA))
                .thenReturn(null);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(suscripcionFlotillaRepository.save(any(SuscripcionFlotilla.class))).thenReturn(mockSuscripcionFlotilla);

        // Act
        String result = gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(dto);

        // Assert
        assertEquals("Vehículo suscrito al plan corporativo con éxito.", result);
        verify(suscripcionFlotillaRepository).save(any(SuscripcionFlotilla.class));
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_PlanNotFound() {
        // Arrange
        SuscripcionFlotillaDTO dto = new SuscripcionFlotillaDTO();
        dto.setIdPlanCorporativo(999L);

        when(planCorporativoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(dto);
        });

        assertEquals("Plan corporativo no encontrado.", exception.getMessage());
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_VehiculoNotFound() {
        // Arrange
        SuscripcionFlotillaDTO dto = new SuscripcionFlotillaDTO();
        dto.setIdPlanCorporativo(1L);
        dto.setIdVehiculo(999L);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(dto);
        });

        assertEquals("Vehículo no encontrado.", exception.getMessage());
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_VehiculoAlreadySubscribed() {
        // Arrange
        SuscripcionFlotillaDTO dto = new SuscripcionFlotillaDTO();
        dto.setIdPlanCorporativo(1L);
        dto.setIdVehiculo(1L);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(suscripcionFlotillaRepository.findByVehiculo_IdAndEstado(1L, SuscripcionFlotilla.EstadoSuscripcion.ACTIVA))
                .thenReturn(mockSuscripcionFlotilla);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(dto);
        });

        assertEquals("El vehículo ya está suscrito a este plan corporativo.", exception.getMessage());
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_VehiculoHasIndividualSubscription() {
        // Arrange
        SuscripcionFlotillaDTO dto = new SuscripcionFlotillaDTO();
        dto.setIdPlanCorporativo(1L);
        dto.setIdVehiculo(1L);

        when(planCorporativoRepository.findById(1L)).thenReturn(Optional.of(mockPlanCorporativo));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(suscripcionFlotillaRepository.findByVehiculo_IdAndEstado(1L, SuscripcionFlotilla.EstadoSuscripcion.ACTIVA))
                .thenReturn(null);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList(mockSuscripcion));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(dto);
        });

        assertEquals("El vehículo ya está suscrito a un plan individual.", exception.getMessage());
    }

    @Test
    void testCancelarSuscripcionVehiculoPlanCorporativo_Success() {
        // Arrange
        when(suscripcionFlotillaRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcionFlotilla));
        when(suscripcionFlotillaRepository.save(any(SuscripcionFlotilla.class))).thenReturn(mockSuscripcionFlotilla);

        // Act
        String result = gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(1L);

        // Assert
        assertEquals("Suscripción de vehículo a plan corporativo cancelada con éxito.", result);
        verify(suscripcionFlotillaRepository).save(any(SuscripcionFlotilla.class));
    }

    @Test
    void testCancelarSuscripcionVehiculoPlanCorporativo_SuscripcionNotFound() {
        // Arrange
        when(suscripcionFlotillaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(999L);
        });

        assertEquals("Suscripción de flotilla no encontrada.", exception.getMessage());
    }

    @Test
    void testCancelarSuscripcionVehiculoPlanCorporativo_AlreadyInactive() {
        // Arrange
        SuscripcionFlotilla suscripcionInactiva = new SuscripcionFlotilla();
        suscripcionInactiva.setEstado(SuscripcionFlotilla.EstadoSuscripcion.INACTIVA);

        when(suscripcionFlotillaRepository.findById(1L)).thenReturn(Optional.of(suscripcionInactiva));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(1L);
        });

        assertEquals("La suscripción de flotilla ya está inactiva.", exception.getMessage());
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_Success() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(empresaFlotillaRepository.findAll()).thenReturn(Arrays.asList(mockEmpresaFlotilla));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(1L, 1L))
                .thenReturn(Arrays.asList(mockPlanCorporativo));
        when(suscripcionFlotillaRepository.findByPlanCorporativo_IdPlanCorporativo(1L))
                .thenReturn(Arrays.asList(mockSuscripcionFlotilla));

        // Act
        DetalleEmpresaFlotillaDTO result = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEmpresasFlotilla());
        assertEquals(1, result.getEmpresasFlotilla().size());

        DetalleEmpresaFlotillaDTO.EmpresaFlotillaDTO empresaDTO = result.getEmpresasFlotilla().get(0);
        assertEquals(mockEmpresaFlotilla.getIdEmpresaFlotilla(), empresaDTO.getIdEmpresaFlotilla());
        assertEquals("Test Fleet Company", empresaDTO.getNombreEmpresa());
        assertEquals("Test Fleet Company S.A.", empresaDTO.getRazonSocial());
        assertEquals("1234567-8", empresaDTO.getNit());

        assertEquals(1, empresaDTO.getPlanesCorporativos().size());
        DetalleEmpresaFlotillaDTO.PlanCorporativoDTO planDTO = empresaDTO.getPlanesCorporativos().get(0);
        assertEquals(mockPlanCorporativo.getIdPlanCorporativo(), planDTO.getIdPlanCorporativo());
        assertEquals("Corporate Plan Test", planDTO.getNombrePlanCorporativo());
        assertEquals(50, planDTO.getNumeroPlacasContratadas());

        assertEquals(1, planDTO.getSuscripcionesVehiculos().size());
        DetalleEmpresaFlotillaDTO.SuscripcionVehiculoDTO suscripcionDTO = planDTO.getSuscripcionesVehiculos().get(0);
        assertEquals(mockSuscripcionFlotilla.getIdSuscripcionFlotilla(), suscripcionDTO.getIdSuscripcionFlotilla());
        assertEquals("ABC123", suscripcionDTO.getPlacaVehiculo());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(empresaFlotillaRepository).findAll();
        verify(planCorporativoRepository).findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(1L, 1L);
        verify(suscripcionFlotillaRepository).findByPlanCorporativo_IdPlanCorporativo(1L);
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_EmpresaNotFound() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(999L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(999L);
        });

        assertEquals("Empresa no encontrada para el usuario proporcionado.", exception.getMessage());
    }

    @Test
    void testObtenerVehiculos_Success() {
        // Arrange
        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo);
        when(vehiculoRepository.findAll()).thenReturn(vehiculos);

        // Act
        List<VehiculoClienteDTO> result = gestionEmpresaFlotillaService.obtenerVehiculos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        VehiculoClienteDTO vehiculoDTO = result.get(0);
        assertEquals(mockVehiculo.getId(), vehiculoDTO.getIdVehiculo());
        assertEquals("ABC123", vehiculoDTO.getPlaca());
        assertEquals("Toyota", vehiculoDTO.getMarca());
        assertEquals("Corolla", vehiculoDTO.getModelo());
        assertEquals("Blanco", vehiculoDTO.getColor());
        assertEquals("CUATRO_RUEDAS", vehiculoDTO.getTipoVehiculo());

        verify(vehiculoRepository).findAll();
    }

    @Test
    void testObtenerVehiculos_EmptyList() {
        // Arrange
        when(vehiculoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<VehiculoClienteDTO> result = gestionEmpresaFlotillaService.obtenerVehiculos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(vehiculoRepository).findAll();
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_EmptyFlotillas() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(empresaFlotillaRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        DetalleEmpresaFlotillaDTO result = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEmpresasFlotilla());
        assertTrue(result.getEmpresasFlotilla().isEmpty());
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_EmptyPlanes() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(empresaFlotillaRepository.findAll()).thenReturn(Arrays.asList(mockEmpresaFlotilla));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(1L, 1L))
                .thenReturn(Arrays.asList());

        // Act
        DetalleEmpresaFlotillaDTO result = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEmpresasFlotilla().size());
        assertTrue(result.getEmpresasFlotilla().get(0).getPlanesCorporativos().isEmpty());
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_EmptySuscripciones() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList(mockEmpresa));
        when(empresaFlotillaRepository.findAll()).thenReturn(Arrays.asList(mockEmpresaFlotilla));
        when(planCorporativoRepository.findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(1L, 1L))
                .thenReturn(Arrays.asList(mockPlanCorporativo));
        when(suscripcionFlotillaRepository.findByPlanCorporativo_IdPlanCorporativo(1L))
                .thenReturn(Arrays.asList());

        // Act
        DetalleEmpresaFlotillaDTO result = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEmpresasFlotilla().size());
        assertEquals(1, result.getEmpresasFlotilla().get(0).getPlanesCorporativos().size());
        assertTrue(result.getEmpresasFlotilla().get(0).getPlanesCorporativos().get(0).getSuscripcionesVehiculos().isEmpty());
    }
}
