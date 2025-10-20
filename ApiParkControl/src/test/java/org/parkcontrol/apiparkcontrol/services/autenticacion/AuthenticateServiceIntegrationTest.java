package org.parkcontrol.apiparkcontrol.services.autenticacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AuthenticateServiceIntegrationTest {

    @Autowired
    private authenticateService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenAuthRepository tokenAutenticacionRepository;

    @MockBean
    private EmailService emailService;

    private Rol clienteRol;
    private Encriptation passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up database
        tokenAutenticacionRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Cliente del sistema");
        clienteRol = rolRepository.save(clienteRol);

        // Initialize password encoder
        passwordEncoder = new Encriptation();
    }

    @Test
    void testRegisterUser_Integration() {
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

        // Act
        Usuario result = authService.registerUser(userDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getIdUsuario());
        assertEquals("testuser", result.getNombreUsuario());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, result.getEstado());
        
        // Verify in database
        Optional<Usuario> savedUser = usuarioRepository.findById(result.getIdUsuario());
        assertTrue(savedUser.isPresent());
        assertEquals("testuser", savedUser.get().getNombreUsuario());
        
        Optional<Persona> savedPersona = personaRepository.findById(result.getPersona().getIdPersona());
        assertTrue(savedPersona.isPresent());
        assertEquals("juan@test.com", savedPersona.get().getCorreo());
    }

    @Test
    void testLoginUser_Integration_WithoutTwoFactor() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("password123");

        // Act
        LoginResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAutenticacion().isAutenticacion());
        assertEquals(testUser.getNombreUsuario(), result.getCredenciales().getNombreUsuario());
        assertEquals("Inicio de sesión exitoso", result.getCredenciales().getMensaje());
        
        // Verify user was updated
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(0, updatedUser.getIntentosFallidos());
        assertNotNull(updatedUser.getUltimaFechaAcceso());
    }

    @Test
    void testLoginUser_Integration_WithTwoFactor() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(true);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("password123");

        // Act
        LoginResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAutenticacion().isAutenticacion());
        assertEquals("Código de verificación enviado al correo", result.getCredenciales().getMensaje());
        assertNotNull(result.getCredenciales().getToken());
        
        // Verify token was created in database
        List<TokenAuth> tokens = tokenAutenticacionRepository.findAll();
        assertEquals(1, tokens.size());
        assertEquals(TokenAuth.TipoToken.DobleFactor, tokens.get(0).getTipoToken());
        assertEquals(TokenAuth.EstadoToken.ACTIVO, tokens.get(0).getEstado());
        
        verify(emailService).sendVerificationCode(eq(testUser.getPersona().getCorreo()), anyString());
    }

    @Test
    void testFull2FAFlow_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(true);
        
        // Step 1: Login with 2FA
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("password123");
        
        LoginResponseDTO loginResult = authService.loginUser(loginRequest);
        String token = loginResult.getCredenciales().getToken();
        
        // Get the verification code from database
        TokenAuth savedToken = tokenAutenticacionRepository.findByTokenHash(token).orElse(null);
        assertNotNull(savedToken);
        String verificationCode = savedToken.getCodigoVerificacion();
        
        // Step 2: Verify 2FA code
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken(token);
        verify2FADTO.setCodigoVerificacion(verificationCode);
        
        // Act
        Map<String, Object> result = authService.verify2FACode(verify2FADTO);
        
        // Assert
        assertNotNull(result);
        assertEquals("Código de verificación autenticado correctamente", result.get("mensaje"));
        assertEquals(testUser.getIdUsuario(), result.get("idUsuario"));
        assertEquals(testUser.getNombreUsuario(), result.get("nombreUsuario"));
        
        // Verify token was marked as used
        TokenAuth updatedToken = tokenAutenticacionRepository.findById(savedToken.getIdToken()).orElse(null);
        assertNotNull(updatedToken);
        assertEquals(TokenAuth.EstadoToken.USADO, updatedToken.getEstado());
    }

    @Test
    void testPasswordRecoveryFlow_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        String originalPasswordHash = testUser.getContraseniaHash();

        // Step 1: Request password recovery
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo(testUser.getPersona().getCorreo());

        Map<String, Object> recoveryResult = authService.recoverPassword(recoverPasswordDTO);
        String token = (String) recoveryResult.get("token");

        // Get the verification code from database
        TokenAuth savedToken = tokenAutenticacionRepository.findByTokenHash(token).orElse(null);
        assertNotNull(savedToken);
        String verificationCode = savedToken.getCodigoVerificacion();

        // Step 2: Verify recovery code
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(token);
        verifyRecoveryDTO.setCodigoVerificacion(verificationCode);

        Map<String, Object> verifyResult = authService.verifyRecoveryCode(verifyRecoveryDTO);

        // Step 3: Reset password
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(testUser.getIdUsuario());
        resetPasswordDTO.setToken(token);
        resetPasswordDTO.setNuevaContrasenia("newpassword123");

        // Act
        authService.resetPassword(resetPasswordDTO);

        // Assert
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        // Verify the password was changed by checking it's different
        assertNotEquals(originalPasswordHash, updatedUser.getContraseniaHash());
        // Also verify the new password can be validated
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getContraseniaHash()));

        verify(emailService).sendRecoveryCode(eq(testUser.getPersona().getCorreo()), anyString());
    }

    @Test
    void testRecoverPassword_EmailFailure_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo(testUser.getPersona().getCorreo());

        // Mock email service to throw exception
        doThrow(new MessagingException("Email error")).when(emailService).sendRecoveryCode(anyString(), anyString());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertTrue(exception.getMessage().contains("Error al enviar el correo electrónico"));
    }

    @Test
    void testVerifyRecoveryCode_ExpiredToken_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo(testUser.getPersona().getCorreo());

        Map<String, Object> recoveryResult = authService.recoverPassword(recoverPasswordDTO);
        String token = (String) recoveryResult.get("token");

        // Manually expire the token
        TokenAuth savedToken = tokenAutenticacionRepository.findByTokenHash(token).orElse(null);
        assertNotNull(savedToken);
        savedToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        tokenAutenticacionRepository.save(savedToken);

        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(token);
        verifyRecoveryDTO.setCodigoVerificacion(savedToken.getCodigoVerificacion());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("El token ha expirado", exception.getMessage());
    }

    @Test
    void testVerifyRecoveryCode_WrongCode_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo(testUser.getPersona().getCorreo());

        Map<String, Object> recoveryResult = authService.recoverPassword(recoverPasswordDTO);
        String token = (String) recoveryResult.get("token");

        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(token);
        verifyRecoveryDTO.setCodigoVerificacion("wrongcode");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyRecoveryCode(verifyRecoveryDTO);
        });
        assertEquals("Código de verificación incorrecto", exception.getMessage());
    }

    @Test
    void testResetPassword_ExpiredToken_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo(testUser.getPersona().getCorreo());

        Map<String, Object> recoveryResult = authService.recoverPassword(recoverPasswordDTO);
        String token = (String) recoveryResult.get("token");

        TokenAuth savedToken = tokenAutenticacionRepository.findByTokenHash(token).orElse(null);
        assertNotNull(savedToken);
        String verificationCode = savedToken.getCodigoVerificacion();

        // Verify the code first
        Verify2FADTO verifyRecoveryDTO = new Verify2FADTO();
        verifyRecoveryDTO.setToken(token);
        verifyRecoveryDTO.setCodigoVerificacion(verificationCode);
        authService.verifyRecoveryCode(verifyRecoveryDTO);

        // Now expire the token manually
        savedToken = tokenAutenticacionRepository.findByTokenHash(token).orElse(null);
        savedToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        tokenAutenticacionRepository.save(savedToken);

        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(testUser.getIdUsuario());
        resetPasswordDTO.setToken(token);
        resetPasswordDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.resetPassword(resetPasswordDTO);
        });
        assertEquals("El token ha expirado", exception.getMessage());
    }

    @Test
    void testLoginUser_WrongPassword_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("wrongpassword");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());

        // Verify user's failed attempts were incremented
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(1, updatedUser.getIntentosFallidos());
    }

    @Test
    void testLoginUser_MultipleFailedAttempts_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("wrongpassword");

        // Act - Try 5 times with wrong password
        for (int i = 0; i < 5; i++) {
            assertThrows(Exception.class, () -> {
                authService.loginUser(loginRequest);
            });
        }

        // Assert - User should be suspended after 5 failed attempts
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(Usuario.EstadoUsuario.SUSPENDIDO, updatedUser.getEstado());
        assertEquals(5, updatedUser.getIntentosFallidos());
    }

    @Test
    void testToggle2FAStatus_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        assertFalse(testUser.isDobleFactorHabilitado());
        
        // Act
        authService.toggle2FAStatus(testUser.getIdUsuario());
        
        // Assert
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.isDobleFactorHabilitado());
        
        // Toggle again
        authService.toggle2FAStatus(testUser.getIdUsuario());
        
        Usuario toggledAgainUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(toggledAgainUser);
        assertFalse(toggledAgainUser.isDobleFactorHabilitado());
    }

    @Test
    void testChangePasswordFirstTime_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        testUser.setDebeCambiarContrasenia(true);
        testUser.setEsPrimeraVez(true);
        usuarioRepository.save(testUser);
        
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(testUser.getIdUsuario());
        changePasswordDTO.setNuevaContrasenia("newpassword123");
        
        // Act
        Map<String, Object> result = authService.changePasswordFirstTime(changePasswordDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals("Contraseña cambiada exitosamente", result.get("mensaje"));
        assertEquals(testUser.getIdUsuario(), result.get("idUsuario"));
        
        Usuario updatedUser = usuarioRepository.findById(testUser.getIdUsuario()).orElse(null);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.isDebeCambiarContrasenia());
        assertFalse(updatedUser.isEsPrimeraVez());
    }

    @Test
    void testChangePasswordFirstTime_UserNotFound_Integration() {
        // Arrange
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(999L); // Non-existent user
        changePasswordDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePasswordFirstTime(changePasswordDTO);
        });
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testLoginUser_UserSuspended_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        testUser.setEstado(Usuario.EstadoUsuario.SUSPENDIDO);
        usuarioRepository.save(testUser);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("password123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("El usuario no está activo. Contacte al administrador.", exception.getMessage());
    }

    @Test
    void testRegisterUser_With2FAEnabled_Integration() {
        // Arrange
        RegisterUserDTO userDTO = new RegisterUserDTO();
        userDTO.setNombre("Carlos");
        userDTO.setApellido("López");
        userDTO.setFechaNacimiento("1985-05-15");
        userDTO.setDpi("9876543210987");
        userDTO.setCorreo("carlos@test.com");
        userDTO.setTelefono("87654321");
        userDTO.setDireccionCompleta("Another Address");
        userDTO.setCiudad("Another City");
        userDTO.setPais("Another Country");
        userDTO.setCodigoPostal("54321");
        userDTO.setNombreUsuario("carlosuser");
        userDTO.setContraseniaHash("password456");
        userDTO.setDobleFactorHabilitado(true);
        userDTO.setEstado("ACTIVO");

        // Act
        Usuario result = authService.registerUser(userDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getIdUsuario());
        assertEquals("carlosuser", result.getNombreUsuario());
        assertTrue(result.isDobleFactorHabilitado());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, result.getEstado());
    }

    @Test
    void testVerify2FACode_TokenNotFound_Integration() throws Exception {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("nonexistenttoken");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Token no encontrado", exception.getMessage());
    }

    @Test
    void testRecoverPassword_UserNotFound_Integration() {
        // Arrange
        RecoverPasswordDTO recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("nonexistent@test.com");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.recoverPassword(recoverPasswordDTO);
        });
        assertEquals("Correo no registrado", exception.getMessage());
    }

    @Test
    void testLoginUser_UserNotFound_Integration() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario("nonexistentuser");
        loginRequest.setContrasenia("password123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.loginUser(loginRequest);
        });
        assertEquals("Nombre de usuario o contraseña incorrectos", exception.getMessage());
    }

    @Test
    void testChangePasswordFirstTime_AlreadyChanged_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        // User already changed password (default is false)
        assertFalse(testUser.isDebeCambiarContrasenia());
        
        ResetPasswordDTO changePasswordDTO = new ResetPasswordDTO();
        changePasswordDTO.setIdUsuario(testUser.getIdUsuario());
        changePasswordDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePasswordFirstTime(changePasswordDTO);
        });
        assertEquals("El usuario ya ha cambiado su contraseña anteriormente", exception.getMessage());
    }

    @Test
    void testLogin_FirstTimeUser_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        testUser.setDebeCambiarContrasenia(true);
        testUser.setEsPrimeraVez(true);
        usuarioRepository.save(testUser);
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUsuario(testUser.getNombreUsuario());
        loginRequest.setContrasenia("password123");

        // Act
        LoginResponseDTO result = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAutenticacion().isDebeCambiarContrasenia());
        assertEquals(testUser.getNombreUsuario(), result.getCredenciales().getNombreUsuario());
    }

    @Test
    void testVerify2FACode_WrongTokenType_Integration() throws Exception {
        // Arrange
        Usuario testUser = createTestUser(false);
        
        // Create a recovery token instead of 2FA token
        TokenAuth recoveryToken = new TokenAuth();
        recoveryToken.setUsuario(testUser);
        recoveryToken.setTokenHash("recoverytoken123");
        recoveryToken.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        recoveryToken.setCodigoVerificacion("123456");
        recoveryToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        recoveryToken.setEstado(TokenAuth.EstadoToken.ACTIVO);
        tokenAutenticacionRepository.save(recoveryToken);

        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("recoverytoken123");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verify2FACode(verify2FADTO);
        });
        assertEquals("Tipo de token incorrecto", exception.getMessage());
    }

    @Test
    void testToggle2FAStatus_UserNotFound_Integration() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.toggle2FAStatus(999L);
        });
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    private Usuario createTestUser(boolean twoFactorEnabled) {
        Persona persona = new Persona();
        persona.setNombre("Test");
        persona.setApellido("User");
        persona.setCorreo("test@test.com");
        persona.setDpi("1234567890123");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setRol(clienteRol);
        usuario.setNombreUsuario("testuser");
        // Use the actual password encoder to encrypt the password
        usuario.setContraseniaHash(passwordEncoder.encrypt("password123"));
        usuario.setDobleFactorHabilitado(twoFactorEnabled);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setDebeCambiarContrasenia(false);
        usuario.setEsPrimeraVez(false);

        return usuarioRepository.save(usuario);
    }
}
