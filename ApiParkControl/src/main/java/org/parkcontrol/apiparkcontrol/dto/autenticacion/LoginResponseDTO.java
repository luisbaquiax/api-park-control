package org.parkcontrol.apiparkcontrol.dto.autenticacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private Autenticacion autenticacion;
    private Credenciales credenciales;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Autenticacion {
        private boolean autenticacion;
        private boolean debeCambiarContrasenia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Credenciales {
        private String mensaje;
        private Long idUsuario;
        private Long rol;
        private String nombreRol;
        private String nombreUsuario;
        private String token;

    }
}
