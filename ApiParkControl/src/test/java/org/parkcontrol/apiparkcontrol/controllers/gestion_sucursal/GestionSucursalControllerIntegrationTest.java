package org.parkcontrol.apiparkcontrol.controllers.gestion_sucursal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
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
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class GestionSucursalControllerIntegrationTest {

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
    private SucursalRepository sucursalRepository;
    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private BitacoraTarifaSucursalRepository bitacoraTarifaSucursalRepository;

    // Test entities
    private Usuario testUsuarioEmpresa;
    private Usuario testUsuarioSucursal;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TarifaBase testTarifaBase;
    private Rol empresaRol;
    private Rol sucursalRol;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean database
        bitacoraTarifaSucursalRepository.deleteAll();
        tarifaSucursalRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test roles
        empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario de empresa");
        empresaRol = rolRepository.save(empresaRol);

        sucursalRol = new Rol();
        sucursalRol.setNombreRol("SUCURSAL");
        sucursalRol.setDescripcion("Usuario de sucursal");
        sucursalRol = rolRepository.save(sucursalRol);

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

        // Create test tarifa base
        testTarifaBase = new TarifaBase();
        testTarifaBase.setEmpresa(testEmpresa);
        testTarifaBase.setPrecioPorHora(new BigDecimal("10.00"));
        testTarifaBase.setMoneda("GTQ");
        testTarifaBase.setFechaVigenciaInicio(LocalDate.from(LocalDateTime.now().minusDays(30)));
        testTarifaBase.setFechaVigenciaFin(LocalDate.from(LocalDateTime.now().plusDays(330)));
        testTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        testTarifaBase = tarifaBaseRepository.save(testTarifaBase);

        // Create test persona and usuario sucursal
        Persona personaSucursal = new Persona();
        personaSucursal.setNombre("Usuario");
        personaSucursal.setApellido("Sucursal");
        personaSucursal.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        personaSucursal.setDpi(generateUniqueDpi());
        personaSucursal.setCorreo("sucursal@test.com");
        personaSucursal.setTelefono("87654321");
        personaSucursal.setDireccionCompleta("Sucursal Address");
        personaSucursal.setCiudad("Guatemala");
        personaSucursal.setPais("Guatemala");
        personaSucursal.setCodigoPostal("01002");
        personaSucursal.setEstado(Persona.Estado.ACTIVO);
        personaSucursal = personaRepository.save(personaSucursal);

        testUsuarioSucursal = new Usuario();
        testUsuarioSucursal.setPersona(personaSucursal);
        testUsuarioSucursal.setRol(sucursalRol);
        testUsuarioSucursal.setNombreUsuario("usersucursal");
        testUsuarioSucursal.setContraseniaHash("hashedPassword");
        testUsuarioSucursal.setDobleFactorHabilitado(false);
        testUsuarioSucursal.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioSucursal.setDebeCambiarContrasenia(true);
        testUsuarioSucursal.setEsPrimeraVez(true);
        testUsuarioSucursal.setIntentosFallidos(0);
        testUsuarioSucursal = usuarioRepository.save(testUsuarioSucursal);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuarioSucursal);
        testSucursal.setNombre("Sucursal Test");
        testSucursal.setDireccionCompleta("Avenida Test 123");
        testSucursal.setCiudad("Guatemala");
        testSucursal.setDepartamento("Guatemala");
        testSucursal.setLatitud(new BigDecimal("14.6349"));
        testSucursal.setLongitud(new BigDecimal("-90.5069"));
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(100);
        testSucursal.setTelefonoContacto("22334455");
        testSucursal.setCorreoContacto("test@sucursal.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);
    }

    @Test
    void testGetMiSucursal_Success_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idSucursal", is(testSucursal.getIdSucursal().intValue())))
                .andExpect(jsonPath("$.nombreSucursal", is("Sucursal Test")))
                .andExpect(jsonPath("$.direccionCompletaSucursal", is("Avenida Test 123")))
                .andExpect(jsonPath("$.ciudadSucursal", is("Guatemala")))
                .andExpect(jsonPath("$.departamentoSucursal", is("Guatemala")))
                .andExpect(jsonPath("$.horaApertura", is("08:00")))
                .andExpect(jsonPath("$.horaCierre", is("18:00")))
                .andExpect(jsonPath("$.capacidad2Ruedas", is(50)))
                .andExpect(jsonPath("$.capacidad4Ruedas", is(100)))
                .andExpect(jsonPath("$.latitud", is(14.6349)))
                .andExpect(jsonPath("$.longitud", is(-90.5069)))
                .andExpect(jsonPath("$.telefonoContactoSucursal", is("22334455")))
                .andExpect(jsonPath("$.correoContactoSucursal", is("test@sucursal.com")))
                .andExpect(jsonPath("$.estadoSucursal", is("ACTIVA")))
                .andExpect(jsonPath("$.empresa.idEmpresa", is(testEmpresa.getIdEmpresa().intValue())))
                .andExpect(jsonPath("$.empresa.nombreComercial", is("Test Company")))
                .andExpect(jsonPath("$.empresa.nit", is("1234567-8")));
    }

    @Test
    void testGetMiSucursal_UsuarioNotFound_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada para el usuario con ID: 999")));
    }

    @Test
    void testEditarMiSucursal_Success_Integration() throws Exception {
        // Arrange
        EditarSucursalDTO editarDTO = new EditarSucursalDTO();
        editarDTO.setIdSucursal(testSucursal.getIdSucursal());
        editarDTO.setNombreSucursal("Sucursal Test Actualizada");
        editarDTO.setDireccionCompletaSucursal("Nueva Avenida 456");
        editarDTO.setCiudadSucursal("Antigua");
        editarDTO.setDepartamentoSucursal("Sacatepéquez");
        editarDTO.setHoraApertura("07:00");
        editarDTO.setHoraCierre("19:00");
        editarDTO.setCapacidad2Ruedas(60);
        editarDTO.setCapacidad4Ruedas(120);
        editarDTO.setLatitud(14.5607);
        editarDTO.setLongitud(-90.7346);
        editarDTO.setTelefonoContactoSucursal("33445566");
        editarDTO.setCorreoContactoSucursal("nueva@sucursal.com");
        editarDTO.setEstadoSucursal("ACTIVA");

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Sucursal editada exitosamente.")));

        // Verify the sucursal was actually updated
        Sucursal updatedSucursal = sucursalRepository.findById(testSucursal.getIdSucursal()).orElse(null);
        assertNotNull(updatedSucursal);
        assertEquals("Sucursal Test Actualizada", updatedSucursal.getNombre());
        assertEquals("Nueva Avenida 456", updatedSucursal.getDireccionCompleta());
        assertEquals("Antigua", updatedSucursal.getCiudad());
        assertEquals("Sacatepéquez", updatedSucursal.getDepartamento());
        assertEquals(LocalTime.of(7, 0), updatedSucursal.getHoraApertura());
        assertEquals(LocalTime.of(19, 0), updatedSucursal.getHoraCierre());
        assertEquals(60, updatedSucursal.getCapacidad2Ruedas());
        assertEquals(120, updatedSucursal.getCapacidad4Ruedas());
        assertEquals("33445566", updatedSucursal.getTelefonoContacto());
        assertEquals("nueva@sucursal.com", updatedSucursal.getCorreoContacto());
    }

    @Test
    void testEditarMiSucursal_SucursalNotFound_Integration() throws Exception {
        // Arrange
        EditarSucursalDTO editarDTO = new EditarSucursalDTO();
        editarDTO.setIdSucursal(999L);
        editarDTO.setNombreSucursal("No Existe");

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada con ID: 999")));
    }

    @Test
    void testCrearNuevaTarifaSucursal_Success_Integration() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        nuevaTarifaDTO.setPrecioPorHora("15.00");
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin("2024-12-31");
        nuevaTarifaDTO.setEsTarifaBase(false);

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaTarifaDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nueva tarifa de sucursal creada exitosamente.")));

        // Verify the tarifa was actually created
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());
        assertEquals(new BigDecimal("15.00"), tarifas.get(0).getPrecioPorHora());
        assertEquals("GTQ", tarifas.get(0).getMoneda());
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.VIGENTE, tarifas.get(0).getEstado());

        // Verify bitacora was created
        List<BitacoraTarifaSucursal> bitacoras = bitacoraTarifaSucursalRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaSucursal.Accion.CREACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testCrearNuevaTarifaSucursal_TarifaBase_Integration() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO tarifaBaseDTO = new NuevaTarifaSucursalDTO();
        tarifaBaseDTO.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        tarifaBaseDTO.setPrecioPorHora("20.00"); // This will be ignored
        tarifaBaseDTO.setMoneda("GTQ");
        tarifaBaseDTO.setFechaVigenciaInicio("2024-01-01");
        tarifaBaseDTO.setFechaVigenciaFin("2024-12-31");
        tarifaBaseDTO.setEsTarifaBase(true);

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifaBaseDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nueva tarifa de sucursal creada exitosamente.")));

        // Verify the tarifa was created with base price
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());
        assertEquals(testTarifaBase.getPrecioPorHora(), tarifas.get(0).getPrecioPorHora());
    }

    @Test
    void testCrearNuevaTarifaSucursal_UsuarioNotFound_Integration() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(999L);
        nuevaTarifaDTO.setPrecioPorHora("15.00");
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaTarifaDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada para el usuario proporcionado")));
    }

    @Test
    void testEditarTarifaSucursal_Success_Integration() throws Exception {
        // Arrange - First create a tarifa
        TarifaSucursal tarifaExistente = new TarifaSucursal();
        tarifaExistente.setSucursal(testSucursal);
        tarifaExistente.setPrecioPorHora(new BigDecimal("12.00"));
        tarifaExistente.setMoneda("GTQ");
        tarifaExistente.setFechaVigenciaInicio(LocalDateTime.now());
        tarifaExistente.setFechaVigenciaFin(LocalDateTime.now().plusMonths(6));
        tarifaExistente.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        tarifaExistente = tarifaSucursalRepository.save(tarifaExistente);

        TarifaSucursalDTO editarTarifaDTO = new TarifaSucursalDTO();
        editarTarifaDTO.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        editarTarifaDTO.setIdTarifaSucursal(tarifaExistente.getIdTarifaSucursal());
        editarTarifaDTO.setPrecioPorHora(18.00);
        editarTarifaDTO.setMoneda("GTQ");
        // Use proper ISO format for dates
        editarTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        editarTarifaDTO.setFechaVigenciaFin("2024-12-31");
        editarTarifaDTO.setEstado("VIGENTE");

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarTarifaDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Tarifa de sucursal editada exitosamente.")));

        // Verify the tarifa was actually updated - using compareTo for BigDecimal comparison
        TarifaSucursal updatedTarifa = tarifaSucursalRepository.findById(tarifaExistente.getIdTarifaSucursal()).orElse(null);
        assertNotNull(updatedTarifa);
        assertEquals(0, new BigDecimal("18.00").compareTo(updatedTarifa.getPrecioPorHora()));

        // Verify bitacora was created
        List<BitacoraTarifaSucursal> bitacoras = bitacoraTarifaSucursalRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaSucursal.Accion.ACTUALIZACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testEditarTarifaSucursal_TarifaNotFound_Integration() throws Exception {
        // Arrange
        TarifaSucursalDTO editarTarifaDTO = new TarifaSucursalDTO();
        editarTarifaDTO.setIdTarifaSucursal(999L);

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarTarifaDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Tarifa de sucursal no encontrada")));
    }

    @Test
    void testGetTarifasSucursalPorUsuario_Success_Integration() throws Exception {
        // Arrange - Create some tarifas
        TarifaSucursal tarifa1 = createTestTarifa(new BigDecimal("15.00"), TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        TarifaSucursal tarifa2 = createTestTarifa(new BigDecimal("12.00"), TarifaSucursal.EstadoTarifaSucursal.HISTORICO);

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idUsuarioSucursal", is(testUsuarioSucursal.getIdUsuario().intValue())))
                .andExpect(jsonPath("$[0].precioPorHora", is(15.0)))
                .andExpect(jsonPath("$[0].moneda", is("GTQ")))
                .andExpect(jsonPath("$[0].estado", is("VIGENTE")))
                .andExpect(jsonPath("$[1].precioPorHora", is(12.0)))
                .andExpect(jsonPath("$[1].estado", is("HISTORICO")));
    }

    @Test
    void testGetTarifasSucursalPorUsuario_EmptyList_Integration() throws Exception {
        // Act & Assert - No tarifas created
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetTarifasSucursalPorUsuario_UsuarioNotFound_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada para el usuario proporcionado")));
    }

    @Test
    void testCompleteWorkflow_SucursalManagement_Integration() throws Exception {
        // Test complete workflow: Get sucursal -> Edit sucursal -> Create tarifa -> Edit tarifa -> Get tarifas

        // Step 1: Get initial sucursal
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreSucursal", is("Sucursal Test")));

        // Step 2: Edit sucursal
        EditarSucursalDTO editarDTO = new EditarSucursalDTO();
        editarDTO.setIdSucursal(testSucursal.getIdSucursal());
        editarDTO.setNombreSucursal("Sucursal Test Workflow");
        editarDTO.setDireccionCompletaSucursal("Workflow Avenue 999");
        editarDTO.setCiudadSucursal("Guatemala");
        editarDTO.setDepartamentoSucursal("Guatemala");
        editarDTO.setHoraApertura("09:00");
        editarDTO.setHoraCierre("17:00");
        editarDTO.setCapacidad2Ruedas(40);
        editarDTO.setCapacidad4Ruedas(80);
        editarDTO.setLatitud(14.6000);
        editarDTO.setLongitud(-90.5000);
        editarDTO.setTelefonoContactoSucursal("11223344");
        editarDTO.setCorreoContactoSucursal("workflow@test.com");
        editarDTO.setEstadoSucursal("ACTIVA");

        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarDTO)))
                .andExpect(status().isOk());

        // Step 3: Create initial tarifa
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        nuevaTarifaDTO.setPrecioPorHora("12.50");
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin("2024-06-30");
        nuevaTarifaDTO.setEsTarifaBase(false);

        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaTarifaDTO)))
                .andExpect(status().isOk());

        // Step 4: Get tarifas to find the created tarifa ID
        List<TarifaSucursal> tarifasCreadas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifasCreadas.size());

        // Step 5: Edit the created tarifa
        TarifaSucursalDTO editarTarifaDTO = new TarifaSucursalDTO();
        editarTarifaDTO.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        editarTarifaDTO.setIdTarifaSucursal(tarifasCreadas.get(0).getIdTarifaSucursal());
        editarTarifaDTO.setPrecioPorHora(16.75);
        editarTarifaDTO.setMoneda("GTQ");
        // Use proper date format
        editarTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        editarTarifaDTO.setFechaVigenciaFin("2024-12-31");
        editarTarifaDTO.setEstado("VIGENTE");

        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarTarifaDTO)))
                .andExpect(status().isOk());

        // Step 6: Final verification - Get all tarifas
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].precioPorHora", is(16.75)));

        // Verify sucursal changes persisted
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", testUsuarioSucursal.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreSucursal", is("Sucursal Test Workflow")))
                .andExpect(jsonPath("$.direccionCompletaSucursal", is("Workflow Avenue 999")));
    }

    @Test
    void testCrearMultiplesTarifas_ReplacesOldOnes_Integration() throws Exception {
        // Create first tarifa
        NuevaTarifaSucursalDTO tarifa1 = new NuevaTarifaSucursalDTO();
        tarifa1.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        tarifa1.setPrecioPorHora("10.00");
        tarifa1.setMoneda("GTQ");
        tarifa1.setFechaVigenciaInicio("2024-01-01");
        tarifa1.setEsTarifaBase(false);

        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifa1)))
                .andExpect(status().isOk());

        // Verify first tarifa is VIGENTE
        List<TarifaSucursal> tarifasVigentes = tarifaSucursalRepository
                .findBySucursal_IdSucursalAndEstado(testSucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        assertEquals(1, tarifasVigentes.size());

        // Create second tarifa (should make first one HISTORICO)
        NuevaTarifaSucursalDTO tarifa2 = new NuevaTarifaSucursalDTO();
        tarifa2.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        tarifa2.setPrecioPorHora("15.00");
        tarifa2.setMoneda("GTQ");
        tarifa2.setFechaVigenciaInicio("2024-06-01");
        tarifa2.setEsTarifaBase(false);

        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifa2)))
                .andExpect(status().isOk());

        // Verify only one VIGENTE tarifa remains and one became HISTORICO
        List<TarifaSucursal> todasTarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(2, todasTarifas.size());

        List<TarifaSucursal> vigentes = tarifaSucursalRepository
                .findBySucursal_IdSucursalAndEstado(testSucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        assertEquals(1, vigentes.size());
        assertEquals(new BigDecimal("15.00"), vigentes.get(0).getPrecioPorHora());

        List<TarifaSucursal> historicas = tarifaSucursalRepository
                .findBySucursal_IdSucursalAndEstado(testSucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.HISTORICO);
        assertEquals(1, historicas.size());
        assertEquals(new BigDecimal("10.00"), historicas.get(0).getPrecioPorHora());
    }

    @Test
    void testTarifaSinFechaFin_Integration() throws Exception {
        // Test creating tarifa without end date
        NuevaTarifaSucursalDTO tarifaSinFin = new NuevaTarifaSucursalDTO();
        tarifaSinFin.setIdUsuarioSucursal(testUsuarioSucursal.getIdUsuario());
        tarifaSinFin.setPrecioPorHora("20.00");
        tarifaSinFin.setMoneda("GTQ");
        tarifaSinFin.setFechaVigenciaInicio("2024-01-01");
        tarifaSinFin.setFechaVigenciaFin(null); // No end date
        tarifaSinFin.setEsTarifaBase(false);

        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifaSinFin)))
                .andExpect(status().isOk());

        // Verify tarifa was created without end date
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());
        assertNull(tarifas.get(0).getFechaVigenciaFin());
    }

    private TarifaSucursal createTestTarifa(BigDecimal precio, TarifaSucursal.EstadoTarifaSucursal estado) {
        TarifaSucursal tarifa = new TarifaSucursal();
        tarifa.setSucursal(testSucursal);
        tarifa.setPrecioPorHora(precio);
        tarifa.setMoneda("GTQ");
        tarifa.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        tarifa.setFechaVigenciaFin(LocalDateTime.now().plusDays(30));
        tarifa.setEstado(estado);
        return tarifaSucursalRepository.save(tarifa);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
