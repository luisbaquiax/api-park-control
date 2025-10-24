package org.parkcontrol.apiparkcontrol.services.empresa_sucursal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaSucursalServiceUnitTest {

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

    @InjectMocks
    private EmpresaSucursalService empresaSucursalService;

    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private Usuario mockUsuario;
    private Persona mockPersona;
    private Rol mockRol;

    @BeforeEach
    void setUp() {
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Pérez");
        mockPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        mockPersona.setDpi("1234567890123");
        mockPersona.setCorreo("juan@test.com");
        mockPersona.setTelefono("12345678");
        mockPersona.setDireccionCompleta("Test Address");
        mockPersona.setCiudad("Test City");
        mockPersona.setPais("Test Country");
        mockPersona.setCodigoPostal("12345");
        mockPersona.setEstado(Persona.Estado.ACTIVO);

        mockRol = new Rol();
        mockRol.setIdRol(2L);
        mockRol.setNombreRol("SUCURSAL");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setPersona(mockPersona);
        mockUsuario.setRol(mockRol);
        mockUsuario.setNombreUsuario("sucursaluser");
        mockUsuario.setContraseniaHash("hashedPassword");
        mockUsuario.setDobleFactorHabilitado(false);
        mockUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        mockUsuario.setDebeCambiarContrasenia(true);
        mockUsuario.setEsPrimeraVez(true);

        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setRazonSocial("Test Company S.A.");
        mockEmpresa.setNit("1234567-8");
        mockEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setUsuarioSucursal(mockUsuario);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setDireccionCompleta("Test Address");
        mockSucursal.setCiudad("Test City");
        mockSucursal.setDepartamento("Test Department");
        mockSucursal.setHoraApertura(LocalTime.of(8, 0));
        mockSucursal.setHoraCierre(LocalTime.of(18, 0));
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(30);
        mockSucursal.setLatitud(new BigDecimal("14.634915"));
        mockSucursal.setLongitud(new BigDecimal("-90.506882"));
        mockSucursal.setTelefonoContacto("87654321");
        mockSucursal.setCorreoContacto("sucursal@test.com");
        mockSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
    }

    @Test
    void testCrearUsuarioSucursal_Success() {
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
        userDTO.setNombreUsuario("sucursaluser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("ACTIVO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        // Mock password encoder
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("password123")).thenReturn("hashedPassword");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals(mockUsuario.getIdUsuario(), result.getIdUsuario());
        assertEquals("sucursaluser", result.getNombreUsuario());
        assertTrue(result.isDebeCambiarContrasenia());
        assertTrue(result.isEsPrimeraVez());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, result.getEstado());

        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(rolRepository).findByNombreRol("SUCURSAL");
    }

    @Test
    void testCrearUsuarioSucursal_WithDifferentEstado() {
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

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("password456")).thenReturn("hashedPassword456");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_Success() {
        // Arrange
        Long idEmpresa = 1L;
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);

        when(sucursalRepository.findByEmpresaIdEmpresa(idEmpresa)).thenReturn(sucursales);

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ObtenerSucursalesEmpresaDTO dto = result.get(0);
        assertNotNull(dto.getSucursalDTO());
        assertEquals(mockSucursal.getIdSucursal(), dto.getSucursalDTO().getIdSucursal());
        assertEquals("Sucursal Test", dto.getSucursalDTO().getNombre());
        assertEquals("Test City", dto.getSucursalDTO().getCiudad());
        assertEquals("08:00", dto.getSucursalDTO().getHoraApertura());
        assertEquals("18:00", dto.getSucursalDTO().getHoraCierre());
        assertEquals(50, dto.getSucursalDTO().getCapacidad2Ruedas());
        assertEquals(30, dto.getSucursalDTO().getCapacidad4Ruedas());
        assertEquals("ACTIVA", dto.getSucursalDTO().getEstado());

        UsuarioSucursalDTO usuarioDTO = dto.getSucursalDTO().getUsuario();
        assertNotNull(usuarioDTO);
        assertEquals("Juan", usuarioDTO.getNombre());
        assertEquals("Pérez", usuarioDTO.getApellido());
        assertEquals("juan@test.com", usuarioDTO.getCorreo());
        assertEquals("sucursaluser", usuarioDTO.getNombreUsuario());
        assertEquals("ACTIVO", usuarioDTO.getEstado());

        verify(sucursalRepository).findByEmpresaIdEmpresa(idEmpresa);
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_EmptyList() {
        // Arrange
        Long idEmpresa = 999L;
        when(sucursalRepository.findByEmpresaIdEmpresa(idEmpresa)).thenReturn(Arrays.asList());

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sucursalRepository).findByEmpresaIdEmpresa(idEmpresa);
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_MultipleSucursales() {
        // Arrange
        Long idEmpresa = 1L;
        
        // Create second sucursal
        Sucursal mockSucursal2 = new Sucursal();
        mockSucursal2.setIdSucursal(2L);
        mockSucursal2.setEmpresa(mockEmpresa);
        mockSucursal2.setUsuarioSucursal(mockUsuario);
        mockSucursal2.setNombre("Sucursal Test 2");
        mockSucursal2.setDireccionCompleta("Test Address 2");
        mockSucursal2.setCiudad("Test City 2");
        mockSucursal2.setDepartamento("Test Department 2");
        mockSucursal2.setHoraApertura(LocalTime.of(9, 0));
        mockSucursal2.setHoraCierre(LocalTime.of(19, 0));
        mockSucursal2.setCapacidad2Ruedas(40);
        mockSucursal2.setCapacidad4Ruedas(25);
        mockSucursal2.setEstado(Sucursal.EstadoSucursal.INACTIVA);

        List<Sucursal> sucursales = Arrays.asList(mockSucursal, mockSucursal2);

        when(sucursalRepository.findByEmpresaIdEmpresa(idEmpresa)).thenReturn(sucursales);

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first sucursal
        assertEquals(mockSucursal.getIdSucursal(), result.get(0).getSucursalDTO().getIdSucursal());
        assertEquals("ACTIVA", result.get(0).getSucursalDTO().getEstado());
        
        // Verify second sucursal
        assertEquals(mockSucursal2.getIdSucursal(), result.get(1).getSucursalDTO().getIdSucursal());
        assertEquals("INACTIVA", result.get(1).getSucursalDTO().getEstado());

        verify(sucursalRepository).findByEmpresaIdEmpresa(idEmpresa);
    }

    @Test
    void testCrearNuevaSucursal_Success() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(1L);
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

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(mockSucursal);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("carlospass")).thenReturn("hashedCarlosPass");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);
        
        verify(empresaRepository).findById(1L);
        verify(rolRepository).findByNombreRol("SUCURSAL");
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    void testCrearNuevaSucursal_EmpresaNotFound() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(999L);
        dto.setNombre("Test");
        dto.setApellido("User");

        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            empresaSucursalService.crearNuevaSucursal(dto);
        });
        
        assertEquals("Empresa no encontrada", exception.getMessage());
        verify(empresaRepository).findById(999L);
        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void testCrearNuevaSucursal_WithDifferentTimeFormats() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(1L);
        dto.setNombre("Test");
        dto.setApellido("User");
        dto.setFechaNacimiento("1990-01-01");
        dto.setDpi("1234567890123");
        dto.setCorreo("test@test.com");
        dto.setTelefono("12345678");
        dto.setDireccionCompleta("Test Address");
        dto.setCiudad("Test City");
        dto.setPais("Test Country");
        dto.setCodigoPostal("12345");
        dto.setNombreUsuario("testuser");
        dto.setContraseniaHash("password");
        dto.setDobleFactorHabilitado(true);
        dto.setNombreSucursal("Test Sucursal");
        dto.setDireccionCompletaSucursal("Test Sucursal Address");
        dto.setCiudadSucursal("Test Sucursal City");
        dto.setDepartamentoSucursal("Test Department");
        dto.setHoraApertura("06:00");
        dto.setHoraCierre("22:00");
        dto.setCapacidad2Ruedas(100);
        dto.setCapacidad4Ruedas(80);
        dto.setLatitud(14.0);
        dto.setLongitud(-90.0);
        dto.setTelefonoContactoSucursal("87654321");
        dto.setCorreoContactoSucursal("contact@test.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(mockSucursal);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("password")).thenReturn("hashedPassword");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);
        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    void testCrearUsuarioSucursal_WithDobleFactorEnabled() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("Test");
        userDTO.setApellido("User");
        userDTO.setFechaNacimiento("1990-01-01");
        userDTO.setDpi("1234567890123");
        userDTO.setCorreo("test@test.com");
        userDTO.setTelefono("12345678");
        userDTO.setDireccionCompleta("Test Address");
        userDTO.setCiudad("Test City");
        userDTO.setPais("Test Country");
        userDTO.setCodigoPostal("12345");
        userDTO.setNombreUsuario("testuser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(true); // Enable 2FA
        userDTO.setEstado("ACTIVO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("password123")).thenReturn("hashedPassword");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(rolRepository).findByNombreRol("SUCURSAL");
    }

    @Test
    void testCrearUsuarioSucursal_WithInactivoEstado() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("Inactive");
        userDTO.setApellido("User");
        userDTO.setFechaNacimiento("1990-01-01");
        userDTO.setDpi("1234567890123");
        userDTO.setCorreo("inactive@test.com");
        userDTO.setTelefono("12345678");
        userDTO.setDireccionCompleta("Test Address");
        userDTO.setCiudad("Test City");
        userDTO.setPais("Test Country");
        userDTO.setCodigoPostal("12345");
        userDTO.setNombreUsuario("inactiveuser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("INACTIVO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("password123")).thenReturn("hashedPassword");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
