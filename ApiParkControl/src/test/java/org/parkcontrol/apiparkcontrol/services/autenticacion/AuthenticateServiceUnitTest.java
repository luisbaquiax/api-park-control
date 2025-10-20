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

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateServiceUnitTest {

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
    private TokenAuth mockToken;

    @BeforeEach
    void setUp() {
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Pérez");
        mockPersona.setCorreo("juan@test.com");
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

        mockToken = new TokenAuth();
        mockToken.setIdToken(1L);
        mockToken.setUsuario(mockUsuario);
        mockToken.setTokenHash("testtoken123");
        mockToken.setTipoToken(TokenAuth.TipoToken.DobleFactor);
        mockToken.setCodigoVerificacion("123456");
        mockToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        mockToken.setEstado(TokenAuth.EstadoToken.ACTIVO);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        RegisterUserDTO userDTO = new RegisterUserDTO();
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
        userDTO.setNombreUsuario("testuser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(false);
        userDTO.setEstado("ACTIVO");

        when(rolRepository.findByNombreRol("CLIENTE")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        // Act
        Usuario result = authService.registerUser(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals(mockUsuario.getIdUsuario(), result.getIdUsuario());
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testLoginUser_Success_WithoutTwoFactor() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act
        LoginResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAutenticacion().isAutenticacion());
        assertEquals("testuser", result.getCredenciales().getNombreUsuario());
        verify(usuarioRepository).save(mockUsuario);
    }

    @Test
    void testLoginUser_UserNotFound() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("nonexistent");
        loginRequest.setContrasenia("password123");

        when(usuarioRepository.findByNombreUsuario("nonexistent")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());
    }

    @Test
    void testLoginUser_WrongPassword() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("wrongpassword");

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());
        verify(usuarioRepository).save(mockUsuario);
        assertEquals(1, mockUsuario.getIntentosFallidos());
    }

    @Test
    void testLoginUser_MaxFailedAttempts() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("wrongpassword");
        
        mockUsuario.setIntentosFallidos(4); // One more attempt will suspend

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());
        assertEquals(Usuario.EstadoUsuario.SUSPENDIDO, mockUsuario.getEstado());
    }

    @Test
    void testLoginUser_UserSuspended() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");
        
        mockUsuario.setEstado(Usuario.EstadoUsuario.SUSPENDIDO);

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("El usuario no está activo. Contacte al administrador.", exception.getMessage());
    }

    @Test
    void testLoginUser_With2FA() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");
        
        mockUsuario.setDobleFactorHabilitado(true);

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        when(tokenAutenticacionRepository.save(any(TokenAuth.class))).thenReturn(mockToken);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act
        LoginResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAutenticacion().isAutenticacion());
        assertEquals("Código de verificación enviado al correo", result.getCredenciales().getMensaje());
        verify(emailService).sendVerificationCode(eq("juan@test.com"), anyString());
        verify(tokenAutenticacionRepository).save(any(TokenAuth.class));
    }

    @Test
    void testLoginUser_With2FA_EmailFailure() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");
        
        mockUsuario.setDobleFactorHabilitado(true);

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        when(tokenAutenticacionRepository.save(any(TokenAuth.class))).thenReturn(mockToken);
        doThrow(new MessagingException("Email error")).when(emailService).sendVerificationCode(anyString(), anyString());
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertTrue(exception.getMessage().contains("Error al enviar el correo electrónico"));
    }

    @Test
    void testVerify2FACode_Success() throws Exception {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("testtoken123");
        verify2FADTO.setCodigoVerificacion("123456");

        when(tokenAutenticacionRepository.findByTokenHash("testtoken123")).thenReturn(Optional.of(mockToken));

        // Act
        Map<String, Object> result = authService.verify2FACode(verify2FADTO);

        // Assert
        assertNotNull(result);
        assertEquals("Código de verificación autenticado correctamente", result.get("mensaje"));
        assertEquals(1L, result.get("idUsuario"));
        assertEquals(TokenAuth.EstadoToken.USADO, mockToken.getEstado());
        verify(tokenAutenticacionRepository).save(mockToken);
        verify(usuarioRepository).save(mockUsuario);
    }

    @Test
    void testVerify2FACode_TokenNotFound() {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("invalidtoken");
        verify2FADTO.setCodigoVerificacion("123456");

        when(tokenAutenticacionRepository.findByTokenHash("invalidtoken")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Token no encontrado", exception.getMessage());
    }

    @Test
    void testVerify2FACode_WrongTokenType() {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("testtoken123");
        verify2FADTO.setCodigoVerificacion("123456");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);

        when(tokenAutenticacionRepository.findByTokenHash("testtoken123")).thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Tipo de token incorrecto", exception.getMessage());
    }

    @Test
    void testVerify2FACode_ExpiredToken() {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("testtoken123");
        verify2FADTO.setCodigoVerificacion("123456");
        
        mockToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        when(tokenAutenticacionRepository.findByTokenHash("testtoken123")).thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("El token ha expirado", exception.getMessage());
        assertEquals(TokenAuth.EstadoToken.EXPIRADO, mockToken.getEstado());
        verify(tokenAutenticacionRepository).save(mockToken);
    }

    @Test
    void testVerify2FACode_NullCodigoVerificacion() {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("testtoken123");
        verify2FADTO.setCodigoVerificacion(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }

    @Test
    void testVerify2FACode_EmptyToken() {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }

    @Test
    void testRecoverPassword_Success() throws Exception {
        // Arrange
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("juan@test.com");

        when(personaRepository.findByCorreo("juan@test.com")).thenReturn(mockPersona);
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockUsuario);
        when(tokenAutenticacionRepository.save(any(TokenAuth.class))).thenReturn(mockToken);

        // Act
        Map<String, Object> result = authService.recoverPassword(recoverPasswordDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Código de recuperación enviado al correo", result.get("mensaje"));
        assertEquals(1L, result.get("user"));
        verify(emailService).sendRecoveryCode(eq("juan@test.com"), anyString());
        verify(tokenAutenticacionRepository).save(any(TokenAuth.class));
    }

    @Test
    void testRecoverPassword_EmailNotFound() {
        // Arrange
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("notfound@test.com");

        when(personaRepository.findByCorreo("notfound@test.com")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertEquals("Correo no registrado", exception.getMessage());
    }

    @Test
    void testRecoverPassword_EmptyEmail() {
        // Arrange
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertEquals("El correo es requerido", exception.getMessage());
    }

    @Test
    void testRecoverPassword_EmailFailure() throws Exception {
        // Arrange
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("juan@test.com");

        when(personaRepository.findByCorreo("juan@test.com")).thenReturn(mockPersona);
        when(usuarioRepository.findByPersona(mockPersona)).thenReturn(mockUsuario);
        when(tokenAutenticacionRepository.save(any(TokenAuth.class))).thenReturn(mockToken);
        doThrow(new MessagingException("Email error")).when(emailService).sendRecoveryCode(anyString(), anyString());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertTrue(exception.getMessage().contains("Error al enviar el correo electrónico"));
    }

    @Test
    void testVerifyRecoveryCode_TokenNotFound() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken("invalidtoken");
        verifyRecoveryDTO.setCodigoVerificacion("123456");

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("invalidtoken", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void testVerifyRecoveryCode_InactiveToken() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken("testtoken123");
        verifyRecoveryDTO.setCodigoVerificacion("123456");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        mockToken.setEstado(TokenAuth.EstadoToken.USADO);

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void testVerifyRecoveryCode_ExpiredToken() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken("testtoken123");
        verifyRecoveryDTO.setCodigoVerificacion("123456");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        mockToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("El token ha expirado", exception.getMessage());
        assertEquals(TokenAuth.EstadoToken.EXPIRADO, mockToken.getEstado());
    }

    @Test
    void testVerifyRecoveryCode_WrongCode() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken("testtoken123");
        verifyRecoveryDTO.setCodigoVerificacion("wrongcode");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Código de verificación incorrecto", exception.getMessage());
    }

    @Test
    void testResetPassword_TokenNotFound() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("invalidtoken");
        resetPasswordDTO.setNuevaContrasenia("newpassword123");

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("invalidtoken", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void testResetPassword_WrongUser() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(999L);
        resetPasswordDTO.setToken("testtoken123");
        resetPasswordDTO.setNuevaContrasenia("newpassword123");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        mockToken.setEstado(TokenAuth.EstadoToken.USADO);

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void testResetPassword_InactiveToken() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("testtoken123");
        resetPasswordDTO.setNuevaContrasenia("newpassword123");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        mockToken.setEstado(TokenAuth.EstadoToken.ACTIVO);

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void testResetPassword_ExpiredToken() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("testtoken123");
        resetPasswordDTO.setNuevaContrasenia("newpassword123");
        
        mockToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        mockToken.setEstado(TokenAuth.EstadoToken.USADO);
        mockToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        when(tokenAutenticacionRepository.findByTokenHashAndTipoToken("testtoken123", TokenAuth.TipoToken.RESET_PASSWORD))
                .thenReturn(Optional.of(mockToken));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("El token ha expirado", exception.getMessage());
    }

    @Test
    void testResetPassword_EmptyToken() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("");
        resetPasswordDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("ID de usuario, token y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testResetPassword_EmptyPassword() {
        // Arrange
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("testtoken123");
        resetPasswordDTO.setNuevaContrasenia("");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("ID de usuario, token y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testToggle2FAStatus_UserNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.toggle2FAStatus(999L);
        });
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testToggle2FAStatus_DisableFrom2FA() throws Exception {
        // Arrange
        mockUsuario.setDobleFactorHabilitado(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));

        // Act
        authService.toggle2FAStatus(1L);

        // Assert
        assertFalse(mockUsuario.isDobleFactorHabilitado());
        verify(usuarioRepository).save(mockUsuario);
    }

    @Test
    void testChangePasswordFirstTime_EmptyPassword() {
        // Arrange
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(1L);
        changePasswordDTO.setNuevaContrasenia("");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePasswordFirstTime(changePasswordDTO);
        });
        assertEquals("ID de usuario y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testChangePasswordFirstTime_EmptyUserId() {
        // Arrange
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(null);
        changePasswordDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePasswordFirstTime(changePasswordDTO);
        });
        assertEquals("ID de usuario y nueva contraseña son requeridos", exception.getMessage());
    }

    @Test
    void testLoginUser_UserInactive() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("testuser");
        loginRequest.setContrasenia("password123");
        
        mockUsuario.setEstado(Usuario.EstadoUsuario.INACTIVO);

        when(usuarioRepository.findByNombreUsuario("testuser")).thenReturn(mockUsuario);
        
        // Mock the passwordEncoder field using reflection
        org.parkcontrol.apiparkcontrol.utils.Encriptation mockEncoder = mock(org.parkcontrol.apiparkcontrol.utils.Encriptation.class);
        when(mockEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        ReflectionTestUtils.setField(authService, "passwordEncoder", mockEncoder);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("El usuario no está activo. Contacte al administrador.", exception.getMessage());
    }

    @Test
    void testRegisterUser_WithDifferentEstado() {
        // Arrange
        RegisterUserDTO userDTO = new RegisterUserDTO();
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
        userDTO.setNombreUsuario("testuser");
        userDTO.setContraseniaHash("password123");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("INACTIVO");

        when(rolRepository.findByNombreRol("CLIENTE")).thenReturn(mockRol);
        when(personaRepository.save(any(Persona.class))).thenReturn(mockPersona);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        // Act
        Usuario result = authService.registerUser(userDTO);

        // Assert
        assertNotNull(result);
        verify(personaRepository).save(any(Persona.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testVerifyRecoveryCode_NullParameters() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(null);
        verifyRecoveryDTO.setCodigoVerificacion("123456");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }

    @Test
    void testVerifyRecoveryCode_EmptyCode() {
        // Arrange
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken("testtoken123");
        verifyRecoveryDTO.setCodigoVerificacion("");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Token y código de verificación son requeridos", exception.getMessage());
    }
}
