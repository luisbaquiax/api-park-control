package org.parkcontrol.apiparkcontrol.controllers.autenticacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.services.autenticacion.authenticateService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacionControllerUnitTest {

    @Mock
    private authenticateService authService;

    @InjectMocks
    private AutenticacionController autenticacionController;

    private RegisterUserDTO registerUserDTO;
    private LoginRequestDTO loginRequestDTO;
    private Verify2FADTO verify2FADTO;
    private RecoverPasswordDTO recoverPasswordDTO;
    private ResetPasswordDTO resetPasswordDTO;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        // Setup RegisterUserDTO
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setNombre("Juan");
        registerUserDTO.setApellido("Pérez");
        registerUserDTO.setFechaNacimiento("1990-01-01");
        registerUserDTO.setDpi("1234567890123");
        registerUserDTO.setCorreo("juan@test.com");
        registerUserDTO.setTelefono("12345678");
        registerUserDTO.setDireccionCompleta("Dirección test");
        registerUserDTO.setCiudad("Ciudad test");
        registerUserDTO.setPais("Guatemala");
        registerUserDTO.setCodigoPostal("01001");
        registerUserDTO.setNombreUsuario("juanperez");
        registerUserDTO.setContraseniaHash("password123");
        registerUserDTO.setDobleFactorHabilitado(false);
        registerUserDTO.setEstado("ACTIVO");

        // Setup LoginRequestDTO
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setNombreUsuario("juanperez");
        loginRequestDTO.setContrasenia("password123");

        // Setup Verify2FADTO
        verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("abc123");
        verify2FADTO.setCodigoVerificacion("123456");

        // Setup RecoverPasswordDTO
        recoverPasswordDTO = new RecoverPasswordDTO();
        recoverPasswordDTO.setCorreo("juan@test.com");

        // Setup ResetPasswordDTO
        resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setIdUsuario(1L);
        resetPasswordDTO.setToken("abc123");
        resetPasswordDTO.setNuevaContrasenia("newPassword123");

        // Setup mock Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("juanperez");
    }

    @Test
    void testRegisterUserController_Success() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterUserDTO.class))).thenReturn(mockUsuario);

        // Act
        ResponseEntity<?> response = autenticacionController.registerUserController(registerUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Usuario registrado exitosamente", responseBody.get("message"));
        assertEquals("success", responseBody.get("status"));

        verify(authService).registerUser(registerUserDTO);
    }

    @Test
    void testRegisterUserController_Exception() throws Exception {
        // Arrange
        when(authService.registerUser(any(RegisterUserDTO.class)))
                .thenThrow(new RuntimeException("Error en el servicio"));

        // Act
        ResponseEntity<?> response = autenticacionController.registerUserController(registerUserDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(((String) responseBody.get("message")).contains("Error al registrar el usuario"));
        assertEquals("error", responseBody.get("status"));

        verify(authService).registerUser(registerUserDTO);
    }

    @Test
    void testLoginUserController_Success() throws Exception {
        // Arrange
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .autenticacion(new LoginResponseDTO.Autenticacion(false, false))
                .credenciales(LoginResponseDTO.Credenciales.builder()
                        .mensaje("Inicio de sesión exitoso")
                        .idUsuario(1L)
                        .rol(1L)
                        .nombreRol("CLIENTE")
                        .nombreUsuario("juanperez")
                        .build())
                .build();

        when(authService.loginUser(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.loginUserController(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());

        verify(authService).loginUser(loginRequestDTO);
    }

    @Test
    void testLoginUserController_With2FA() throws Exception {
        // Arrange
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .autenticacion(new LoginResponseDTO.Autenticacion(true, false))
                .credenciales(LoginResponseDTO.Credenciales.builder()
                        .mensaje("Código de verificación enviado al correo")
                        .idUsuario(1L)
                        .token("abc123")
                        .build())
                .build();

        when(authService.loginUser(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.loginUserController(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());

        verify(authService).loginUser(loginRequestDTO);
    }

    @Test
    void testLoginUserController_Exception() throws Exception {
        // Arrange
        when(authService.loginUser(any(LoginRequestDTO.class)))
                .thenThrow(new Exception("Usuario no encontrado"));

        // Act
        ResponseEntity<?> response = autenticacionController.loginUserController(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Usuario no encontrado", responseBody.get("message"));

        verify(authService).loginUser(loginRequestDTO);
    }

    @Test
    void testVerify2FAController_Success() throws Exception {
        // Arrange
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("mensaje", "Código de verificación autenticado correctamente");
        serviceResponse.put("idUsuario", 1L);
        serviceResponse.put("rol", 1L);
        serviceResponse.put("nombreRol", "CLIENTE");
        serviceResponse.put("nombreUsuario", "juanperez");

        when(authService.verify2FACode(any(Verify2FADTO.class))).thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.verify2FAController(verify2FADTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());

        verify(authService).verify2FACode(verify2FADTO);
    }

    @Test
    void testVerify2FAController_Exception() throws Exception {
        // Arrange
        when(authService.verify2FACode(any(Verify2FADTO.class)))
                .thenThrow(new Exception("Código de verificación incorrecto"));

        // Act
        ResponseEntity<?> response = autenticacionController.verify2FAController(verify2FADTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Código de verificación incorrecto", responseBody.get("message"));
        assertTrue(responseBody.containsKey("details"));

        verify(authService).verify2FACode(verify2FADTO);
    }

    @Test
    void testRecoverPasswordController_Success() throws Exception {
        // Arrange
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("mensaje", "Código de recuperación enviado al correo");
        serviceResponse.put("user", 1L);
        serviceResponse.put("token", "abc123");

        when(authService.recoverPassword(any(RecoverPasswordDTO.class))).thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.recoverPasswordController(recoverPasswordDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());

        verify(authService).recoverPassword(recoverPasswordDTO);
    }

    @Test
    void testRecoverPasswordController_Exception() throws Exception {
        // Arrange
        when(authService.recoverPassword(any(RecoverPasswordDTO.class)))
                .thenThrow(new Exception("Correo no registrado"));

        // Act
        ResponseEntity<?> response = autenticacionController.recoverPasswordController(recoverPasswordDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Correo no registrado", responseBody.get("message"));

        verify(authService).recoverPassword(recoverPasswordDTO);
    }

    @Test
    void testVerifyRecoveryCodeController_Success() throws Exception {
        // Arrange
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("mensaje", "Código de verificación autenticado correctamente");
        serviceResponse.put("token", "abc123");
        serviceResponse.put("idUsuario", 1L);
        serviceResponse.put("nombreUsuario", "juanperez");

        when(authService.verifyRecoveryCode(any(Verify2FADTO.class))).thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.verifyRecoveryCodeController(verify2FADTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());

        verify(authService).verifyRecoveryCode(verify2FADTO);
    }

    @Test
    void testVerifyRecoveryCodeController_Exception() throws Exception {
        // Arrange
        when(authService.verifyRecoveryCode(any(Verify2FADTO.class)))
                .thenThrow(new Exception("Token inválido"));

        // Act
        ResponseEntity<?> response = autenticacionController.verifyRecoveryCodeController(verify2FADTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Token inválido", responseBody.get("message"));

        verify(authService).verifyRecoveryCode(verify2FADTO);
    }

    @Test
    void testResetPasswordController_Success() throws Exception {
        // Arrange
        doNothing().when(authService).resetPassword(any(ResetPasswordDTO.class));

        // Act
        ResponseEntity<?> response = autenticacionController.resetPasswordController(resetPasswordDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Contraseña actualizada correctamente", responseBody.get("message"));

        verify(authService).resetPassword(resetPasswordDTO);
    }

    @Test
    void testResetPasswordController_Exception() throws Exception {
        // Arrange
        doThrow(new Exception("Token expirado")).when(authService).resetPassword(any(ResetPasswordDTO.class));

        // Act
        ResponseEntity<?> response = autenticacionController.resetPasswordController(resetPasswordDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Token expirado", responseBody.get("message"));

        verify(authService).resetPassword(resetPasswordDTO);
    }

    @Test
    void testToggle2FAStatusController_Success() throws Exception {
        // Arrange
        Long idUsuario = 1L;
        doNothing().when(authService).toggle2FAStatus(idUsuario);

        // Act
        ResponseEntity<?> response = autenticacionController.toggle2FAStatusController(idUsuario);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Autenticación de dos factores actualizada correctamente", responseBody.get("message"));

        verify(authService).toggle2FAStatus(idUsuario);
    }

    @Test
    void testToggle2FAStatusController_UserNotFound() throws Exception {
        // Arrange
        Long idUsuario = 999L;
        doThrow(new Exception("Usuario no encontrado")).when(authService).toggle2FAStatus(idUsuario);

        // Act
        ResponseEntity<?> response = autenticacionController.toggle2FAStatusController(idUsuario);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Usuario no encontrado", responseBody.get("message"));

        verify(authService).toggle2FAStatus(idUsuario);
    }

    @Test
    void testToggle2FAStatusController_OtherException() throws Exception {
        // Arrange
        Long idUsuario = 1L;
        doThrow(new Exception("Error interno")).when(authService).toggle2FAStatus(idUsuario);

        // Act
        ResponseEntity<?> response = autenticacionController.toggle2FAStatusController(idUsuario);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Error interno", responseBody.get("message"));

        verify(authService).toggle2FAStatus(idUsuario);
    }

    @Test
    void testChangePasswordFirstLoginController_Success() throws Exception {
        // Arrange
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("mensaje", "Contraseña cambiada exitosamente");
        serviceResponse.put("idUsuario", 1L);
        serviceResponse.put("rol", 1L);
        serviceResponse.put("nombreRol", "CLIENTE");
        serviceResponse.put("nombreUsuario", "juanperez");

        when(authService.changePasswordFirstTime(any(ResetPasswordDTO.class))).thenReturn(serviceResponse);

        // Act
        ResponseEntity<?> response = autenticacionController.changePasswordFirstLoginController(resetPasswordDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Contraseña actualizada correctamente", responseBody.get("message"));

        verify(authService).changePasswordFirstTime(resetPasswordDTO);
    }

    @Test
    void testChangePasswordFirstLoginController_Exception() throws Exception {
        // Arrange
        when(authService.changePasswordFirstTime(any(ResetPasswordDTO.class)))
                .thenThrow(new Exception("El usuario ya ha cambiado su contraseña anteriormente"));

        // Act
        ResponseEntity<?> response = autenticacionController.changePasswordFirstLoginController(resetPasswordDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("El usuario ya ha cambiado su contraseña anteriormente", responseBody.get("message"));

        verify(authService).changePasswordFirstTime(resetPasswordDTO);
    }

    @Test
    void testVerify2FAController_WithNullToken() throws Exception {
        // Arrange
        Verify2FADTO nullTokenDTO = new Verify2FADTO();
        nullTokenDTO.setToken(null);
        nullTokenDTO.setCodigoVerificacion("123456");
        
        when(authService.verify2FACode(any(Verify2FADTO.class)))
                .thenThrow(new Exception("Token y código de verificación son requeridos"));

        // Act
        ResponseEntity<?> response = autenticacionController.verify2FAController(nullTokenDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Token y código de verificación son requeridos", responseBody.get("message"));

        verify(authService).verify2FACode(nullTokenDTO);
    }

    @Test
    void testVerify2FAController_WithEmptyCode() throws Exception {
        // Arrange
        Verify2FADTO emptyCodeDTO = new Verify2FADTO();
        emptyCodeDTO.setToken("abc123");
        emptyCodeDTO.setCodigoVerificacion("");
        
        when(authService.verify2FACode(any(Verify2FADTO.class)))
                .thenThrow(new Exception("Token y código de verificación son requeridos"));

        // Act
        ResponseEntity<?> response = autenticacionController.verify2FAController(emptyCodeDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(authService).verify2FACode(emptyCodeDTO);
    }

    @Test
    void testRecoverPasswordController_WithNullEmail() throws Exception {
        // Arrange
        RecoverPasswordDTO nullEmailDTO = new RecoverPasswordDTO();
        nullEmailDTO.setCorreo(null);
        
        when(authService.recoverPassword(any(RecoverPasswordDTO.class)))
                .thenThrow(new Exception("El correo es requerido"));

        // Act
        ResponseEntity<?> response = autenticacionController.recoverPasswordController(nullEmailDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(authService).recoverPassword(nullEmailDTO);
    }

    @Test
    void testResetPasswordController_WithNullFields() throws Exception {
        // Arrange
        ResetPasswordDTO nullFieldsDTO = new ResetPasswordDTO();
        nullFieldsDTO.setIdUsuario(null);
        nullFieldsDTO.setToken(null);
        nullFieldsDTO.setNuevaContrasenia(null);
        
        doThrow(new Exception("ID de usuario, token y nueva contraseña son requeridos"))
                .when(authService).resetPassword(any(ResetPasswordDTO.class));

        // Act
        ResponseEntity<?> response = autenticacionController.resetPasswordController(nullFieldsDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(authService).resetPassword(nullFieldsDTO);
    }
}
