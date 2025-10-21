package org.parkcontrol.apiparkcontrol.services.planes_suscripcion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
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
class PlanesSuscripcionServiceUnitTest {

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
    private BitacoraConfiguracionDescuentoRepository bitacoraConfiguracionDescuentoRepository;

    @InjectMocks
    private PlanesSuscripcionService planesSuscripcionService;

    private Empresa mockEmpresa;
    private Usuario mockUsuario;
    private TipoPlan mockTipoPlan;
    private ConfiguracionDescuentoPlan mockConfigDescuento;

    @BeforeEach
    void setUp() {
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("testuser");

        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setEmpresa(mockEmpresa);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setCodigoPlan("WW-001");
        mockTipoPlan.setDescripcion("Plan Workweek");
        mockTipoPlan.setPrecioPlan(150.00);
        mockTipoPlan.setHorasDia(8);
        mockTipoPlan.setHorasMensuales(160);
        mockTipoPlan.setDiasAplicables("L-M-X-J-V");
        mockTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        mockTipoPlan.setOrdenBeneficio(2);
        mockTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        mockTipoPlan.setFechaCreacion(LocalDateTime.now());

        mockConfigDescuento = new ConfiguracionDescuentoPlan();
        mockConfigDescuento.setId(1L);
        mockConfigDescuento.setTipoPlan(mockTipoPlan);
        mockConfigDescuento.setDescuentoMensual(new BigDecimal("15.00"));
        mockConfigDescuento.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        mockConfigDescuento.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        mockConfigDescuento.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        mockConfigDescuento.setCreadoPor(mockUsuario);
        mockConfigDescuento.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        mockConfigDescuento.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_Success() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);

        // Act
        List<DetalleTipoPlanDTO> result = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        DetalleTipoPlanDTO planDTO = result.get(0);
        assertEquals(1L, planDTO.getId());
        assertEquals("WORKWEEK", planDTO.getNombrePlan());
        assertEquals("WW-001", planDTO.getCodigoPlan());
        assertEquals("Plan Workweek", planDTO.getDescripcion());
        assertEquals(150.00, planDTO.getPrecioPlan());
        assertEquals(8, planDTO.getHorasDia());
        assertEquals(160, planDTO.getHorasMensuales());
        assertEquals("L-M-X-J-V", planDTO.getDiasAplicables());
        assertEquals("08:00 - 18:00", planDTO.getCoberturaHoraria());
        assertEquals(2, planDTO.getOrdenBeneficio());
        assertEquals("VIGENTE", planDTO.getActivo());

        DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configDTO = planDTO.getConfiguracionDescuento();
        assertNotNull(configDTO);
        assertEquals(1L, configDTO.getIdConfiguracionDescuento());
        assertEquals(15.00, configDTO.getDescuentoMensual());
        assertEquals(5.00, configDTO.getDescuentoAnualAdicional());

        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(tipoPlanRepository).findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE);
        verify(configuracionDescuentoPlanRepository).findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_EmpresaNotFound() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(999L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(999L);
        });

        assertEquals("Empresa no encontrada para el usuario con ID: 999", exception.getMessage());
        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(999L);
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_WithoutConfigDescuento() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(null);

        // Act
        List<DetalleTipoPlanDTO> result = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getConfiguracionDescuento());
    }

    @Test
    void testCrearNuevoPlanSuscripcion_Success() {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(1L);
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek");
        nuevoPlanDTO.setPrecioPlan(150.00);
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(15.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(5.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.save(any(ConfiguracionDescuentoPlan.class))).thenReturn(mockConfigDescuento);
        when(bitacoraConfiguracionDescuentoRepository.save(any(BitacoraConfiguracionDescuento.class)))
                .thenReturn(new BitacoraConfiguracionDescuento());

        // Act
        String result = planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);

        // Assert
        assertEquals("Nuevo plan de suscripción creado con éxito para la empresa con ID: 1", result);
        verify(empresaRepository).findById(1L);
        verify(tipoPlanRepository, times(2)).findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE);
        verify(usuarioRepository).findById(1L);
        verify(tipoPlanRepository).save(any(TipoPlan.class));
        verify(configuracionDescuentoPlanRepository).save(any(ConfiguracionDescuentoPlan.class));
        verify(bitacoraConfiguracionDescuentoRepository).save(any(BitacoraConfiguracionDescuento.class));
    }

    @Test
    void testCrearNuevoPlanSuscripcion_EmpresaNotFound() {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(999L);

        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });

        assertEquals("Empresa no encontrada con ID: 999", exception.getMessage());
        verify(empresaRepository).findById(999L);
    }

    @Test
    void testCrearNuevoPlanSuscripcion_PlanAlreadyExists() {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(1L);
        nuevoPlanDTO.setNombrePlan("WORKWEEK");

        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe un plan con el nombre WORKWEEK"));
        verify(empresaRepository).findById(1L);
        verify(tipoPlanRepository).findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE);
    }

    @Test
    void testCrearNuevoPlanSuscripcion_UsuarioNotFound() {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(1L);
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek");
        nuevoPlanDTO.setPrecioPlan(150.00);
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V"); // Agregar este campo requerido
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(15.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(5.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(999L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });

        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testEditarPlanSuscripcion_Success() {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(1L);
        editarPlanDTO.setNombrePlan("WORKWEEK");
        editarPlanDTO.setCodigoPlan("WW-002");
        editarPlanDTO.setDescripcion("Plan Workweek Actualizado");
        editarPlanDTO.setPrecioPlan(175.00);
        editarPlanDTO.setHorasMensuales(170);
        editarPlanDTO.setDiasAplicables("L-M-X-J-V");
        editarPlanDTO.setCoberturaHoraria("08:00 - 19:00");
        editarPlanDTO.setDescuentoMensual(16.00);
        editarPlanDTO.setDescuentoAnualAdicional(6.00);
        editarPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editarPlanDTO.setFechaVigenciaFin("2024-12-31");
        editarPlanDTO.setIdUsuarioCreacion(1L);

        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);
        when(configuracionDescuentoPlanRepository.save(any(ConfiguracionDescuentoPlan.class))).thenReturn(mockConfigDescuento);
        when(bitacoraConfiguracionDescuentoRepository.save(any(BitacoraConfiguracionDescuento.class)))
                .thenReturn(new BitacoraConfiguracionDescuento());

        // Act
        String result = planesSuscripcionService.editarPlanSuscripcion(editarPlanDTO);

        // Assert
        assertEquals("Plan de suscripción editado con éxito para la empresa con ID: 1", result);
        verify(tipoPlanRepository).findById(1L);
        verify(tipoPlanRepository, times(2)).save(any(TipoPlan.class));
        verify(configuracionDescuentoPlanRepository, times(2)).save(any(ConfiguracionDescuentoPlan.class));
        verify(bitacoraConfiguracionDescuentoRepository).save(any(BitacoraConfiguracionDescuento.class));
    }

    @Test
    void testEditarPlanSuscripcion_PlanNotFound() {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(999L);

        when(tipoPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.editarPlanSuscripcion(editarPlanDTO);
        });

        assertEquals("Tipo de plan no encontrado con ID: 999", exception.getMessage());
        verify(tipoPlanRepository).findById(999L);
    }

    @Test
    void testValidarPorcentajesDescuento_Success() {
        // Arrange
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());

        // Act
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(15.00, "WORKWEEK", 5.00, 1L);

        // Assert
        assertTrue(result);
        verify(tipoPlanRepository).findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE);
    }

    @Test
    void testValidarPorcentajesDescuento_SameOrderBenefit() {
        // Arrange
        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setId(2L);
        existingPlan.setOrdenBeneficio(2); // Same as WORKWEEK

        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(2L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);

        // Act
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(15.00, "WORKWEEK", 5.00, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidarPorcentajesDescuento_HigherBenefitLowerPercentage() {
        // Arrange - WORKWEEK (order 2) trying to have lower percentage than OFFICE_LIGHT (order 3)
        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setId(3L);
        existingPlan.setOrdenBeneficio(3); // Lower benefit than WORKWEEK

        ConfiguracionDescuentoPlan existingConfig = new ConfiguracionDescuentoPlan();
        existingConfig.setDescuentoMensual(new BigDecimal("10.00"));
        existingConfig.setDescuentoAnualAdicional(new BigDecimal("3.00"));

        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(3L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(existingConfig);

        // Act - WORKWEEK should have higher percentages than OFFICE_LIGHT
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(5.00, "WORKWEEK", 2.00, 1L);

        // Assert
        assertFalse(result); // Should fail because WORKWEEK should have higher percentages
    }

    @Test
    void testValidarPorcentajesDescuento_LowerBenefitHigherPercentage() {
        // Arrange - OFFICE_LIGHT (order 3) trying to have higher percentage than WORKWEEK (order 2)
        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setId(2L);
        existingPlan.setOrdenBeneficio(2); // Higher benefit than OFFICE_LIGHT

        ConfiguracionDescuentoPlan existingConfig = new ConfiguracionDescuentoPlan();
        existingConfig.setDescuentoMensual(new BigDecimal("15.00"));
        existingConfig.setDescuentoAnualAdicional(new BigDecimal("5.00"));

        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(2L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(existingConfig);

        // Act - OFFICE_LIGHT should have lower percentages than WORKWEEK
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(20.00, "OFFICE_LIGHT", 8.00, 1L);

        // Assert
        assertFalse(result); // Should fail because OFFICE_LIGHT should have lower percentages
    }

    @Test
    void testValidarPorcentajesDescuento_WithNullConfigDescuento() {
        // Arrange
        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setId(2L);
        existingPlan.setOrdenBeneficio(1);

        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(2L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(null);

        // Act
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(15.00, "WORKWEEK", 5.00, 1L);

        // Assert
        assertTrue(result); // Should pass when no config is found
    }

    @Test
    void testObtenerOrdenBeneficio_AllPlanTypes() {
        // Test each plan type individually to cover all switch cases
        NuevoPlanDTO planDTO = new NuevoPlanDTO();
        planDTO.setIdEmpresa(1L);
        planDTO.setCodigoPlan("TEST");
        planDTO.setDescripcion("Test");
        planDTO.setPrecioPlan(100.00);
        planDTO.setHorasMensuales(100);
        planDTO.setDiasAplicables("L-M-X-J-V");
        planDTO.setCoberturaHoraria("Test");
        planDTO.setDescuentoMensual(50.00);
        planDTO.setDescuentoAnualAdicional(20.00);
        planDTO.setFechaVigenciaInicio("2024-01-01");
        planDTO.setFechaVigenciaFin("2024-12-31");
        planDTO.setIdUsuarioCreacion(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.save(any(ConfiguracionDescuentoPlan.class))).thenReturn(mockConfigDescuento);
        when(bitacoraConfiguracionDescuentoRepository.save(any(BitacoraConfiguracionDescuento.class)))
                .thenReturn(new BitacoraConfiguracionDescuento());

        // Test all plan types
        String[] planTypes = {"FULL_ACCESS", "WORKWEEK", "OFFICE_LIGHT", "DIARIO_FLEXIBLE", "NOCTURNO"};
        Double[] precios = {500.00, 350.00, 250.00, 150.00, 100.00};

        for (int i = 0; i < planTypes.length; i++) {
            planDTO.setNombrePlan(planTypes[i]);
            planDTO.setPrecioPlan(precios[i]);
            assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));
        }
    }

    @Test
    void testObtenerOrdenBeneficio_InvalidPlan() {
        // Arrange
        NuevoPlanDTO planDTO = new NuevoPlanDTO();
        planDTO.setIdEmpresa(1L);
        planDTO.setNombrePlan("INVALID_PLAN");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO);
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("INVALID_PLAN") || 
                  exception.getMessage().contains("plan no válido") || 
                  exception.getMessage().contains("IllegalArgumentException"));
    }

    @Test
    void testCrearNuevoPlanSuscripcion_InvalidPercentages() {
        // Arrange - WORKWEEK intentando tener porcentajes MAYORES que FULL_ACCESS
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(1L);
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek");
        nuevoPlanDTO.setPrecioPlan(300.00);
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(30.00); // MAYOR que FULL_ACCESS (invalid)
        nuevoPlanDTO.setDescuentoAnualAdicional(12.00); // MAYOR que FULL_ACCESS (invalid)
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(1L);

        // FULL_ACCESS ya existe con orden 1 (mayor beneficio)
        TipoPlan existingPlan = new TipoPlan();
        existingPlan.setId(2L);
        existingPlan.setNombrePlan(TipoPlan.NombrePlan.FULL_ACCESS);
        existingPlan.setOrdenBeneficio(1);

        ConfiguracionDescuentoPlan existingConfig = new ConfiguracionDescuentoPlan();
        existingConfig.setDescuentoMensual(new BigDecimal("25.00"));
        existingConfig.setDescuentoAnualAdicional(new BigDecimal("10.00"));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(existingPlan));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(2L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(existingConfig);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Los porcentajes de descuento no cumplen con las reglas de negocio"));
    }

    @Test
    void testValidarPorcentajesDescuento_MultipleConflicts() {
        // Arrange - WORKWEEK (order 2) con múltiples planes existentes
        TipoPlan plan1 = new TipoPlan(); // FULL_ACCESS
        plan1.setId(1L);
        plan1.setOrdenBeneficio(1);
        
        TipoPlan plan2 = new TipoPlan(); // OFFICE_LIGHT  
        plan2.setId(3L);
        plan2.setOrdenBeneficio(3);

        ConfiguracionDescuentoPlan config1 = new ConfiguracionDescuentoPlan();
        config1.setDescuentoMensual(new BigDecimal("25.00"));
        config1.setDescuentoAnualAdicional(new BigDecimal("10.00"));

        ConfiguracionDescuentoPlan config2 = new ConfiguracionDescuentoPlan();
        config2.setDescuentoMensual(new BigDecimal("10.00"));
        config2.setDescuentoAnualAdicional(new BigDecimal("3.00"));

        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList(plan1, plan2));
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(config1);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(3L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(config2);

        // Act - WORKWEEK con 15% mensual: MENOS que FULL_ACCESS (25%) ✓ pero MAS que OFFICE_LIGHT (10%) ✓
        boolean result = planesSuscripcionService.validarPorcentajesDescuento(15.00, "WORKWEEK", 5.00, 1L);

        // Assert - Debe ser TRUE porque cumple las reglas: menor que superior, mayor que inferior
        assertTrue(result);
    }

    @Test
    void testCalcularHorasDias_DifferentDayFormats() {
        // Test con diferentes formatos de días para verificar el cálculo
        NuevoPlanDTO planDTO = new NuevoPlanDTO();
        planDTO.setIdEmpresa(1L);
        planDTO.setNombrePlan("WORKWEEK");
        planDTO.setCodigoPlan("WW-TEST");
        planDTO.setDescripcion("Test");
        planDTO.setPrecioPlan(200.00);
        planDTO.setHorasMensuales(173);
        planDTO.setCoberturaHoraria("08:00 - 17:00");
        planDTO.setDescuentoMensual(15.00);
        planDTO.setDescuentoAnualAdicional(5.00);
        planDTO.setFechaVigenciaInicio("2024-01-01");
        planDTO.setFechaVigenciaFin("2024-12-31");
        planDTO.setIdUsuarioCreacion(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.save(any(ConfiguracionDescuentoPlan.class))).thenReturn(mockConfigDescuento);
        when(bitacoraConfiguracionDescuentoRepository.save(any(BitacoraConfiguracionDescuento.class)))
                .thenReturn(new BitacoraConfiguracionDescuento());

        // Test con 5 días (L-M-X-J-V)
        planDTO.setDiasAplicables("L-M-X-J-V");
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));

        // Test con 7 días (L-M-X-J-V-S-D)
        planDTO.setCodigoPlan("WW-TEST2");
        planDTO.setDiasAplicables("L-M-X-J-V-S-D");
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));

        // Test con 1 día (L)
        planDTO.setCodigoPlan("WW-TEST3");
        planDTO.setDiasAplicables("L");
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));
    }

    @Test
    void testCrearNuevoPlanSuscripcion_WithDifferentPriceRanges() {
        // Test para verificar que el servicio maneja diferentes rangos de precios
        NuevoPlanDTO planDTO = new NuevoPlanDTO();
        planDTO.setIdEmpresa(1L);
        planDTO.setNombrePlan("FULL_ACCESS");
        planDTO.setCodigoPlan("FA-001");
        planDTO.setDescripcion("Plan Full Access");
        planDTO.setHorasMensuales(300);
        planDTO.setDiasAplicables("L-M-X-J-V-S-D");
        planDTO.setCoberturaHoraria("00:00 - 23:59");
        planDTO.setDescuentoMensual(25.00);
        planDTO.setDescuentoAnualAdicional(10.00);
        planDTO.setFechaVigenciaInicio("2024-01-01");
        planDTO.setFechaVigenciaFin("2024-12-31");
        planDTO.setIdUsuarioCreacion(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(Arrays.asList());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(tipoPlanRepository.save(any(TipoPlan.class))).thenReturn(mockTipoPlan);
        when(configuracionDescuentoPlanRepository.save(any(ConfiguracionDescuentoPlan.class))).thenReturn(mockConfigDescuento);
        when(bitacoraConfiguracionDescuentoRepository.save(any(BitacoraConfiguracionDescuento.class)))
                .thenReturn(new BitacoraConfiguracionDescuento());

        // Test con precio alto
        planDTO.setPrecioPlan(1000.00);
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));

        // Test con precio bajo
        planDTO.setCodigoPlan("FA-002");
        planDTO.setPrecioPlan(50.00);
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));

        // Test con precio cero
        planDTO.setCodigoPlan("FA-003");
        planDTO.setPrecioPlan(0.00);
        assertDoesNotThrow(() -> planesSuscripcionService.crearNuevoPlanSuscripcion(planDTO));
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_ConfigDescuentoWithNullFin() {
        // Test case where fechaVigenciaFin is null
        ConfiguracionDescuentoPlan configWithNullFin = new ConfiguracionDescuentoPlan();
        configWithNullFin.setId(1L);
        configWithNullFin.setTipoPlan(mockTipoPlan);
        configWithNullFin.setDescuentoMensual(new BigDecimal("15.00"));
        configWithNullFin.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        configWithNullFin.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        configWithNullFin.setFechaVigenciaFin(null); // Null end date
        configWithNullFin.setCreadoPor(mockUsuario);
        configWithNullFin.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configWithNullFin.setFechaCreacion(LocalDateTime.now());

        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(configWithNullFin);

        // Act & Assert - Should throw NullPointerException when trying to call toString() on null
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L);
        });

        assertNotNull(exception);
    }
}
