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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaSucursalServiceEdgeCasesTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private EmpresaSucursalService empresaSucursalService;

    private Empresa mockEmpresa;
    private Rol mockRol;

    @BeforeEach
    void setUp() {
        mockRol = new Rol();
        mockRol.setIdRol(2L);
        mockRol.setNombreRol("SUCURSAL");

        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
    }

    @Test
    void testCrearUsuarioSucursal_WithMinimalData() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("A");
        userDTO.setApellido("B");
        userDTO.setFechaNacimiento("2000-01-01");
        userDTO.setDpi("0000000000000");
        userDTO.setCorreo("a@b.c");
        userDTO.setTelefono("00000000");
        userDTO.setDireccionCompleta("X");
        userDTO.setCiudad("Y");
        userDTO.setPais("Z");
        userDTO.setCodigoPostal("1");
        userDTO.setNombreUsuario("u");
        userDTO.setContraseniaHash("p");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("ACTIVO");

        Persona mockPersona = new Persona();
        mockPersona.setIdPersona(1L);

        Usuario mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("p")).thenReturn("hashedP");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(rolRepository).findByNombreRol("SUCURSAL");
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testCrearUsuarioSucursal_WithExtremeValues() {
        // Arrange
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("VeryLongNameThatExceedsNormalLimits");
        userDTO.setApellido("VeryLongSurnameThatExceedsNormalLimits");
        userDTO.setFechaNacimiento("1900-01-01");
        userDTO.setDpi("9999999999999");
        userDTO.setCorreo("verylongemailaddress@verylongdomainname.com");
        userDTO.setTelefono("99999999");
        userDTO.setDireccionCompleta("Very long address with many details and specifications");
        userDTO.setCiudad("VeryLongCityName");
        userDTO.setPais("VeryLongCountryName");
        userDTO.setCodigoPostal("99999");
        userDTO.setNombreUsuario("verylongusername");
        userDTO.setContraseniaHash("verylongpassword123456789");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("SUSPENDIDO");

        Persona mockPersona = new Persona();
        mockPersona.setIdPersona(1L);

        Usuario mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("verylongpassword123456789")).thenReturn("hashedVeryLongPassword");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_WithComplexSucursalData() {
        // Arrange
        Long idEmpresa = 1L;

        Persona persona = new Persona();
        persona.setNombre("José María");
        persona.setApellido("García-López");
        persona.setFechaNacimiento(LocalDate.of(1975, 12, 31));
        persona.setCorreo("jose.maria@company.co.uk");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Calle Principal #123, Zona 10");
        persona.setCiudad("Ciudad de Guatemala");
        persona.setPais("Guatemala");
        persona.setCodigoPostal("01010");

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setNombreUsuario("jose.maria.garcia");
        usuario.setDobleFactorHabilitado(true);
        usuario.setEstado(Usuario.EstadoUsuario.INACTIVO);

        Sucursal sucursal = new Sucursal();
        sucursal.setIdSucursal(1L);
        sucursal.setEmpresa(mockEmpresa);
        sucursal.setUsuarioSucursal(usuario);
        sucursal.setNombre("Sucursal Central - Centro Histórico");
        sucursal.setDireccionCompleta("Avenida de la Reforma, Centro Histórico");
        sucursal.setCiudad("Ciudad de Guatemala");
        sucursal.setDepartamento("Guatemala");
        sucursal.setHoraApertura(LocalTime.of(6, 30));
        sucursal.setHoraCierre(LocalTime.of(22, 45));
        sucursal.setCapacidad2Ruedas(150);
        sucursal.setCapacidad4Ruedas(100);
        sucursal.setEstado(Sucursal.EstadoSucursal.MANTENIMIENTO);

        when(sucursalRepository.findByEmpresaIdEmpresa(idEmpresa)).thenReturn(Arrays.asList(sucursal));

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ObtenerSucursalesEmpresaDTO dto = result.get(0);
        assertEquals("Sucursal Central - Centro Histórico", dto.getSucursalDTO().getNombre());
        assertEquals("Ciudad de Guatemala", dto.getSucursalDTO().getCiudad());
        assertEquals("Guatemala", dto.getSucursalDTO().getDepartamento());
        assertEquals("06:30", dto.getSucursalDTO().getHoraApertura());
        assertEquals("22:45", dto.getSucursalDTO().getHoraCierre());
        assertEquals(150, dto.getSucursalDTO().getCapacidad2Ruedas());
        assertEquals(100, dto.getSucursalDTO().getCapacidad4Ruedas());
        assertEquals("MANTENIMIENTO", dto.getSucursalDTO().getEstado());

        UsuarioSucursalDTO usuarioDTO = dto.getSucursalDTO().getUsuario();
        assertEquals("José María", usuarioDTO.getNombre());
        assertEquals("García-López", usuarioDTO.getApellido());
        assertEquals("jose.maria@company.co.uk", usuarioDTO.getCorreo());
        assertEquals("jose.maria.garcia", usuarioDTO.getNombreUsuario());
        assertEquals("INACTIVO", usuarioDTO.getEstado());
        assertTrue(usuarioDTO.isDobleFactorHabilitado());
    }

    @Test
    void testCrearNuevaSucursal_WithExtremeTimes() {
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
        dto.setDobleFactorHabilitado(false);
        dto.setNombreSucursal("24/7 Sucursal");
        dto.setDireccionCompletaSucursal("Always Open Address");
        dto.setCiudadSucursal("Night City");
        dto.setDepartamentoSucursal("Sleepless Department");
        dto.setHoraApertura("00:01");
        dto.setHoraCierre("23:58");
        dto.setCapacidad2Ruedas(1);
        dto.setCapacidad4Ruedas(1);
        dto.setLatitud(-90.0);
        dto.setLongitud(-180.0);
        dto.setTelefonoContactoSucursal("00000001");
        dto.setCorreoContactoSucursal("always@open.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(new Sucursal());

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
    void testCrearNuevaSucursal_WithMaximumCapacities() {
        // Arrange
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(1L);
        dto.setNombre("Mega");
        dto.setApellido("Manager");
        dto.setFechaNacimiento("1980-06-15");
        dto.setDpi("5555555555555");
        dto.setCorreo("mega@manager.com");
        dto.setTelefono("55555555");
        dto.setDireccionCompleta("Mega Address");
        dto.setCiudad("Mega City");
        dto.setPais("Mega Country");
        dto.setCodigoPostal("55555");
        dto.setNombreUsuario("megamanager");
        dto.setContraseniaHash("megapass");
        dto.setDobleFactorHabilitado(true);
        dto.setNombreSucursal("Mega Sucursal");
        dto.setDireccionCompletaSucursal("Mega Sucursal Address");
        dto.setCiudadSucursal("Mega Sucursal City");
        dto.setDepartamentoSucursal("Mega Department");
        dto.setHoraApertura("05:00");
        dto.setHoraCierre("23:00");
        dto.setCapacidad2Ruedas(99999);
        dto.setCapacidad4Ruedas(99999);
        dto.setLatitud(90.0);
        dto.setLongitud(180.0);
        dto.setTelefonoContactoSucursal("99999999");
        dto.setCorreoContactoSucursal("mega@sucursal.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(new Sucursal());

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.encrypt("megapass")).thenReturn("hashedMegaPass");
        ReflectionTestUtils.setField(empresaSucursalService, "passwordEncoder", mockEncoder);

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);
        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    void testPasswordEncoderFieldAccess() {
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
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("ACTIVO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        // Don't mock the password encoder to test the real field access
        
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            empresaSucursalService.crearUsuarioSucursal(userDTO);
        });

        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testCrearUsuarioSucursal_WithAllEstadoOptions() {
        // Test INACTIVO estado
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
        userDTO.setContraseniaHash("password");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("INACTIVO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testCrearUsuarioSucursal_WithSuspendidoEstado() {
        // Test SUSPENDIDO estado
        UsuarioSucursalDTO userDTO = new UsuarioSucursalDTO();
        userDTO.setNombre("Suspended");
        userDTO.setApellido("User");
        userDTO.setFechaNacimiento("1985-12-25");
        userDTO.setDpi("9999999999999");
        userDTO.setCorreo("suspended@test.com");
        userDTO.setTelefono("99999999");
        userDTO.setDireccionCompleta("Suspended Address");
        userDTO.setCiudad("Suspended City");
        userDTO.setPais("Suspended Country");
        userDTO.setCodigoPostal("99999");
        userDTO.setNombreUsuario("suspendeduser");
        userDTO.setContraseniaHash("suspendedpass");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("SUSPENDIDO");

        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        // Act
        Usuario result = empresaSucursalService.crearUsuarioSucursal(userDTO);

        // Assert
        assertNotNull(result);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testObtenerUsuariosSucursalPorEmpresa_WithDifferentEstadosSucursal() {
        // Arrange
        Long idEmpresa = 1L;

        // Create sucursales with different estados
        Usuario usuario = new Usuario();
        usuario.setPersona(new Persona());
        usuario.getPersona().setNombre("Test");
        usuario.getPersona().setApellido("User");
        usuario.getPersona().setFechaNacimiento(LocalDate.of(1990, 1, 1));
        usuario.getPersona().setCorreo("test@test.com");
        usuario.getPersona().setTelefono("12345678");
        usuario.getPersona().setDireccionCompleta("Test Address");
        usuario.getPersona().setCiudad("Test City");
        usuario.getPersona().setPais("Test Country");
        usuario.getPersona().setCodigoPostal("12345");
        usuario.setNombreUsuario("testuser");
        usuario.setDobleFactorHabilitado(false);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);

        Sucursal sucursalActiva = new Sucursal();
        sucursalActiva.setIdSucursal(1L);
        sucursalActiva.setEmpresa(mockEmpresa);
        sucursalActiva.setUsuarioSucursal(usuario);
        sucursalActiva.setNombre("Sucursal Activa");
        sucursalActiva.setDireccionCompleta("Address Activa");
        sucursalActiva.setCiudad("City Activa");
        sucursalActiva.setDepartamento("Department Activa");
        sucursalActiva.setHoraApertura(LocalTime.of(8, 0));
        sucursalActiva.setHoraCierre(LocalTime.of(18, 0));
        sucursalActiva.setCapacidad2Ruedas(50);
        sucursalActiva.setCapacidad4Ruedas(30);
        sucursalActiva.setEstado(Sucursal.EstadoSucursal.ACTIVA);

        Sucursal sucursalInactiva = new Sucursal();
        sucursalInactiva.setIdSucursal(2L);
        sucursalInactiva.setEmpresa(mockEmpresa);
        sucursalInactiva.setUsuarioSucursal(usuario);
        sucursalInactiva.setNombre("Sucursal Inactiva");
        sucursalInactiva.setDireccionCompleta("Address Inactiva");
        sucursalInactiva.setCiudad("City Inactiva");
        sucursalInactiva.setDepartamento("Department Inactiva");
        sucursalInactiva.setHoraApertura(LocalTime.of(9, 0));
        sucursalInactiva.setHoraCierre(LocalTime.of(17, 0));
        sucursalInactiva.setCapacidad2Ruedas(40);
        sucursalInactiva.setCapacidad4Ruedas(25);
        sucursalInactiva.setEstado(Sucursal.EstadoSucursal.INACTIVA);

        Sucursal sucursalMantenimiento = new Sucursal();
        sucursalMantenimiento.setIdSucursal(3L);
        sucursalMantenimiento.setEmpresa(mockEmpresa);
        sucursalMantenimiento.setUsuarioSucursal(usuario);
        sucursalMantenimiento.setNombre("Sucursal Mantenimiento");
        sucursalMantenimiento.setDireccionCompleta("Address Mantenimiento");
        sucursalMantenimiento.setCiudad("City Mantenimiento");
        sucursalMantenimiento.setDepartamento("Department Mantenimiento");
        sucursalMantenimiento.setHoraApertura(LocalTime.of(10, 0));
        sucursalMantenimiento.setHoraCierre(LocalTime.of(16, 0));
        sucursalMantenimiento.setCapacidad2Ruedas(30);
        sucursalMantenimiento.setCapacidad4Ruedas(20);
        sucursalMantenimiento.setEstado(Sucursal.EstadoSucursal.MANTENIMIENTO);

        when(sucursalRepository.findByEmpresaIdEmpresa(idEmpresa))
                .thenReturn(Arrays.asList(sucursalActiva, sucursalInactiva, sucursalMantenimiento));

        // Act
        List<ObtenerSucursalesEmpresaDTO> result = empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify all different estados are represented
        List<String> estados = result.stream()
                .map(dto -> dto.getSucursalDTO().getEstado())
                .toList();
        
        assertTrue(estados.contains("ACTIVA"));
        assertTrue(estados.contains("INACTIVA"));
        assertTrue(estados.contains("MANTENIMIENTO"));

        verify(sucursalRepository).findByEmpresaIdEmpresa(idEmpresa);
    }

    @Test
    void testCrearNuevaSucursal_WithZeroCapacities() {
        // Test edge case with zero capacities
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(1L);
        dto.setNombre("Zero");
        dto.setApellido("Capacity");
        dto.setFechaNacimiento("1995-08-20");
        dto.setDpi("0000000000000");
        dto.setCorreo("zero@capacity.com");
        dto.setTelefono("00000000");
        dto.setDireccionCompleta("Zero Address");
        dto.setCiudad("Zero City");
        dto.setPais("Zero Country");
        dto.setCodigoPostal("00000");
        dto.setNombreUsuario("zerouser");
        dto.setContraseniaHash("zeropass");
        dto.setDobleFactorHabilitado(false);
        dto.setNombreSucursal("Zero Capacity Sucursal");
        dto.setDireccionCompletaSucursal("Zero Capacity Address");
        dto.setCiudadSucursal("Zero Capacity City");
        dto.setDepartamentoSucursal("Zero Department");
        dto.setHoraApertura("12:00");
        dto.setHoraCierre("12:01");
        dto.setCapacidad2Ruedas(0);
        dto.setCapacidad4Ruedas(0);
        dto.setLatitud(0.0);
        dto.setLongitud(0.0);
        dto.setTelefonoContactoSucursal("00000000");
        dto.setCorreoContactoSucursal("zero@sucursal.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(new Sucursal());

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);
        verify(sucursalRepository).save(any(Sucursal.class));
    }

    @Test
    void testCrearNuevaSucursal_WithNegativeCoordinates() {
        // Test edge case with negative coordinates
        CreateSucursalDTO dto = new CreateSucursalDTO();
        dto.setIdEmpresa(1L);
        dto.setNombre("Negative");
        dto.setApellido("Coords");
        dto.setFechaNacimiento("1987-11-11");
        dto.setDpi("1111111111111");
        dto.setCorreo("negative@coords.com");
        dto.setTelefono("11111111");
        dto.setDireccionCompleta("Negative Address");
        dto.setCiudad("Negative City");
        dto.setPais("Negative Country");
        dto.setCodigoPostal("11111");
        dto.setNombreUsuario("negativeuser");
        dto.setContraseniaHash("negativepass");
        dto.setDobleFactorHabilitado(true);
        dto.setNombreSucursal("Negative Coords Sucursal");
        dto.setDireccionCompletaSucursal("Negative Coords Address");
        dto.setCiudadSucursal("Negative Coords City");
        dto.setDepartamentoSucursal("Negative Department");
        dto.setHoraApertura("08:30");
        dto.setHoraCierre("17:30");
        dto.setCapacidad2Ruedas(25);
        dto.setCapacidad4Ruedas(15);
        dto.setLatitud(-45.123456);
        dto.setLongitud(-123.654321);
        dto.setTelefonoContactoSucursal("11111111");
        dto.setCorreoContactoSucursal("negative@sucursal.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(rolRepository.findByNombreRol("SUCURSAL")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(new Persona());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(new Sucursal());

        // Act
        String result = empresaSucursalService.crearNuevaSucursal(dto);

        // Assert
        assertEquals("Sucursal creada exitosamente", result);
        verify(sucursalRepository).save(any(Sucursal.class));
    }
}
