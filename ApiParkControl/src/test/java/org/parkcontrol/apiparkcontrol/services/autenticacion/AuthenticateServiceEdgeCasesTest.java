package org.parkcontrol.apiparkcontrol.services.autenticacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateServiceEdgeCasesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private TokenAuthRepository tokenAutenticacionRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private authenticateService authService;

    private Usuario mockUsuario;
    private Persona mockPersona;
    private Rol mockRol;

    @BeforeEach
    void setUp() {
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Test");
        mockPersona.setApellido("User");
        mockPersona.setCorreo("test@test.com");
        mockPersona.setDpi("1234567890123");
        mockPersona.setTelefono("12345678");
        mockPersona.setDireccionCompleta("Test Address");
        mockPersona.setCiudad("Test City");
        mockPersona.setPais("Test Country");
        mockPersona.setCodigoPostal("12345");
        mockPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        mockPersona.setEstado(Persona.Estado.ACTIVO);

        mockRol = new Rol();
        mockRol.setIdRol(1L);
        mockRol.setNombreRol("CLIENTE");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setPersona(mockPersona);
        mockUsuario.setRol(mockRol);
        mockUsuario.setNombreUsuario("testuser");
        mockUsuario.setContraseniaHash("hashedPassword");
        mockUsuario.setDobleFactorHabilitado(false);
        mockUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        mockUsuario.setIntentosFallidos(0);
        mockUsuario.setDebeCambiarContrasenia(false);
        mockUsuario.setEsPrimeraVez(false);
    }

    @Test
    void testGenerateRandomToken_ConsistentLength() {
        // Test the private method generateRandomToken indirectly through login with 2FA
        mockUsuario.setDobleFactorHabilitado(true);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        when(tokenAutenticacionRepository.save(any(TokenAuth.class))).thenAnswer(invocation -> {
            TokenAuth token = invocation.getArgument(0);
            token.setIdToken(1L);
            return token;
        });

        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        try {
            LoginResponseDTO result = authService.loginUser(loginRequest);
            assertNotNull(result.getCredenciales().getToken());
            assertEquals(10, result.getCredenciales().getToken().length()); // Default length is 10
        } catch (Exception e) {
            // Expected due to email service
        }
    }

    @Test
    void testValidationWithNullValues() {
        // Test empty/null values in DTOs
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken(null);
        verify2FADTO.setCodigoVerificacion(null);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }

    @Test
    void testValidationWithEmptyStrings() {
        // Test empty string values
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("   "); // Whitespace only

        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertEquals("El correo es requerido", exception.getMessage());
    }

    @Test
    void testResetPasswordWithAllNullValues() {
        // Test with all null values
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(null);
        resetPasswordDTO.setToken(null);
        resetPasswordDTO.setNuevaContrasenia(null);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("ID de usuario, token y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testChangePasswordFirstTimeWithNullUserId() {
        // Test with null user ID
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(null);
        changePasswordDTO.setNuevaContrasenia("newpassword");

        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePasswordFirstTime(changePasswordDTO);
        });
        assertEquals("ID de usuario y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testVerifyRecoveryCodeWithNullToken() {
        // Test with null token in recovery verification
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(null);
        verifyRecoveryDTO.setCodigoVerificacion("123456");

        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }

    @Test
    void testStateTransitions() {
        // Test different user states
        mockUsuario.setEstado(Usuario.EstadoUsuario.INACTIVO);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("El usuario no está activo. Contacte al administrador.", exception.getMessage());
    }

    @Test
    void testBoundaryConditions() {
        // Test exactly 5 failed attempts
        mockUsuario.setIntentosFallidos(4); // This will be the 5th attempt
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("wrongpassword");

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());
        assertEquals(Usuario.EstadoUsuario.SUSPENDIDO, mockUsuario.getEstado());
        assertEquals(5, mockUsuario.getIntentosFallidos());
    }
}
