package org.parkcontrol.apiparkcontrol.services.empresa_sucursal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class EmpresaSucursalServiceIntegrationTest {

    @Autowired
    private EmpresaSucursalService empresaSucursalService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    private Empresa testEmpresa;
    private Rol sucursalRol;

    @BeforeEach
    void setUp() {
        // Clean up database
        sucursalRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        empresaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        sucursalRol = new Rol();
        sucursalRol.setNombreRol("SUCURSAL");
        sucursalRol.setDescripcion("Usuario de sucursal");
        sucursalRol = rolRepository.save(sucursalRol);

        // Create test empresa
        testEmpresa = new Empresa();
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
    void testCrearUsuarioSucursal_Integration() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("Juan");
        userDTO.setApellido("Pérez");
        userDTO.setFechaNacimiento("1990-01-01");
        userDTO.setDpi("1234567890123");
        userDTO.setCorreo("juan@test.com");
        userDTO.setTelefono("12345678");
        userDTO.setDireccionCompleta("Test Address");
        userDTO.setCiudad("Test City");
        userDTO.setPais("Test Country");
        userDTO.setCodigoPostal("12345");
        userDTO.setNombreUsuario("juanuser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("ACTIVO");

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getIdUsuario());
        assertEquals("juanuser", result.getNombreUsuario());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, result.getEstado());
        assertTrue(result.isDebeCambiarContrasenia());
        assertTrue(result.isEsPrimeraVez());
        assertFalse(result.isDobleFactorHabilitado());

        // Verify in database
        Optional<Usuario> savedUser = usuarioRepository.findById(result.getIdUsuario());
        assertTrue(savedUser.isPresent());
        assertEquals("juanuser", savedUser.get().getNombreUsuario());
        assertEquals("SUCURSAL", savedUser.get().getRol().getNombreRol());

        Optional<Persona> savedPersona = personaRepository.findById(result.getPersona().getIdPersona());
        assertTrue(savedPersona.isPresent());
        assertEquals("juan@test.com", savedPersona.get().getCorreo());
        assertEquals("Juan", savedPersona.get().getNombre());
    }

    @Test
    void testCrearUsuarioSucursal_WithDifferentEstado_Integration() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("Maria");
        userDTO.setApellido("García");
        userDTO.setFechaNacimiento("1985-05-15");
        userDTO.setDpi("9876543210987");
        userDTO.setCorreo("maria@test.com");
        userDTO.setTelefono("87654321");
        userDTO.setDireccionCompleta("Another Address");
        userDTO.setCiudad("Another City");
        userDTO.setPais("Another Country");
        userDTO.setCodigoPostal("54321");
        userDTO.setNombreUsuario("mariauser");
        userDTO.setContraseniaHash("password456");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("INACTIVO");

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("mariauser", result.getNombreUsuario());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, result.getEstado()); // Always set to ACTIVO in method
        assertTrue(result.isDobleFactorHabilitado());
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_Integration() {
        // Arrange
        // Create test user and sucursal
        Usuario testUser = createTestUsuarioSucursal();
        createTestSucursal(testUser);

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(testEmpresa.getIdEmpresa());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ObtenerSucursalesEmpresaDTO dto = result.get(0);
        assertNotNull(dto.getSucursalDTO());
        assertEquals("Test Sucursal", dto.getSucursalDTO().getNombre());
        assertEquals("Test City", dto.getSucursalDTO().getCiudad());
        assertEquals("Test Department", dto.getSucursalDTO().getDepartamento());
        assertEquals("08:00", dto.getSucursalDTO().getHoraApertura());
        assertEquals("18:00", dto.getSucursalDTO().getHoraCierre());
        assertEquals(50, dto.getSucursalDTO().getCapacidad2Ruedas());
        assertEquals(30, dto.getSucursalDTO().getCapacidad4Ruedas());
        assertEquals("ACTIVA", dto.getSucursalDTO().getEstado());

        UsuarioSucursalDTO usuarioDTO = dto.getSucursalDTO().getUsuario();
        assertNotNull(usuarioDTO);
        assertEquals("Test", usuarioDTO.getNombre());
        assertEquals("User", usuarioDTO.getApellido());
        assertEquals("test@test.com", usuarioDTO.getCorreo());
        assertEquals("testuser", usuarioDTO.getNombreUsuario());
        assertEquals("ACTIVO", usuarioDTO.getEstado());
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_EmptyResult_Integration() {
        // Arrange - Use non-existent empresa ID

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_MultipleSucursales_Integration() {
        // Arrange
        Usuario testUser1 = createTestUsuarioSucursal();
        createTestSucursal(testUser1);

        Usuario testUser2 = createTestUsuarioSucursal("testuser2", "test2@test.com", "Test 2", "User 2");
        createTestSucursal(testUser2, "Sucursal 2", "Test City 2", Sucursal.EstadoSucursal.INACTIVA);

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(testEmpresa.getIdEmpresa());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify different sucursales exist
        List<String> nombres = result.stream()
                .map(dto -> dto.getSucursalDTO().getNombre())
                .toList();
        assertTrue(nombres.contains("Test Sucursal"));
        assertTrue(nombres.contains("Sucursal 2"));
    }

    @Test
    void testCrearNuevaSucursal_Integration() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(testEmpresa.getIdEmpresa());
        // Usuario data
        dto.setNombre("Carlos");
        dto.setApellido("López");
        dto.setFechaNacimiento("1988-03-10");
        dto.setDpi("5555555555555");
        dto.setCorreo("carlos@test.com");
        dto.setTelefono("11111111");
        dto.setDireccionCompleta("Carlos Address");
        dto.setCiudad("Carlos City");
        dto.setPais("Carlos Country");
        dto.setCodigoPostal("11111");
        dto.setNombreUsuario("carlosuser");
        dto.setContraseniaHash("carlospass");
        dto.setDobleFactorHabilitado(false);
        // Sucursal data
        dto.setNombreSucursal("Nueva Sucursal");
        dto.setDireccionCompletaSucursal("Sucursal Address");
        dto.setCiudadSucursal("Sucursal City");
        dto.setDepartamentoSucursal("Sucursal Department");
        dto.setHoraApertura("07:30");
        dto.setHoraCierre("19:30");
        dto.setCapacidad2Ruedas(60);
        dto.setCapacidad4Ruedas(40);
        dto.setLatitud(15.123456);
        dto.setLongitud(-91.123456);
        dto.setTelefonoContactoSucursal("22222222");
        dto.setCorreoContactoSucursal("nuevasucursal@test.com");

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);

        // Verify sucursal was created
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(testEmpresa.getIdEmpresa());
        assertEquals(1, sucursales.size());

        Sucursal createdSucursal = sucursales.get(0);
        assertEquals("Nueva Sucursal", createdSucursal.getNombre());
        assertEquals("Sucursal City", createdSucursal.getCiudad());
        assertEquals("Sucursal Department", createdSucursal.getDepartamento());
        assertEquals(LocalTime.of(7, 30), createdSucursal.getHoraApertura());
        assertEquals(LocalTime.of(19, 30), createdSucursal.getHoraCierre());
        assertEquals(60, createdSucursal.getCapacidad2Ruedas());
        assertEquals(40, createdSucursal.getCapacidad4Ruedas());
        assertEquals(new BigDecimal("15.123456"), createdSucursal.getLatitud());
        assertEquals(new BigDecimal("-91.123456"), createdSucursal.getLongitud());
        assertEquals("22222222", createdSucursal.getTelefonoContacto());
        assertEquals("nuevasucursal@test.com", createdSucursal.getCorreoContacto());
        assertEquals(Sucursal.EstadoSucursal.ACTIVA, createdSucursal.getEstado());

        // Verify usuario was created
        Usuario createdUsuario = createdSucursal.getUsuarioSucursal();
        assertNotNull(createdUsuario);
        assertEquals("carlosuser", createdUsuario.getNombreUsuario());
        assertEquals("SUCURSAL", createdUsuario.getRol().getNombreRol());
        assertTrue(createdUsuario.isDebeCambiarContrasenia());
        assertTrue(createdUsuario.isEsPrimeraVez());

        // Verify persona was created
        Persona createdPersona = createdUsuario.getPersona();
        assertNotNull(createdPersona);
        assertEquals("Carlos", createdPersona.getNombre());
        assertEquals("López", createdPersona.getApellido());
        assertEquals("carlos@test.com", createdPersona.getCorreo());
    }

    @Test
    void testCrearNuevaSucursal_EmpresaNotFound_Integration() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(999L); // Non-existent empresa
        dto.setNombre("Test");
        dto.setApellido("User");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            empresaSucursalService.crearNuevaSucursal(dto);
        });

        assertEquals("Empresa no encontrada", exception.getMessage());

        // Verify no sucursal was created
        List<Sucursal> sucursales = sucursalRepository.findAll();
        assertTrue(sucursales.isEmpty());
    }

    @Test
    void testCrearNuevaSucursal_WithDifferentTimeFormats_Integration() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(testEmpresa.getIdEmpresa());
        dto.setNombre("Ana");
        dto.setApellido("Martínez");
        dto.setFechaNacimiento("1992-12-25");
        dto.setDpi("7777777777777");
        dto.setCorreo("ana@test.com");
        dto.setTelefono("33333333");
        dto.setDireccionCompleta("Ana Address");
        dto.setCiudad("Ana City");
        dto.setPais("Ana Country");
        dto.setCodigoPostal("33333");
        dto.setNombreUsuario("anauser");
        dto.setContraseniaHash("anapass");
        dto.setDobleFactorHabilitado(true);
        dto.setNombreSucursal("Sucursal 24H");
        dto.setDireccionCompletaSucursal("24H Address");
        dto.setCiudadSucursal("24H City");
        dto.setDepartamentoSucursal("24H Department");
        dto.setHoraApertura("00:00");
        dto.setHoraCierre("23:59");
        dto.setCapacidad2Ruedas(100);
        dto.setCapacidad4Ruedas(80);
        dto.setLatitud(16.0);
        dto.setLongitud(-89.0);
        dto.setTelefonoContactoSucursal("44444444");
        dto.setCorreoContactoSucursal("24h@test.com");

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);

        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(testEmpresa.getIdEmpresa());
        assertEquals(1, sucursales.size());

        Sucursal createdSucursal = sucursales.get(0);
        assertEquals(LocalTime.of(0, 0), createdSucursal.getHoraApertura());
        assertEquals(LocalTime.of(23, 59), createdSucursal.getHoraCierre());
        assertTrue(createdSucursal.getUsuarioSucursal().isDobleFactorHabilitado());
    }

    @Test
    void testCrearNuevaSucursal_WithMinimalCapacities_Integration() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(testEmpresa.getIdEmpresa());
        dto.setNombre("Minimal");
        dto.setApellido("User");
        dto.setFechaNacimiento("2000-01-01");
        dto.setDpi("1111111111111");
        dto.setCorreo("minimal@test.com");
        dto.setTelefono("11111111");
        dto.setDireccionCompleta("Minimal Address");
        dto.setCiudad("Minimal City");
        dto.setPais("Minimal Country");
        dto.setCodigoPostal("11111");
        dto.setNombreUsuario("minimaluser");
        dto.setContraseniaHash("minimalpass");
        dto.setDobleFactorHabilitado(false);
        dto.setNombreSucursal("Minimal Sucursal");
        dto.setDireccionCompletaSucursal("Minimal Address");
        dto.setCiudadSucursal("Minimal City");
        dto.setDepartamentoSucursal("Minimal Department");
        dto.setHoraApertura("08:00");
        dto.setHoraCierre("16:00");
        dto.setCapacidad2Ruedas(1);
        dto.setCapacidad4Ruedas(1);
        dto.setLatitud(1.0);
        dto.setLongitud(1.0);
        dto.setTelefonoContactoSucursal("11111111");
        dto.setCorreoContactoSucursal("minimal@sucursal.com");

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);

        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(testEmpresa.getIdEmpresa());
        assertEquals(1, sucursales.size());

        Sucursal createdSucursal = sucursales.get(0);
        assertEquals(1, createdSucursal.getCapacidad2Ruedas());
        assertEquals(1, createdSucursal.getCapacidad4Ruedas());
        assertEquals(BigDecimal.valueOf(1.0), createdSucursal.getLatitud());
        assertEquals(BigDecimal.valueOf(1.0), createdSucursal.getLongitud());
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_WithDifferentUsuarioEstados_Integration() {
        // Arrange
        // Create users with different estados
        Usuario usuarioActivo = createTestUsuarioSucursal("activeuser", "active@test.com", "Active", "User");
        Usuario usuarioInactivo = createTestUsuarioSucursal("inactiveuser", "inactive@test.com", "Inactive", "User");
        usuarioInactivo.setEstado(Usuario.EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuarioInactivo);

        Usuario usuarioSuspendido = createTestUsuarioSucursal("suspendeduser", "suspended@test.com", "Suspended", "User");
        usuarioSuspendido.setEstado(Usuario.EstadoUsuario.SUSPENDIDO);
        usuarioRepository.save(usuarioSuspendido);

        // Create sucursales with different user estados
        createTestSucursal(usuarioActivo, "Sucursal Activa", "Active City", Sucursal.EstadoSucursal.ACTIVA);
        createTestSucursal(usuarioInactivo, "Sucursal Inactiva", "Inactive City", Sucursal.EstadoSucursal.ACTIVA);
        createTestSucursal(usuarioSuspendido, "Sucursal Suspendida", "Suspended City", Sucursal.EstadoSucursal.ACTIVA);

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(testEmpresa.getIdEmpresa());

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify different user estados
        List<String> userEstados = result.stream()
                .map(dto -> dto.getSucursalDTO().getUsuario().getEstado())
                .toList();

        assertTrue(userEstados.contains("ACTIVO"));
        assertTrue(userEstados.contains("INACTIVO"));
        assertTrue(userEstados.contains("SUSPENDIDO"));
    }

    @Test
    void testCrearUsuarioSucursal_WithSpecialCharacters_Integration() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("José María");
        userDTO.setApellido("García-López");
        userDTO.setFechaNacimiento("1988-09-15");
        userDTO.setDpi("2222222222222");
        userDTO.setCorreo("jose.maria@ñoño.com");
        userDTO.setTelefono("22222222");
        userDTO.setDireccionCompleta("Calle 123, Avenida Principal, #456");
        userDTO.setCiudad("Ciudad de Panamá");
        userDTO.setPais("Panamá");
        userDTO.setCodigoPostal("00001");
        userDTO.setNombreUsuario("jose.maria.garcia");
        userDTO.setContraseniaHash("josé123ñoño");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("ACTIVO");

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("jose.maria.garcia", result.getNombreUsuario());
        assertTrue(result.isDobleFactorHabilitado());

        // Verify in database
        Persona savedPersona = result.getPersona();
        assertEquals("José María", savedPersona.getNombre());
        assertEquals("García-López", savedPersona.getApellido());
        assertEquals("jose.maria@ñoño.com", savedPersona.getCorreo());
        assertEquals("Ciudad de Panamá", savedPersona.getCiudad());
        assertEquals("Panamá", savedPersona.getPais());
    }

    @Test
    void testCompleteWorkflow_Integration() {
        // Test complete workflow: create user, create sucursal, then get sucursales
        
        // Step 1: Create sucursal with user
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(testEmpresa.getIdEmpresa());
        dto.setNombre("Workflow");
        dto.setApellido("Test");
        dto.setFechaNacimiento("1990-06-15");
        dto.setDpi("3333333333333");
        dto.setCorreo("workflow@test.com");
        dto.setTelefono("33333333");
        dto.setDireccionCompleta("Workflow Address");
        dto.setCiudad("Workflow City");
        dto.setPais("Workflow Country");
        dto.setCodigoPostal("33333");
        dto.setNombreUsuario("workflowuser");
        dto.setContraseniaHash("workflowpass");
        dto.setDobleFactorHabilitado(false);
        dto.setNombreSucursal("Workflow Sucursal");
        dto.setDireccionCompletaSucursal("Workflow Sucursal Address");
        dto.setCiudadSucursal("Workflow Sucursal City");
        dto.setDepartamentoSucursal("Workflow Department");
        dto.setHoraApertura("09:00");
        dto.setHoraCierre("18:00");
        dto.setCapacidad2Ruedas(75);
        dto.setCapacidad4Ruedas(50);
        dto.setLatitud(14.5);
        dto.setLongitud(-90.5);
        dto.setTelefonoContactoSucursal("33333333");
        dto.setCorreoContactoSucursal("workflow@sucursal.com");

        // Step 2: Create the sucursal
        String createResult = empresaSucursalService.crearNuevaSucursal(dto);
        assertEquals("Sucursal creada exitosamente", createResult);

        // Step 3: Get sucursales for the empresa
        List<ObtenerSucursalesEmpresaDTO> sucursales = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(testEmpresa.getIdEmpresa());

        // Step 4: Verify the complete workflow
        assertNotNull(sucursales);
        assertEquals(1, sucursales.size());

        ObtenerSucursalesEmpresaDTO sucursalDTO = sucursales.get(0);
        assertEquals("Workflow Sucursal", sucursalDTO.getSucursalDTO().getNombre());
        assertEquals("Workflow Sucursal City", sucursalDTO.getSucursalDTO().getCiudad());
        assertEquals("Workflow Department", sucursalDTO.getSucursalDTO().getDepartamento());
        assertEquals("09:00", sucursalDTO.getSucursalDTO().getHoraApertura());
        assertEquals("18:00", sucursalDTO.getSucursalDTO().getHoraCierre());
        assertEquals(75, sucursalDTO.getSucursalDTO().getCapacidad2Ruedas());
        assertEquals(50, sucursalDTO.getSucursalDTO().getCapacidad4Ruedas());

        UsuarioSucursalDTO usuarioDTO = sucursalDTO.getSucursalDTO().getUsuario();
        assertEquals("Workflow", usuarioDTO.getNombre());
        assertEquals("Test", usuarioDTO.getApellido());
        assertEquals("workflow@test.com", usuarioDTO.getCorreo());
        assertEquals("workflowuser", usuarioDTO.getNombreUsuario());
        assertEquals("ACTIVO", usuarioDTO.getEstado());
        assertFalse(usuarioDTO.isDobleFactorHabilitado());

        // Step 5: Verify data persistence
        List<Sucursal> persistedSucursales = sucursalRepository.findByEmpresaIdEmpresa(testEmpresa.getIdEmpresa());
        assertEquals(1, persistedSucursales.size());

        Sucursal persistedSucursal = persistedSucursales.get(0);
        assertNotNull(persistedSucursal.getFechaCreacion());
        assertNotNull(persistedSucursal.getFechaUltimaActualizacion());
        assertEquals(Sucursal.EstadoSucursal.ACTIVA, persistedSucursal.getEstado());

        Usuario persistedUsuario = persistedSucursal.getUsuarioSucursal();
        assertNotNull(persistedUsuario.getFechaCreacion());
        assertNotNull(persistedUsuario.getFechaUltimaActualizacion());
        assertTrue(persistedUsuario.isDebeCambiarContrasenia());
        assertTrue(persistedUsuario.isEsPrimeraVez());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, persistedUsuario.getEstado());
        assertEquals("SUCURSAL", persistedUsuario.getRol().getNombreRol());
    }

    private Usuario createTestUsuarioSucursal() {
        return createTestUsuarioSucursal("testuser", "test@test.com", "Test", "User");
    }

    private Usuario createTestUsuarioSucursal(String username, String email, String nombre, String apellido) {
        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
        // Generar DPI único para cada test
        persona.setDpi(generateUniqueDpi());
        persona.setCorreo(email);
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setRol(sucursalRol);
        usuario.setNombreUsuario(username);
        usuario.setContraseniaHash("hashedPassword");
        usuario.setDobleFactorHabilitado(false);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario.setDebeCambiarContrasenia(true);
        usuario.setEsPrimeraVez(true);
        usuario.setIntentosFallidos(0);

        return usuarioRepository.save(usuario);
    }

    private Sucursal createTestSucursal(Usuario usuario) {
        return createTestSucursal(usuario, "Test Sucursal", "Test City", Sucursal.EstadoSucursal.ACTIVA);
    }

    private Sucursal createTestSucursal(Usuario usuario, String nombre, String ciudad, Sucursal.EstadoSucursal estado) {
        Sucursal sucursal = new Sucursal();
        sucursal.setEmpresa(testEmpresa);
        sucursal.setUsuarioSucursal(usuario);
        sucursal.setNombre(nombre);
        sucursal.setDireccionCompleta("Test Address");
        sucursal.setCiudad(ciudad);
        sucursal.setDepartamento("Test Department");
        sucursal.setHoraApertura(LocalTime.of(8, 0));
        sucursal.setHoraCierre(LocalTime.of(18, 0));
        sucursal.setCapacidad2Ruedas(50);
        sucursal.setCapacidad4Ruedas(30);
        sucursal.setLatitud(new BigDecimal("14.634915"));
        sucursal.setLongitud(new BigDecimal("-90.506882"));
        sucursal.setTelefonoContacto("87654321");
        sucursal.setCorreoContacto("sucursal@test.com");
        sucursal.setEstado(estado);

        return sucursalRepository.save(sucursal);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
