package org.parkcontrol.apiparkcontrol.controllers.autenticacion;

import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.services.autenticacion.authenticateService;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
class AutenticacionController {

    private final authenticateService authService;
    @Autowired
    public AutenticacionController(authenticateService authService) {
        this.authService = authService;
    }

    //Registro de usuario
    @PostMapping("/registro")
    public ResponseEntity<?> registerUserController(@RequestBody RegisterUserDTO userDTO) {
        try {
            Usuario usuario = authService.registerUser(userDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("status", "success");

            return ResponseEntity.ok(response);

        }catch(Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al registrar el usuario: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }


    }

    //Login de usuario
    @PostMapping
    public ResponseEntity<?> loginUserController(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.loginUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // Verify 2FA Code
    @PostMapping("/verificar-2fa")
    public ResponseEntity<?> verify2FAController(@RequestBody Verify2FADTO verify2FADTO) {
        try {
            Map<String, Object> response = authService.verify2FACode(verify2FADTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá la traza completa en el log del servidor

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", "Token: " + verify2FADTO.getToken() + ", Código: " + verify2FADTO.getCodigoVerificacion());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // Recover Password
    @PostMapping("/recuperar-contrasenia")
    public ResponseEntity<?> recoverPasswordController(@RequestBody RecoverPasswordDTO recoverPasswordDTO) {
        try {
            Map<String, Object> response = authService.recoverPassword(recoverPasswordDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // Verify Recovery Code
    @PostMapping("/verificar-codigo-recuperacion")
    public ResponseEntity<?> verifyRecoveryCodeController(@RequestBody Verify2FADTO verifyRecoveryDTO) {
        try {
            Map<String, Object> response = authService.verifyRecoveryCode(verifyRecoveryDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // Reset Password
    @PostMapping("/resetear-contrasenia")
    public ResponseEntity<?> resetPasswordController(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        try {
            authService.resetPassword(resetPasswordDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Contraseña actualizada correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // Toggle 2FA Status
    @PutMapping("/cambiar-2fa/{idUsuario}")
    public ResponseEntity<?> toggle2FAStatusController(@PathVariable Long idUsuario) {
        try {
            authService.toggle2FAStatus(idUsuario);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Autenticación de dos factores actualizada correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(e.getMessage().contains("no encontrado") ? 404 : 500).body(errorResponse);
        }
    }

    //Cambiar contraseña primer inicio de sesión
    @PutMapping("/primer-inicio-contrasenia")
    public ResponseEntity<?> changePasswordFirstLoginController(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        try {
            authService.changePasswordFirstTime(resetPasswordDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Contraseña actualizada correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}