package org.parkcontrol.apiparkcontrol.controllers.autenticacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.parkcontrol.apiparkcontrol.config.TestConfig;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AutenticacionControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenAuthRepository tokenAuthRepository;

    private Rol clienteRol;
    private Persona testPersona;
    private Usuario testUsuario;
    private Encriptation passwordEncoder;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        passwordEncoder = new Encriptation();

        // Clean up database
        tokenAuthRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role CLIENTE
        clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Usuario cliente");
        clienteRol = rolRepository.save(clienteRol);

        // Create test persona
        testPersona = new Persona();
        testPersona.setNombre("Juan");
        testPersona.setApellido("Pérez");
        testPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("juan@test.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Dirección test");
        testPersona.setCiudad("Ciudad test");
        testPersona.setPais("Guatemala");
        testPersona.setCodigoPostal("01001");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        // Create test usuario with properly encrypted password
        testUsuario = new Usuario();
        testUsuario.setPersona(testPersona);
        testUsuario.setRol(clienteRol);
        testUsuario.setNombreUsuario("juantest");
        testUsuario.setContraseniaHash(passwordEncoder.encrypt("password123"));
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange
        RegisterUserDTO registerDTO = new RegisterUserDTO();
        registerDTO.setNombre("María");
        registerDTO.setApellido("García");
        registerDTO.setFechaNacimiento("1995-05-15");
        registerDTO.setDpi(generateUniqueDpi());
        registerDTO.setCorreo("maria@test.com");
        registerDTO.setTelefono("87654321");
        registerDTO.setDireccionCompleta("Dirección María");
        registerDTO.setCiudad("Guatemala");
        registerDTO.setPais("Guatemala");
        registerDTO.setCodigoPostal("01002");
        registerDTO.setNombreUsuario("mariagarcia");
        registerDTO.setContraseniaHash("password456");
        registerDTO.setDobleFactorHabilitado(false);
        registerDTO.setEstado("ACTIVO");

        // Act & Assert
        mockMvc.perform(post("/api/login/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Usuario registrado exitosamente")));
    }

    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        // Arrange
        RegisterUserDTO registerDTO = new RegisterUserDTO();
        registerDTO.setNombre("Pedro");
        registerDTO.setApellido("López");
        registerDTO.setFechaNacimiento("1992-03-10");
        registerDTO.setDpi(generateUniqueDpi());
        registerDTO.setCorreo("pedro@test.com");
        registerDTO.setTelefono("11111111");
        registerDTO.setDireccionCompleta("Dirección Pedro");
        registerDTO.setCiudad("Antigua");
        registerDTO.setPais("Guatemala");
        registerDTO.setCodigoPostal("03001");
        registerDTO.setNombreUsuario("juantest"); // Same as existing user
        registerDTO.setContraseniaHash("password789");
        registerDTO.setDobleFactorHabilitado(false);
        registerDTO.setEstado("ACTIVO");

        // Act & Assert
        mockMvc.perform(post("/api/login/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al registrar el usuario")));
    }

    @Test
    void testLoginUser_Success_WithoutTwoFactor() throws Exception {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("juantest");
        loginDTO.setContrasenia("password123"); // Raw password

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticacion.autenticacion", is(false))) // Corregido: campo correcto
                .andExpect(jsonPath("$.credenciales.mensaje", is("Inicio de sesión exitoso")))
                .andExpect(jsonPath("$.credenciales.idUsuario", is(testUsuario.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.credenciales.nombreUsuario", is("juantest")));
    }

    @Test
    void testLoginUser_Success_WithTwoFactor() throws Exception {
        // Arrange - Enable 2FA for test user
        testUsuario.setDobleFactorHabilitado(true);
        usuarioRepository.save(testUsuario);

        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("juantest");
        loginDTO.setContrasenia("password123");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticacion.autenticacion", is(true))) // Corregido: campo correcto
                .andExpect(jsonPath("$.credenciales.mensaje", is("Código de verificación enviado al correo")))
                .andExpect(jsonPath("$.credenciales.idUsuario", is(testUsuario.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.credenciales.token").exists());
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("juantest");
        loginDTO.setContrasenia("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Nombre de usuario o contraseña incorrectos")));
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("nonexistent");
        loginDTO.setContrasenia("password123");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Nombre de usuario o contraseña incorrectos")));
    }

    @Test
    void testVerify2FA_Success() throws Exception {
        // Arrange - Create token for 2FA
        TokenAuth token = new TokenAuth();
        token.setUsuario(testUsuario);
        token.setTokenHash("testtoken123");
        token.setTipoToken(TokenAuth.TipoToken.DobleFactor);
        token.setCodigoVerificacion("123456");
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        token.setEstado(TokenAuth.EstadoToken.ACTIVO);
        tokenAuthRepository.save(token);

        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("testtoken123");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        mockMvc.perform(post("/api/login/verificar-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify2FADTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Código de verificación autenticado correctamente")))
                .andExpect(jsonPath("$.idUsuario", is(testUsuario.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.nombreUsuario", is("juantest")));
    }

    @Test
    void testVerify2FA_InvalidToken() throws Exception {
        // Arrange
        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("invalidtoken");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        mockMvc.perform(post("/api/login/verificar-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify2FADTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Token no encontrado")));
    }

    @Test
    void testRecoverPassword_Success() throws Exception {
        // Arrange
        RecoverPasswordDTO recoverDTO = new RecoverPasswordDTO();
        recoverDTO.setCorreo("juan@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/login/recuperar-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoverDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Código de recuperación enviado al correo")))
                .andExpect(jsonPath("$.user", is(testUsuario.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRecoverPassword_EmailNotFound() throws Exception {
        // Arrange
        RecoverPasswordDTO recoverDTO = new RecoverPasswordDTO();
        recoverDTO.setCorreo("nonexistent@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/login/recuperar-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoverDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Correo no registrado")));
    }

    @Test
    void testVerifyRecoveryCode_Success() throws Exception {
        // Arrange - Create recovery token
        TokenAuth token = new TokenAuth();
        token.setUsuario(testUsuario);
        token.setTokenHash("recoverytoken123");
        token.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        token.setCodigoVerificacion("654321");
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        token.setEstado(TokenAuth.EstadoToken.ACTIVO);
        tokenAuthRepository.save(token);

        Verify2FADTO verifyDTO = new Verify2FADTO();
        verifyDTO.setToken("recoverytoken123");
        verifyDTO.setCodigoVerificacion("654321");

        // Act & Assert
        mockMvc.perform(post("/api/login/verificar-codigo-recuperacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Código de verificación autenticado correctamente")))
                .andExpect(jsonPath("$.token", is("recoverytoken123")))
                .andExpect(jsonPath("$.idUsuario", is(testUsuario.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.nombreUsuario", is("juantest")));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange - Create used recovery token
        TokenAuth token = new TokenAuth();
        token.setUsuario(testUsuario);
        token.setTokenHash("usedtoken123");
        token.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        token.setCodigoVerificacion("111111");
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        token.setEstado(TokenAuth.EstadoToken.USADO);
        tokenAuthRepository.save(token);

        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setIdUsuario(testUsuario.getIdUsuario());
        resetDTO.setToken("usedtoken123");
        resetDTO.setNuevaContrasenia("newpassword123");

        // Act & Assert
        mockMvc.perform(post("/api/login/resetear-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Contraseña actualizada correctamente")));
    }

    @Test
    void testToggle2FAStatus_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/login/cambiar-2fa/{idUsuario}", testUsuario.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Autenticación de dos factores actualizada correctamente")));
    }

    @Test
    void testToggle2FAStatus_UserNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/login/cambiar-2fa/{idUsuario}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("no encontrado")));
    }

    @Test
    void testChangePasswordFirstTime_Success() throws Exception {
        // Arrange - Set user to require password change
        testUsuario.setDebeCambiarContrasenia(true);
        testUsuario.setEsPrimeraVez(true);
        usuarioRepository.save(testUsuario);

        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setIdUsuario(testUsuario.getIdUsuario());
        resetDTO.setNuevaContrasenia("firsttimepassword");

        // Act & Assert
        mockMvc.perform(put("/api/login/primer-inicio-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Contraseña actualizada correctamente")));
    }

    @Test
    void testChangePasswordFirstTime_AlreadyChanged() throws Exception {
        // Arrange - User already changed password
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        usuarioRepository.save(testUsuario);

        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setIdUsuario(testUsuario.getIdUsuario());
        resetDTO.setNuevaContrasenia("newpassword");

        // Act & Assert
        mockMvc.perform(put("/api/login/primer-inicio-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("ya ha cambiado su contraseña anteriormente")));
    }

    @Test
    void testCompleteWorkflow_Registration_Login_PasswordChange() throws Exception {
        // Step 1: Register new user
        RegisterUserDTO registerDTO = new RegisterUserDTO();
        registerDTO.setNombre("Carlos");
        registerDTO.setApellido("Mendez");
        registerDTO.setFechaNacimiento("1988-12-20");
        registerDTO.setDpi(generateUniqueDpi());
        registerDTO.setCorreo("carlos@test.com");
        registerDTO.setTelefono("99999999");
        registerDTO.setDireccionCompleta("Dirección Carlos");
        registerDTO.setCiudad("Quetzaltenango");
        registerDTO.setPais("Guatemala");
        registerDTO.setCodigoPostal("09001");
        registerDTO.setNombreUsuario("carlosmendez");
        registerDTO.setContraseniaHash("initialpassword");
        registerDTO.setDobleFactorHabilitado(false);
        registerDTO.setEstado("ACTIVO");

        mockMvc.perform(post("/api/login/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // Step 2: Login with new user
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("carlosmendez");
        loginDTO.setContrasenia("initialpassword");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credenciales.nombreUsuario", is("carlosmendez")));

        // Step 3: Test password recovery workflow
        RecoverPasswordDTO recoverDTO = new RecoverPasswordDTO();
        recoverDTO.setCorreo("carlos@test.com");

        mockMvc.perform(post("/api/login/recuperar-contrasenia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoverDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Código de recuperación enviado al correo")));
    }

    @Test
    void testLoginUser_WithEncryptedPassword_Success() throws Exception {
        // This test verifies that the existing testUsuario (with properly encrypted password) works
        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("juantest");
        loginDTO.setContrasenia("password123");

        // Act & Assert
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticacion.autenticacion", is(false))) // Corregido: campo correcto
                .andExpect(jsonPath("$.credenciales.mensaje", is("Inicio de sesión exitoso")))
                .andExpect(jsonPath("$.credenciales.nombreUsuario", is("juantest")));
    }

    @Test
    void testLoginUser_InvalidPassword_FailsAfterMultipleAttempts() throws Exception {
        // Arrange - Create user for failed login test
        Persona failPersona = new Persona();
        failPersona.setNombre("Pedro");
        failPersona.setApellido("Fail");
        failPersona.setFechaNacimiento(LocalDate.of(1992, 3, 10));
        failPersona.setDpi(generateUniqueDpi());
        failPersona.setCorreo("pedro@test.com");
        failPersona.setTelefono("11111111");
        failPersona.setDireccionCompleta("Dirección Pedro");
        failPersona.setCiudad("Antigua");
        failPersona.setPais("Guatemala");
        failPersona.setCodigoPostal("03001");
        failPersona.setEstado(Persona.Estado.ACTIVO);
        failPersona = personaRepository.save(failPersona);

        Usuario failUser = new Usuario();
        failUser.setPersona(failPersona);
        failUser.setRol(clienteRol);
        failUser.setNombreUsuario("pedrofail");
        failUser.setContraseniaHash(passwordEncoder.encrypt("correctpassword"));
        failUser.setDobleFactorHabilitado(false);
        failUser.setEstado(Usuario.EstadoUsuario.ACTIVO);
        failUser.setDebeCambiarContrasenia(false);
        failUser.setEsPrimeraVez(false);
        failUser.setIntentosFallidos(0);
        failUser = usuarioRepository.save(failUser);

        LoginRequestDTO loginDTO = new LoginRequestDTO();
        loginDTO.setNombreUsuario("pedrofail");
        loginDTO.setContrasenia("wrongpassword");

        // Act - Fail 5 times to trigger suspension
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is("error")))
                    .andExpect(jsonPath("$.message", containsString("Nombre de usuario o contraseña incorrectos")));
        }

        // Verify user is suspended
        Usuario updatedUser = usuarioRepository.findById(failUser.getIdUsuario()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(updatedUser);
        org.junit.jupiter.api.Assertions.assertEquals(Usuario.EstadoUsuario.SUSPENDIDO, updatedUser.getEstado());
        org.junit.jupiter.api.Assertions.assertEquals(5, updatedUser.getIntentosFallidos());
    }

    @Test
    void testVerify2FA_ExpiredToken() throws Exception {
        // Arrange - Create expired token
        TokenAuth token = new TokenAuth();
        token.setUsuario(testUsuario);
        token.setTokenHash("expiredtoken123");
        token.setTipoToken(TokenAuth.TipoToken.DobleFactor);
        token.setCodigoVerificacion("123456");
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(5)); // Expired
        token.setEstado(TokenAuth.EstadoToken.ACTIVO);
        tokenAuthRepository.save(token);

        Verify2FADTO verify2FADTO = new Verify2FADTO();
        verify2FADTO.setToken("expiredtoken123");
        verify2FADTO.setCodigoVerificacion("123456");

        // Act & Assert
        mockMvc.perform(post("/api/login/verificar-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify2FADTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("El token ha expirado")));
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
