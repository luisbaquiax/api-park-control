package org.parkcontrol.apiparkcontrol.controllers.planes_suscripcion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.NuevoPlanDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PlanesSuscripcionControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories for setup
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Autowired
    private BitacoraConfiguracionDescuentoRepository bitacoraConfiguracionDescuentoRepository;

    // Test entities
    private Empresa testEmpresa;
    private Usuario testUsuarioEmpresa;
    private Rol empresaRol;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean database
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

        // Create test persona and usuario empresa
        Persona personaEmpresa = new Persona();
        personaEmpresa.setNombre("Admin");
        personaEmpresa.setApellido("Empresa");
        personaEmpresa.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        personaEmpresa.setDpi(generateUniqueDpi());
        personaEmpresa.setCorreo("admin@empresa.com");
        personaEmpresa.setTelefono("12345678");
        personaEmpresa.setDireccionCompleta("Admin Address");
        personaEmpresa.setCiudad("Guatemala");
        personaEmpresa.setPais("Guatemala");
        personaEmpresa.setCodigoPostal("01001");
        personaEmpresa.setEstado(Persona.Estado.ACTIVO);
        personaEmpresa = personaRepository.save(personaEmpresa);

        testUsuarioEmpresa = new Usuario();
        testUsuarioEmpresa.setPersona(personaEmpresa);
        testUsuarioEmpresa.setRol(empresaRol);
        testUsuarioEmpresa.setNombreUsuario("adminempresa");
        testUsuarioEmpresa.setContraseniaHash("hashedPassword");
        testUsuarioEmpresa.setDobleFactorHabilitado(false);
        testUsuarioEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioEmpresa.setDebeCambiarContrasenia(false);
        testUsuarioEmpresa.setEsPrimeraVez(false);
        testUsuarioEmpresa.setIntentosFallidos(0);
        testUsuarioEmpresa = usuarioRepository.save(testUsuarioEmpresa);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testUsuarioEmpresa);
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
    void testGetPlanesPorEmpresa_EmptyList_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", testUsuarioEmpresa.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetPlanesPorEmpresa_WithPlanes_Integration() throws Exception {
        // Arrange - Create test plan
        TipoPlan testPlan = createTestTipoPlan();
        ConfiguracionDescuentoPlan testConfig = createTestConfigDescuento(testPlan);

        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", testUsuarioEmpresa.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testPlan.getId().intValue())))
                .andExpect(jsonPath("$[0].idEmpresa", is(testEmpresa.getIdEmpresa().intValue())))
                .andExpect(jsonPath("$[0].nombrePlan", is("WORKWEEK")))
                .andExpect(jsonPath("$[0].codigoPlan", is("WW-001")))
                .andExpect(jsonPath("$[0].descripcion", is("Plan Workweek")))
                .andExpect(jsonPath("$[0].precioPlan", is(300.0)))
                .andExpect(jsonPath("$[0].horasDia", is(8)))
                .andExpect(jsonPath("$[0].horasMensuales", is(160)))
                .andExpect(jsonPath("$[0].diasAplicables", is("L-M-X-J-V")))
                .andExpect(jsonPath("$[0].coberturaHoraria", is("08:00 - 18:00")))
                .andExpect(jsonPath("$[0].ordenBeneficio", is(2)))
                .andExpect(jsonPath("$[0].activo", is("VIGENTE")))
                .andExpect(jsonPath("$[0].configuracionDescuento.descuentoMensual", is(15.0)))
                .andExpect(jsonPath("$[0].configuracionDescuento.descuentoAnualAdicional", is(5.0)));
    }

    @Test
    void testGetPlanesPorEmpresa_EmpresaNotFound_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Empresa no encontrada para el usuario con ID: 999")));
    }

    @Test
    void testCrearNuevoPlan_Success_Integration() throws Exception {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek para uso de lunes a viernes");
        nuevoPlanDTO.setPrecioPlan(300.00);
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(15.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(5.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPlanDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nuevo plan de suscripción creado con éxito para la empresa con ID: " + testEmpresa.getIdEmpresa())));

        // Verify the plan was created
        List<TipoPlan> planes = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, planes.size());
        assertEquals("WW-001", planes.get(0).getCodigoPlan());
    }

    @Test
    void testCrearNuevoPlan_EmpresaNotFound_Integration() throws Exception {
        // Arrange
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(999L);
        nuevoPlanDTO.setNombrePlan("WORKWEEK");

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Empresa no encontrada con ID: 999")));
    }

    @Test
    void testCrearNuevoPlan_DuplicatedPlan_Integration() throws Exception {
        // Arrange - First create a plan
        TipoPlan existingPlan = createTestTipoPlan();

        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK"); // Same name as existing plan
        nuevoPlanDTO.setCodigoPlan("WW-002");
        nuevoPlanDTO.setDescripcion("Duplicate plan");
        nuevoPlanDTO.setPrecioPlan(250.00);
        nuevoPlanDTO.setHorasMensuales(140);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
        nuevoPlanDTO.setCoberturaHoraria("09:00 - 17:00");
        nuevoPlanDTO.setDescuentoMensual(12.00);
        nuevoPlanDTO.setDescuentoAnualAdicional(4.00);
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Ya existe un plan con el nombre WORKWEEK")));
    }

    @Test
    void testEditarPlanSuscripcion_Success_Integration() throws Exception {
        // Arrange - Create original plan
        TipoPlan originalPlan = createTestTipoPlan();
        ConfiguracionDescuentoPlan originalConfig = createTestConfigDescuento(originalPlan);

        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(originalPlan.getId());
        editarPlanDTO.setNombrePlan("WORKWEEK");
        editarPlanDTO.setCodigoPlan("WW-002");
        editarPlanDTO.setDescripcion("Plan Workweek actualizado");
        editarPlanDTO.setPrecioPlan(350.00);
        editarPlanDTO.setHorasMensuales(180);
        editarPlanDTO.setDiasAplicables("L-M-X-J-V");
        editarPlanDTO.setCoberturaHoraria("07:00 - 19:00");
        editarPlanDTO.setDescuentoMensual(18.00);
        editarPlanDTO.setDescuentoAnualAdicional(7.00);
        editarPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editarPlanDTO.setFechaVigenciaFin("2024-12-31");
        editarPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarPlanDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Plan de suscripción editado con éxito para la empresa con ID: " + testEmpresa.getIdEmpresa())));

        // Verify original plan is now historic
        TipoPlan updatedOriginal = tipoPlanRepository.findById(originalPlan.getId()).orElse(null);
        assertNotNull(updatedOriginal);
        assertEquals(TipoPlan.EstadoConfiguracion.HISTORICO, updatedOriginal.getActivo());

        // Verify new plan was created
        List<TipoPlan> vigentePlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(1, vigentePlans.size());
        assertEquals("WW-002", vigentePlans.get(0).getCodigoPlan());
    }

    @Test
    void testEditarPlanSuscripcion_PlanNotFound_Integration() throws Exception {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(999L);
        editarPlanDTO.setNombrePlan("WORKWEEK");

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Tipo de plan no encontrado con ID: 999")));
    }

    @Test
    void testCrearNuevoPlan_AllPlanTypes_Integration() throws Exception {
        // Test creating all plan types
        String[] planTypes = {"FULL_ACCESS", "WORKWEEK", "OFFICE_LIGHT", "DIARIO_FLEXIBLE", "NOCTURNO"};
        double[] monthlyDiscounts = {25.00, 20.00, 15.00, 10.00, 5.00};
        double[] annualDiscounts = {10.00, 8.00, 6.00, 4.00, 2.00};
        double[] precios = {500.00, 350.00, 250.00, 150.00, 100.00};

        for (int i = 0; i < planTypes.length; i++) {
            NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
            nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
            nuevoPlanDTO.setNombrePlan(planTypes[i]);
            nuevoPlanDTO.setCodigoPlan(planTypes[i] + "-001");
            nuevoPlanDTO.setDescripcion("Plan " + planTypes[i]);
            nuevoPlanDTO.setPrecioPlan(precios[i]);
            nuevoPlanDTO.setHorasMensuales(200 - i * 20); // Decreasing hours
            nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
            nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
            nuevoPlanDTO.setDescuentoMensual(monthlyDiscounts[i]);
            nuevoPlanDTO.setDescuentoAnualAdicional(annualDiscounts[i]);
            nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
            nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
            nuevoPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

            mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nuevoPlanDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("success")));
        }

        // Verify all plans were created
        List<TipoPlan> allPlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        assertEquals(5, allPlans.size());
    }

    @Test
    void testCompleteWorkflow_CreateAndEditPlan_Integration() throws Exception {
        // Step 1: Create a plan
        NuevoPlanDTO createPlanDTO = new NuevoPlanDTO();
        createPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        createPlanDTO.setNombrePlan("OFFICE_LIGHT");
        createPlanDTO.setCodigoPlan("OL-001");
        createPlanDTO.setDescripcion("Plan Office Light");
        createPlanDTO.setPrecioPlan(200.00);
        createPlanDTO.setHorasMensuales(120);
        createPlanDTO.setDiasAplicables("L-M-X-J-V");
        createPlanDTO.setCoberturaHoraria("09:00 - 17:00");
        createPlanDTO.setDescuentoMensual(12.00);
        createPlanDTO.setDescuentoAnualAdicional(4.00);
        createPlanDTO.setFechaVigenciaInicio("2024-01-01");
        createPlanDTO.setFechaVigenciaFin("2024-12-31");
        createPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPlanDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // Step 2: Get the created plan
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", testUsuarioEmpresa.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoPlan", is("OL-001")))
                .andExpect(jsonPath("$[0].precioPlan", is(200.0)));

        // Step 3: Edit the plan
        List<TipoPlan> createdPlans = tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(
                testEmpresa.getIdEmpresa(), TipoPlan.EstadoConfiguracion.VIGENTE);
        Long planId = createdPlans.get(0).getId();

        NuevoPlanDTO editPlanDTO = new NuevoPlanDTO();
        editPlanDTO.setIdTipoPlan(planId);
        editPlanDTO.setNombrePlan("OFFICE_LIGHT");
        editPlanDTO.setCodigoPlan("OL-002");
        editPlanDTO.setDescripcion("Plan Office Light actualizado");
        editPlanDTO.setPrecioPlan(220.00);
        editPlanDTO.setHorasMensuales(130);
        editPlanDTO.setDiasAplicables("L-M-X-J-V");
        editPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        editPlanDTO.setDescuentoMensual(13.00);
        editPlanDTO.setDescuentoAnualAdicional(5.00);
        editPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editPlanDTO.setFechaVigenciaFin("2024-12-31");
        editPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editPlanDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // Step 4: Verify the edited plan
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", testUsuarioEmpresa.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoPlan", is("OL-002")))
                .andExpect(jsonPath("$[0].precioPlan", is(220.0)))
                .andExpect(jsonPath("$[0].descripcion", is("Plan Office Light actualizado")));
    }

    @Test
    void testCrearNuevoPlan_InvalidValidation_Integration() throws Exception {
        // Create FULL_ACCESS first (highest benefit)
        TipoPlan fullAccessPlan = new TipoPlan();
        fullAccessPlan.setEmpresa(testEmpresa);
        fullAccessPlan.setNombrePlan(TipoPlan.NombrePlan.FULL_ACCESS);
        fullAccessPlan.setCodigoPlan("FA-001");
        fullAccessPlan.setDescripcion("Full Access Plan");
        fullAccessPlan.setPrecioPlan(500.00);
        fullAccessPlan.setHorasDia(12);
        fullAccessPlan.setHorasMensuales(250);
        fullAccessPlan.setDiasAplicables("L-M-X-J-V-S-D");
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
        fullAccessConfig.setCreadoPor(testUsuarioEmpresa);
        fullAccessConfig.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(fullAccessConfig);

        // Try to create WORKWEEK with higher discounts (should fail)
        NuevoPlanDTO nuevoPlanDTO = new NuevoPlanDTO();
        nuevoPlanDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevoPlanDTO.setNombrePlan("WORKWEEK");
        nuevoPlanDTO.setCodigoPlan("WW-001");
        nuevoPlanDTO.setDescripcion("Plan Workweek");
        nuevoPlanDTO.setPrecioPlan(300.00);
        nuevoPlanDTO.setHorasMensuales(160);
        nuevoPlanDTO.setDiasAplicables("L-M-X-J-V");
        nuevoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        nuevoPlanDTO.setDescuentoMensual(30.00); // Higher than FULL_ACCESS
        nuevoPlanDTO.setDescuentoAnualAdicional(15.00); // Higher than FULL_ACCESS
        nuevoPlanDTO.setFechaVigenciaInicio("2024-01-01");
        nuevoPlanDTO.setFechaVigenciaFin("2024-12-31");
        nuevoPlanDTO.setIdUsuarioCreacion(testUsuarioEmpresa.getIdUsuario());

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Los porcentajes de descuento no cumplen con las reglas de negocio")));
    }

    private TipoPlan createTestTipoPlan() {
        TipoPlan tipoPlan = new TipoPlan();
        tipoPlan.setEmpresa(testEmpresa);
        tipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        tipoPlan.setCodigoPlan("WW-001");
        tipoPlan.setDescripcion("Plan Workweek");
        tipoPlan.setPrecioPlan(300.00);
        tipoPlan.setHorasDia(8);
        tipoPlan.setHorasMensuales(160);
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
        config.setCreadoPor(testUsuarioEmpresa);
        config.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        return configuracionDescuentoPlanRepository.save(config);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
