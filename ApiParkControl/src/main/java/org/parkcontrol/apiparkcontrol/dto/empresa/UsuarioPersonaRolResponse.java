package org.parkcontrol.apiparkcontrol.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioPersonaRolResponse {
    private Long idUsuario;
    private Long idPersona;
    private String dpi;
    private String nombreUsuario;
    private String nombreRol;
    private String nombre;
    private String apellido;
}
