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
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("Lunes a Viernes");
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

        // Verify plan was created
        List<TipoPlan> planes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, planes.size());

        TipoPlan createdPlan = planes.get(0);
        assertEquals(TipoPlan.NombrePlan.WORKWEEK, createdPlan.getNombrePlan());
        assertEquals("WW-001", createdPlan.getCodigoPlan());
        assertEquals("Plan Workweek para uso de lunes a viernes", createdPlan.getDescripcion());
        assertEquals(160, createdPlan.getHorasMensuales());
        assertEquals(2, createdPlan.getOrdenBeneficio()); // WORKWEEK order

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
        // Test creating all plan types
        String[] planNames = {"FULL_ACCESS", "WORKWEEK", "OFFICE_LIGHT", "DIARIO_FLEXIBLE", "NOCTURNO"};
        double[] monthlyDiscounts = {25.00, 20.00, 15.00, 10.00, 5.00};
        double[] annualDiscounts = {10.00, 8.00, 6.00, 4.00, 2.00};

        for (int i = 0; i < planNames.length; i++) {
            NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
            nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
            nuevoPlanDTO.setNombrePlan(planNames[i]);
            nuevoPlanDTO.setCodigoPlan(planNames[i] + "-001");
            nuevoPlanDTO.setDescripcion("Plan " + planNames[i]);
            nuevoPlanDTO.setHorasMensuales(100 + (i * 20));
            nuevoPlanDTO.setDiasAplicables("Test");
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

        // Verify all plans were created
        List<TipoPlan> allPlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(5, allPlans.size());

        // Verify order benefit is correct
        for (TipoPlan plan : allPlans) {
            switch (plan.getNombrePlan()) {
                case FULL_ACCESS -> assertEquals(1, plan.getOrdenBeneficio());
                case WORKWEEK -> assertEquals(2, plan.getOrdenBeneficio());
                case OFFICE_LIGHT -> assertEquals(3, plan.getOrdenBeneficio());
                case DIARIO_FLEXIBLE -> assertEquals(4, plan.getOrdenBeneficio());
                case NOCTURNO -> assertEquals(5, plan.getOrdenBeneficio());
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
        editarPlanDTO.setHorasMensuales(180);
        editarPlanDTO.setDiasAplicables("Lunes a Viernes");
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

        // Verify new plan was created
        List<TipoPlan> vigentePlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, vigentePlans.size());

        TipoPlan newPlan = vigentePlans.get(0);
        assertEquals("WW-002", newPlan.getCodigoPlan());
        assertEquals("Plan Workweek actualizado", newPlan.getDescripcion());
        assertEquals(180, newPlan.getHorasMensuales());

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

        assertNotNull(planDTO.getConfiguracionDescuento());
        assertEquals(15.00, planDTO.getConfiguracionDescuento().getDescuentoMensual());
        assertEquals(5.00, planDTO.getConfiguracionDescuento().getDescuentoAnualAdicional());
    }

    @Test
    void testCrearNuevoPlanSuscripcion_DuplicateName_Integration() {
        // Arrange - Create first plan
        createTestTipoPlan();

        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK"); // Same name as existing
        nuevoPlanDTO.setCodigoPlan("WW-002");
        nuevoPlanDTO.setDescripcion("Another plan");
        nuevoPlanDTO.setHorasMensuales(140);
        nuevoPlanDTO.setDiasAplicables("Test");
        nuevoPlanDTO.setCoberturaHoraria("09:00 - 17:00");
        nuevoPlanDTO.setDescuentoMensual(12.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(4.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });
        assertTrue(exception.getMessage().contains("Ya existe un plan con el nombre WORKWEEK"));
    }

    @Test
    void testValidarPorcentajesDescuento_InvalidPercentages_Integration() {
        // Arrange - Create FULL_ACCESS plan first (order 1)
        TipoPlan fullAccessPlan = new TipoPlan();
        fullAccessPlan.setEmpresa(testEmpresa);
        fullAccessPlan.setNombrePlan(TipoPlan.NombrePlan.FULL_ACCESS);
        fullAccessPlan.setCodigoPlan("FA-001");
        fullAccessPlan.setDescripcion("Full Access Plan");
        fullAccessPlan.setHorasMensuales(300);
        fullAccessPlan.setDiasAplicables("Todos los días");
        fullAccessPlan.setCoberturaHoraria("00:00 - 23:59");
        fullAccessPlan.setOrdenBeneficio(1);
        fullAccessPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        fullAccessPlan = tipoPlanRepository.save(fullAccessPlan);

        ConfiguracionDescuentoPlan fullAccessConfig = new ConfiguracionDescuentoPlan();
        fullAccessConfig.setTipoPlan(fullAccessPlan);
        fullAccessConfig.setDescuentoMensual(new BigDecimal("25.00"));
        fullAccessConfig.setDescuentoAnualAdicional(new BigDecimal("10.00"));
        fullAccessConfig.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        fullAccessConfig.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        fullAccessConfig.setCreadoPor(testUsuario);
        fullAccessConfig.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(fullAccessConfig);

        // Try to create WORKWEEK (order 2) with higher percentage than FULL_ACCESS
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Workweek Plan");
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("Lunes a Viernes");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(30.00); // Higher than FULL_ACCESS (invalid)
        nuevoPlanDTO.setDescuentoAnualAdicional(12.00); // Higher than FULL_ACCESS (invalid)
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        });
        assertTrue(exception.getMessage().contains("Los porcentajes de descuento no cumplen"));
    }

    @Test
    void testCompleteWorkflow_Integration() {
        // Test complete workflow: create, get, edit plans

        // Step 1: Create a plan
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan inicial");
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("Lunes a Viernes");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(15.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(5.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        String createResult = planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
        assertTrue(createResult.contains("éxito"));

        // Step 2: Get plans
        List<DetalleTipoPlanDTO> plans = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(testUsuario.getIdUsuario());
        assertEquals(1, plans.size());

        DetalleTipoPlanDTO createdPlan = plans.get(0);
        assertEquals("WORKWEEK", createdPlan.getNombrePlan());
        assertEquals(15.00, createdPlan.getConfiguracionDescuento().getDescuentoMensual());

        // Step 3: Edit the plan
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(createdPlan.getId());
        editarPlanDTO.setNombrePlan("WORKWEEK");
        editarPlanDTO.setCodigoPlan("WW-002");
        editarPlanDTO.setDescripcion("Plan actualizado");
        editarPlanDTO.setHorasMensuales(180);
        editarPlanDTO.setDiasAplicables("Lunes a Viernes");
        editarPlanDTO.setCoberturaHoraria("07:00 - 19:00");
        editarPlanDTO.setDescuentoMensual(18.00);
        editarPlanDTO.setDescuentoAnualAdicional(7.00);
        editarPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editarPlanDTO.setFechaVigenciaFin("2024-12-31");
        editarPlanDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        String editResult = planesSuscripcionService.editarPlanSuscripcion(editarPlanDTO);
        assertTrue(editResult.contains("éxito"));

        // Step 4: Verify changes
        List<DetalleTipoPlanDTO> updatedPlans = planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(testUsuario.getIdUsuario());
        assertEquals(1, updatedPlans.size());

        DetalleTipoPlanDTO updatedPlan = updatedPlans.get(0);
        assertEquals("WW-002", updatedPlan.getCodigoPlan());
        assertEquals("Plan actualizado", updatedPlan.getDescripcion());
        assertEquals(180, updatedPlan.getHorasMensuales());
        assertEquals(18.00, updatedPlan.getConfiguracionDescuento().getDescuentoMensual());

        // Verify bitacoras
        List<BitacoraConfiguracionDescuento> bitacoras = bitacoraConfiguracionDescuentoRepository.findAll();
        assertEquals(2, bitacoras.size()); // Creation + Update
    }

    @Test
    void testCrearNuevoPlanSuscripcion_EdgeCaseValidations_Integration() {
        // Test edge cases for percentage validation
        
        // Create FULL_ACCESS plan first
        NuevoPlanDTO fullAccessDTO = new NuevoPlanDTO();
        fullAccessDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        fullAccessDTO.setNombrePlan("FULL_ACCESS");
        fullAccessDTO.setCodigoPlan("FA-001");
        fullAccessDTO.setDescripcion("Full Access Plan");
        fullAccessDTO.setHorasMensuales(300);
        fullAccessDTO.setDiasAplicables("Todos los días");
        fullAccessDTO.setCoberturaHoraria("00:00 - 23:59");
        fullAccessDTO.setDescuentoMensual(30.00);
        fullAccessDTO.setDescuentoAnualAdicional(12.00);
        fullAccessDTO.setFechaVigenciaInicio("2024-01-01");
        fullAccessDTO.setFechaVigenciaFin("2024-12-31");
        fullAccessDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        String result1 = planesSuscripcionService.crearNuevoPlanSuscripcion(fullAccessDTO);
        assertTrue(result1.contains("éxito"));

        // Try to create WORKWEEK with exactly same percentages (should fail)
        NuevoPlanDTO workweekDTO = new NuevoPlanDTO();
        workweekDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        workweekDTO.setNombrePlan("WORKWEEK");
        workweekDTO.setCodigoPlan("WW-001");
        workweekDTO.setDescripcion("Workweek Plan");
        workweekDTO.setHorasMensuales(160);
        workweekDTO.setDiasAplicables("Lunes a Viernes");
        workweekDTO.setCoberturaHoraria("08:00 - 18:00");
        workweekDTO.setDescuentoMensual(30.00); // Same as FULL_ACCESS (should fail)
        workweekDTO.setDescuentoAnualAdicional(12.00); // Same as FULL_ACCESS (should fail)
        workweekDTO.setFechaVigenciaInicio("2024-01-01");
        workweekDTO.setFechaVigenciaFin("2024-12-31");
        workweekDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planesSuscripcionService.crearNuevoPlanSuscripcion(workweekDTO); // Corregir aquí
        });
        assertTrue(exception.getMessage().contains("Los porcentajes de descuento no cumplen"));
    }

    @Test
    void testObtenerPlanesSuscripcionPorEmpresa_WithNullFechaFin_Integration() {
        // Create a plan and then manually update config to have null fechaVigenciaFin
        TipoPlan plan = createTestTipoPlan();
        ConfiguracionDescuentoPlan config = createTestConfigDescuento(plan);
        
        // Update config to have null end date
        config.setFechaVigenciaFin(null);
        configuracionDescuentoPlanRepository.save(config);

        // Act & Assert - Should throw NullPointerException
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(testUsuario.getIdUsuario());
        });

        // The service will fail when trying to convert null to string
        assertNotNull(exception);
    }

    @Test
    void testValidarPorcentajesDescuento_BoundaryConditions_Integration() {
        // Test boundary conditions for percentage validation
        
        // Create FULL_ACCESS plan
        NuevoPlanDTO fullAccessDTO = new NuevoPlanDTO();
        fullAccessDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        fullAccessDTO.setNombrePlan("FULL_ACCESS");
        fullAccessDTO.setCodigoPlan("FA-001");
        fullAccessDTO.setDescripcion("Full Access Plan");
        fullAccessDTO.setHorasMensuales(300);
        fullAccessDTO.setDiasAplicables("Todos los días");
        fullAccessDTO.setCoberturaHoraria("00:00 - 23:59");
        fullAccessDTO.setDescuentoMensual(25.00);
        fullAccessDTO.setDescuentoAnualAdicional(10.00);
        fullAccessDTO.setFechaVigenciaInicio("2024-01-01");
        fullAccessDTO.setFechaVigenciaFin("2024-12-31");
        fullAccessDTO.setIdUsuarioCreacion(testUsuario.getIdUsuario());

        planesSuscripcionService.crearNuevoPlanSuscripcion(fullAccessDTO);

        // Test exact boundary values (should fail) - WORKWEEK no puede tener los mismos porcentajes que FULL_ACCESS
        boolean result1 = planesSuscripcionService.validarPorcentajesDescuento(25.00, "WORKWEEK", 10.00, testEmpresa.getIdEmpresa());
        assertFalse(result1); // Exact same values should fail

        // Test values that violate the rule - WORKWEEK debe tener MENORES porcentajes que FULL_ACCESS
        boolean result2 = planesSuscripcionService.validarPorcentajesDescuento(30.00, "WORKWEEK", 12.00, testEmpresa.getIdEmpresa());
        assertFalse(result2); // Higher values should fail for lower benefit plan

        // Test valid values - WORKWEEK debe tener MENORES porcentajes que FULL_ACCESS
        boolean result3 = planesSuscripcionService.validarPorcentajesDescuento(20.00, "WORKWEEK", 8.00, testEmpresa.getIdEmpresa());
        assertTrue(result3); // Valid range should pass: FULL_ACCESS(25%) > WORKWEEK(20%)
    }

    private TipoPlan createTestTipoPlan() {
        TipoPlan tipoPlan = new TipoPlan();
        tipoPlan.setEmpresa(testEmpresa);
        tipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        tipoPlan.setCodigoPlan("WW-001");
        tipoPlan.setDescripcion("Plan Workweek");
        tipoPlan.setHorasMensuales(160);
        tipoPlan.setDiasAplicables("Lunes a Viernes");
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
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
