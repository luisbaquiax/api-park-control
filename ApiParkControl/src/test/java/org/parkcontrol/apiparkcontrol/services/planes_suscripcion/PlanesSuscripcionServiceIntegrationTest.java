package org.parkcontrol.apiparkcontrol.services.planes_suscripcion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PlanesSuscripcionServiceIntegrationTest {

    @Autowired
    private PlanesSuscripcionService planesSuscripcionService;

    @Autowired
    private TipoPlanRepository tipoPlanRepository;

    @Autowired
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private BitacoraConfiguracionDescuentoRepository bitacoraConfiguracionDescuentoRepository;

    private Empresa testEmpresa;
    private Usuario testUsuario;
    private Rol empresaRol;

    @BeforeEach
    void setUp() {
        // Clean up database
        bitacoraConfiguracionDescuentoRepository.deleteAll();
        configuracionDescuentoPlanRepository.deleteAll();
        tipoPlanRepository.deleteAll();
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
        Persona persona = new Persona();
        persona.setNombre("Test");
        persona.setApellido("User");
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        persona.setDpi(generateUniqueDpi());
        persona.setCorreo("test@empresa.com");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        testUsuario = new Usuario();
        testUsuario.setPersona(persona);
        testUsuario.setRol(empresaRol);
        testUsuario.setNombreUsuario("empresauser");
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testUsuario);
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Fiscal Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_EmptyList_Integration() {
        // Act
        List<DetalleTipoPlanDTO> result = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCrearNuevoPlanSuscripcion_Integration() {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek para uso de lunes a viernes");
        nuevoPlanDTO.setPrecioPlan(275.00); // Nuevo campo
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V"); // Formato correcto para calcular horas/día
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(15.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(5.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        // Act
        String result = planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);

        // Assert
        assertEquals("Nuevo plan de suscripción creado con éxito para la empresa con ID: " + testEmpresa.getIdEmpresa(), result);

        // Verify plan was created with all fields
        List<TipoPlan> planes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, planes.size());

        TipoPlan createdPlan = planes.get(0);
        assertEquals(TipoPlan.NombrePlan.WORKWEEK, createdPlan.getNombrePlan());
        assertEquals("WW-001", createdPlan.getCodigoPlan());
        assertEquals("Plan Workweek para uso de lunes a viernes", createdPlan.getDescripcion());
        assertEquals(275.00, createdPlan.getPrecioPlan()); // Verificar nuevo campo
        assertEquals(160, createdPlan.getHorasMensuales());
        assertEquals(2, createdPlan.getOrdenBeneficio()); // WORKWEEK order
        
        // Verificar que horasDia fue calculado correctamente
        // 160 horas / (4.33 semanas * 5 días) ≈ 7.4 ≈ 7 horas/día
        assertNotNull(createdPlan.getHorasDia());
        assertTrue(createdPlan.getHorasDia() >= 7 && createdPlan.getHorasDia() <= 8); // Rango esperado

        // Verify configuration was created
        ConfiguracionDescuentoPlan config = configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(
                createdPlan.getId(), ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        assertNotNull(config);
        assertEquals(0, new BigDecimal("15.00").compareTo(config.getDescuentoMensual()));
        assertEquals(0, new BigDecimal("5.00").compareTo(config.getDescuentoAnualAdicional()));

        // Verify bitacora was created
        List<BitacoraConfiguracionDescuento> bitacoras = bitacoraConfiguracionDescuentoRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraConfiguracionDescuento.Accion.CREACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testCrearNuevoPlanSuscripcion_AllPlanTypes_Integration() {
        // Test creating all plan types with their respective prices
        String[] planNames = {"FULL_ACCESS", "WORKWEEK", "OFFICE_LIGHT", "DIARIO_FLEXIBLE", "NOCTURNO"};
        double[] monthlyDiscounts = {25.00, 20.00, 15.00, 10.00, 5.00};
        double[] annualDiscounts = {10.00, 8.00, 6.00, 4.00, 2.00};
        double[] precios = {500.00, 350.00, 250.00, 150.00, 100.00}; // Precios decrecientes según beneficio
        int[] horasMensuales = {300, 240, 180, 120, 80}; // Horas decrecientes según beneficio

        for (int i = 0; i < planNames.length; i++) {
            NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
            nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
            nuevoPlanDTO.setNombrePlan(planNames[i]);
            nuevoPlanDTO.setCodigoPlan(planNames[i] + "-001");
            nuevoPlanDTO.setDescripcion("Plan " + planNames[i]);
            nuevoPlanDTO.setPrecioPlan(precios[i]); // Nuevo campo
            nuevoPlanDTO.setHorasMensuales(horasMensuales[i]);
            nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
            nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
            nuevoPlanDTO.setDescuentoMensual(monthlyDiscounts[i]);
            nuevoPlanDTO.setDescuentoAnualAdicional(annualDiscounts[i]);
            nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
            nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
            nuevoPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

            String result = planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
            assertNotNull(result);
            assertTrue(result.contains("éxito"));
        }

        // Verify all plans were created with correct prices and calculated hours
        List<TipoPlan> allPlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(5, allPlans.size());

        // Verify prices and calculated hours for each plan
        for (TipoPlan plan : allPlans) {
            assertNotNull(plan.getPrecioPlan());
            assertNotNull(plan.getHorasDia());
            assertTrue(plan.getPrecioPlan() > 0);
            assertTrue(plan.getHorasDia() > 0);
            
            // Verify orden beneficio matches expected values
            switch (plan.getNombrePlan()) {
                case FULL_ACCESS -> {
                    assertEquals(1, plan.getOrdenBeneficio());
                    assertEquals(500.00, plan.getPrecioPlan());
                }
                case WORKWEEK -> {
                    assertEquals(2, plan.getOrdenBeneficio());
                    assertEquals(350.00, plan.getPrecioPlan());
                }
                case OFFICE_LIGHT -> {
                    assertEquals(3, plan.getOrdenBeneficio());
                    assertEquals(250.00, plan.getPrecioPlan());
                }
                case DIARIO_FLEXIBLE -> {
                    assertEquals(4, plan.getOrdenBeneficio());
                    assertEquals(150.00, plan.getPrecioPlan());
                }
                case NOCTURNO -> {
                    assertEquals(5, plan.getOrdenBeneficio());
                    assertEquals(100.00, plan.getPrecioPlan());
                }
            }
        }
    }

    @Test
    void testEditarPlanSuscripcion_Integration() {
        // Arrange - First create a plan to edit
        TipoPlan originalPlan = createTestTipoPlan();
        ConfiguracionDescuentoPlan originalConfig = createTestConfigDescuento(originalPlan);

        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(originalPlan.getId());
        editarPlanDTO.setNombrePlan("WORKWEEK");
        editarPlanDTO.setCodigoPlan("WW-002");
        editarPlanDTO.setDescripcion("Plan Workweek actualizado");
        editarPlanDTO.setPrecioPlan(325.00); // Nuevo precio actualizado
        editarPlanDTO.setHorasMensuales(180);
        editarPlanDTO.setDiasAplicables("L-M-X-J-V");
        editarPlanDTO.setCoberturaHoraria("07:00 - 19:00");
        editarPlanDTO.setDescuentoMensual(18.00);
        editarPlanDTO.setDescuentoAnualAdicional(7.00);
        editarPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editarPlanDTO.setFechaVigenciaFin("2024-12-31");
        editarPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        // Act
        String result = planesSuscripcionService.editarPlanSuscripcion(editarPlanDTO);

        // Assert
        assertEquals("Plan de suscripción editado con éxito para la empresa con ID: " + testEmpresa.getIdEmpresa(), result);

        // Verify original plan is now historic
        TipoPlan updatedOriginal = tipoPlanRepository.findById(originalPlan.getId()).orElse(null);
        assertNotNull(updatedOriginal);
        assertEquals(TipoPlan.EstadoConfiguracion.HISTORICO, updatedOriginal.getActivo());

        // Verify new plan was created with updated values
        List<TipoPlan> vigentePlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, vigentePlans.size());

        TipoPlan newPlan = vigentePlans.get(0);
        assertEquals("WW-002", newPlan.getCodigoPlan());
        assertEquals("Plan Workweek actualizado", newPlan.getDescripcion());
        assertEquals(325.00, newPlan.getPrecioPlan()); // Verificar nuevo precio
        assertEquals(180, newPlan.getHorasMensuales());
        
        // Verificar que horasDia fue recalculado
        assertNotNull(newPlan.getHorasDia());
        assertTrue(newPlan.getHorasDia() >= 8 && newPlan.getHorasDia() <= 9); // Para 180 horas mensuales

        // Verify original config is historic
        ConfiguracionDescuentoPlan updatedOriginalConfig = configuracionDescuentoPlanRepository.findById(originalConfig.getId()).orElse(null);
        assertNotNull(updatedOriginalConfig);
        assertEquals(ConfiguracionDescuentoPlan.EstadoConfiguracion.HISTORICO, updatedOriginalConfig.getEstado());

        // Verify new config was created
        ConfiguracionDescuentoPlan newConfig = configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(
                newPlan.getId(), ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        assertNotNull(newConfig);
        assertEquals(0, new BigDecimal("18.00").compareTo(newConfig.getDescuentoMensual()));
        assertEquals(0, new BigDecimal("7.00").compareTo(newConfig.getDescuentoAnualAdicional()));

        // Verify bitacora for update (should have at least one update entry)
        List<BitacoraConfiguracionDescuento> bitacoras = bitacoraConfiguracionDescuentoRepository.findAll();
        assertTrue(bitacoras.size() >= 1); // At least one bitacora entry
        boolean hasUpdateAction = bitacoras.stream()
                .anyMatch(b -> b.getAccion() == BitacoraConfiguracionDescuento.Accion.ACTUALIZACION);
        assertTrue(hasUpdateAction);
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_WithPlans_Integration() {
        // Arrange - Create test plans
        TipoPlan plan1 = createTestTipoPlan();
        ConfiguracionDescuentoPlan config1 = createTestConfigDescuento(plan1);

        // Act
        List<DetalleTipoPlanDTO> result = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleTipoPlanDTO planDTO = result.get(0);
        assertEquals(plan1.getId(), planDTO.getId());
        assertEquals("WORKWEEK", planDTO.getNombrePlan());
        assertEquals("WW-001", planDTO.getCodigoPlan());
        assertEquals(160, planDTO.getHorasMensuales());
        assertEquals(2, planDTO.getOrdenBeneficio());
        assertEquals("VIGENTE", planDTO.getActivo());
        
        // Verificar los nuevos campos en el DTO
        assertNotNull(planDTO.getPrecioPlan());
        assertNotNull(planDTO.getHorasDia());
        assertTrue(planDTO.getPrecioPlan() > 0);
        assertTrue(planDTO.getHorasDia() > 0);

        assertNotNull(planDTO.getConfiguracionDescuento());
        assertEquals(15.00, planDTO.getConfiguracionDescuento().getDescuentoMensual());
        assertEquals(5.00, planDTO.getConfiguracionDescuento().getDescuentoAnualAdicional());
    }

    @Test
    void testCalcularHorasDias_DifferentDayConfigurations_Integration() {
        // Test con diferentes configuraciones de días para verificar el cálculo
        
        // Test 1: 5 días laborales (L-M-X-J-V)
        NuevoPlanDTO plan5Dias = new NuevoPlanDTO();
        plan5Dias.setIdEmpresa(testEmpresa.getIdEmpresa());
        plan5Dias.setNombrePlan("WORKWEEK");
        plan5Dias.setCodigoPlan("WW-5DIAS");
        plan5Dias.setDescripcion("Plan 5 días");
        plan5Dias.setPrecioPlan(200.00);
        plan5Dias.setHorasMensuales(173); // Horas típicas mensuales
        plan5Dias.setDiasAplicables("L-M-X-J-V"); // 5 días
        plan5Dias.setCoberturaHoraria("08:00 - 17:00");
        plan5Dias.setDescuentoMensual(15.00);
        plan5Dias.setDescuentoAnualAdicional(5.00);
        plan5Dias.setFechaVigenciaInicio("2024-01-01");
        plan5Dias.setFechaVigenciaFin("2024-12-31");
        plan5Dias.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        String result = planesSuscripcionService.crearNuevoPlanSuscripcion(plan5Dias);
        assertTrue(result.contains("éxito"));

        // Verificar el plan creado
        List<TipoPlan> planes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        TipoPlan planCreado = planes.stream()
                .filter(p -> "WW-5DIAS".equals(p.getCodigoPlan()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(planCreado);
        assertNotNull(planCreado.getHorasDia());
        // Para 173 horas / (4.33 semanas * 5 días) ≈ 8 horas/día
        assertTrue(planCreado.getHorasDia() >= 7 && planCreado.getHorasDia() <= 9);

        // Test 2: 7 días completos (L-M-X-J-V-S-D)
        // Marcar el plan anterior como histórico para evitar duplicados
        planCreado.setActivo(TipoPlan.EstadoConfiguracion.HISTORICO);
        tipoPlanRepository.save(planCreado);

        NuevoPlanDTO plan7Dias = new NuevoPlanDTO();
        plan7Dias.setIdEmpresa(testEmpresa.getIdEmpresa());
        plan7Dias.setNombrePlan("FULL_ACCESS");
        plan7Dias.setCodigoPlan("FA-7DIAS");
        plan7Dias.setDescripcion("Plan 7 días");
        plan7Dias.setPrecioPlan(400.00);
        plan7Dias.setHorasMensuales(210); // Más horas para 7 días
        plan7Dias.setDiasAplicables("L-M-X-J-V-S-D"); // 7 días
        plan7Dias.setCoberturaHoraria("00:00 - 23:59");
        plan7Dias.setDescuentoMensual(25.00);
        plan7Dias.setDescuentoAnualAdicional(10.00);
        plan7Dias.setFechaVigenciaInicio("2024-01-01");
        plan7Dias.setFechaVigenciaFin("2024-12-31");
        plan7Dias.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        result = planesSuscripcionService.crearNuevoPlanSuscripcion(plan7Dias);
        assertTrue(result.contains("éxito"));

        // Verificar el segundo plan
        planes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        TipoPlan plan7DiasCreado = planes.stream()
                .filter(p -> "FA-7DIAS".equals(p.getCodigoPlan()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(plan7DiasCreado);
        assertNotNull(plan7DiasCreado.getHorasDia());
        // Para 210 horas / (4.33 semanas * 7 días) ≈ 7 horas/día
        assertTrue(plan7DiasCreado.getHorasDia() >= 6 && plan7DiasCreado.getHorasDia() <= 8);
    }

    private TipoPlan createTestTipoPlan() {
        TipoPlan tipoPlan = new TipoPlan();
        tipoPlan.setEmpresa(testEmpresa);
        tipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        tipoPlan.setCodigoPlan("WW-001");
        tipoPlan.setDescripcion("Plan Workweek");
        tipoPlan.setPrecioPlan(300.00); // Nuevo campo
        tipoPlan.setHorasMensuales(160);
        tipoPlan.setHorasDia(8); // Nuevo campo - calculado para 5 días
        tipoPlan.setDiasAplicables("L-M-X-J-V");
        tipoPlan.setCoberturaHoraria("08:00 - 18:00");
        tipoPlan.setOrdenBeneficio(2);
        tipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        return tipoPlanRepository.save(tipoPlan);
    }

    private ConfiguracionDescuentoPlan createTestConfigDescuento(TipoPlan tipoPlan) {
        ConfiguracionDescuentoPlan config = new ConfiguracionDescuentoPlan();
        config.setTipoPlan(tipoPlan);
        config.setDescuentoMensual(new BigDecimal("15.00"));
        config.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        config.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        config.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        config.setCreadoPor(testUsuario);
        config.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        return configuracionDescuentoPlanRepository.save(config);
    }

    private String generateUniqueDpi() {
        // Usar nanoTime para mayor unicidad
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
