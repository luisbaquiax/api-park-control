package org.parkcontrol.apiparkcontrol.controllers.empresa_sucursal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.CreateSucursalDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class EmpresaSucursalControllerIntegrationTest {

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

    // Test entities
    private Empresa testEmpresa;
    private Usuario testUsuarioEmpresa;
    private Usuario testUsuarioSucursal;
    private Sucursal testSucursal;
    private Rol empresaRol;
    private Rol sucursalRol;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean database
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create roles
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
        personaEmpresa.setNombre("Administrador");
        personaEmpresa.setApellido("Empresa");
        personaEmpresa.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        personaEmpresa.setDpi(generateUniqueDpi());
        personaEmpresa.setCorreo("admin@empresa.com");
        personaEmpresa.setTelefono("12345678");
        personaEmpresa.setDireccionCompleta("Dirección Empresa");
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
        testEmpresa.setNombreComercial("Empresa Test");
        testEmpresa.setRazonSocial("Empresa Test S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Dirección Fiscal Test");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test persona and usuario sucursal
        Persona personaSucursal = new Persona();
        personaSucursal.setNombre("Usuario");
        personaSucursal.setApellido("Sucursal");
        personaSucursal.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        personaSucursal.setDpi(generateUniqueDpi());
        personaSucursal.setCorreo("sucursal@test.com");
        personaSucursal.setTelefono("87654321");
        personaSucursal.setDireccionCompleta("Dirección Sucursal");
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
    void testGetUsuariosSucursalByEmpresa_Success_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", testEmpresa.getIdEmpresa()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sucursalDTO.idSucursal", is(testSucursal.getIdSucursal().intValue())))
                .andExpect(jsonPath("$[0].sucursalDTO.nombre", is("Sucursal Test")))
                .andExpect(jsonPath("$[0].sucursalDTO.direccionCompleta", is("Avenida Test 123")))
                .andExpect(jsonPath("$[0].sucursalDTO.ciudad", is("Guatemala")))
                .andExpect(jsonPath("$[0].sucursalDTO.departamento", is("Guatemala")))
                .andExpect(jsonPath("$[0].sucursalDTO.horaApertura", is("08:00")))
                .andExpect(jsonPath("$[0].sucursalDTO.horaCierre", is("18:00")))
                .andExpect(jsonPath("$[0].sucursalDTO.capacidad2Ruedas", is(50)))
                .andExpect(jsonPath("$[0].sucursalDTO.capacidad4Ruedas", is(100)))
                .andExpect(jsonPath("$[0].sucursalDTO.estado", is("ACTIVA")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.nombre", is("Usuario")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.apellido", is("Sucursal")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.correo", is("sucursal@test.com")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.nombreUsuario", is("usersucursal")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.dobleFactorHabilitado", is(false)))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.estado", is("ACTIVO")));
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_EmpresaNotFound_Integration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", 999L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_MultipleSucursales_Integration() throws Exception {
        // Arrange - Create second sucursal
        Persona personaSucursal2 = new Persona();
        personaSucursal2.setNombre("Segundo");
        personaSucursal2.setApellido("Usuario");
        personaSucursal2.setFechaNacimiento(LocalDate.of(1990, 3, 10));
        personaSucursal2.setDpi(generateUniqueDpi());
        personaSucursal2.setCorreo("segundo@test.com");
        personaSucursal2.setTelefono("11223344");
        personaSucursal2.setDireccionCompleta("Segunda Dirección");
        personaSucursal2.setCiudad("Antigua");
        personaSucursal2.setPais("Guatemala");
        personaSucursal2.setCodigoPostal("01003");
        personaSucursal2.setEstado(Persona.Estado.ACTIVO);
        personaSucursal2 = personaRepository.save(personaSucursal2);

        Usuario usuarioSucursal2 = new Usuario();
        usuarioSucursal2.setPersona(personaSucursal2);
        usuarioSucursal2.setRol(sucursalRol);
        usuarioSucursal2.setNombreUsuario("segundouser");
        usuarioSucursal2.setContraseniaHash("hashedPassword2");
        usuarioSucursal2.setDobleFactorHabilitado(true);
        usuarioSucursal2.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioSucursal2.setDebeCambiarContrasenia(false);
        usuarioSucursal2.setEsPrimeraVez(false);
        usuarioSucursal2.setIntentosFallidos(0);
        usuarioSucursal2 = usuarioRepository.save(usuarioSucursal2);

        Sucursal sucursal2 = new Sucursal();
        sucursal2.setEmpresa(testEmpresa);
        sucursal2.setUsuarioSucursal(usuarioSucursal2);
        sucursal2.setNombre("Segunda Sucursal");
        sucursal2.setDireccionCompleta("Segunda Avenida 456");
        sucursal2.setCiudad("Antigua");
        sucursal2.setDepartamento("Sacatepéquez");
        sucursal2.setLatitud(new BigDecimal("14.5607"));
        sucursal2.setLongitud(new BigDecimal("-90.7346"));
        sucursal2.setHoraApertura(LocalTime.of(9, 0));
        sucursal2.setHoraCierre(LocalTime.of(17, 0));
        sucursal2.setCapacidad2Ruedas(30);
        sucursal2.setCapacidad4Ruedas(60);
        sucursal2.setTelefonoContacto("55667788");
        sucursal2.setCorreoContacto("segunda@sucursal.com");
        sucursal2.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        sucursalRepository.save(sucursal2);

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", testEmpresa.getIdEmpresa()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sucursalDTO.nombre", anyOf(is("Sucursal Test"), is("Segunda Sucursal"))))
                .andExpect(jsonPath("$[1].sucursalDTO.nombre", anyOf(is("Sucursal Test"), is("Segunda Sucursal"))));
    }

    @Test
    void testCreateSucursal_Success_Integration() throws Exception {
        // Arrange
        CreateSucursalDTO createDTO = new CreateSucursalDTO();
        createDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        // Usuario data
        createDTO.setNombre("Nueva");
        createDTO.setApellido("Persona");
        createDTO.setFechaNacimiento("1992-08-25");
        createDTO.setDpi(generateUniqueDpi());
        createDTO.setCorreo("nueva@persona.com");
        createDTO.setTelefono("99887766");
        createDTO.setDireccionCompleta("Nueva Dirección 789");
        createDTO.setCiudad("Quetzaltenango");
        createDTO.setPais("Guatemala");
        createDTO.setCodigoPostal("09001");
        createDTO.setNombreUsuario("nuevousuario");
        createDTO.setContraseniaHash("newpassword123");
        createDTO.setDobleFactorHabilitado(true);
        // Sucursal data
        createDTO.setNombreSucursal("Nueva Sucursal");
        createDTO.setDireccionCompletaSucursal("Nueva Avenida Principal");
        createDTO.setCiudadSucursal("Quetzaltenango");
        createDTO.setDepartamentoSucursal("Quetzaltenango");
        createDTO.setHoraApertura("07:30");
        createDTO.setHoraCierre("19:30");
        createDTO.setCapacidad2Ruedas(40);
        createDTO.setCapacidad4Ruedas(80);
        createDTO.setLatitud(14.8411);
        createDTO.setLongitud(-91.5188);
        createDTO.setTelefonoContactoSucursal("77665544");
        createDTO.setCorreoContactoSucursal("nueva@sucursal.com");

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Sucursal creada exitosamente")));

        // Verify the sucursal was actually created
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", testEmpresa.getIdEmpresa()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // Original + newly created
    }

    @Test
    void testCreateSucursal_EmpresaNotFound_Integration() throws Exception {
        // Arrange
        CreateSucursalDTO createDTO = new CreateSucursalDTO();
        createDTO.setIdEmpresa(999L); // Non-existent empresa
        createDTO.setNombre("Test");
        createDTO.setApellido("User");
        createDTO.setNombreSucursal("Test Sucursal");

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Empresa no encontrada")));
    }

    @Test
    void testCompleteWorkflow_CreateMultipleSucursales_Integration() throws Exception {
        // Test complete workflow: create multiple sucursales and verify they all exist

        // First sucursal
        CreateSucursalDTO sucursal1 = createValidSucursalDTO("Primer", "Usuario", "primer@test.com", "primerusuario", "Primera Sucursal");
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sucursal1)))
                .andExpect(status().isOk());

        // Second sucursal
        CreateSucursalDTO sucursal2 = createValidSucursalDTO("Segundo", "Usuario", "segundo@test.com", "segundousuario", "Segunda Sucursal");
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sucursal2)))
                .andExpect(status().isOk());

        // Verify all sucursales exist
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", testEmpresa.getIdEmpresa()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Original + 2 new ones
    }

    private CreateSucursalDTO createValidSucursalDTO(String nombre, String apellido, String correo, String nombreUsuario, String nombreSucursal) {
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(testEmpresa.getIdEmpresa());
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setFechaNacimiento("1990-01-01");
        dto.setDpi(generateUniqueDpi());
        dto.setCorreo(correo);
        dto.setTelefono("12345678");
        dto.setDireccionCompleta("Test Address");
        dto.setCiudad("Guatemala");
        dto.setPais("Guatemala");
        dto.setCodigoPostal("01001");
        dto.setNombreUsuario(nombreUsuario);
        dto.setContraseniaHash("password123");
        dto.setDobleFactorHabilitado(false);
        dto.setNombreSucursal(nombreSucursal);
        dto.setDireccionCompletaSucursal("Test Sucursal Address");
        dto.setCiudadSucursal("Guatemala");
        dto.setDepartamentoSucursal("Guatemala");
        dto.setHoraApertura("08:00");
        dto.setHoraCierre("18:00");
        dto.setCapacidad2Ruedas(50);
        dto.setCapacidad4Ruedas(100);
        dto.setLatitud(14.6349);
        dto.setLongitud(-90.5069);
        dto.setTelefonoContactoSucursal("87654321");
        dto.setCorreoContactoSucursal("test@sucursal.com");
        return dto;
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
