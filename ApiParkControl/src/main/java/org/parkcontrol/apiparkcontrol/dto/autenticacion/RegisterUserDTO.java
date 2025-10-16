package org.parkcontrol.apiparkcontrol.dto.autenticacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDTO {
    //Direccion, Persona, Usuario

    //Direccion
    private String direccionCompleta;
    private String ciudad;
    private String pais = "Guatemala";
    private String codigoPostal;

    //Persona

    private String nombre;
    private String apellido;
    private String fechaNacimiento; // LocalDate
    private String dpi;
    private String correo;
    private String telefono;

    //Usuario
    private String nombreUsuario;
    private String contraseniaHash;
    private Long idRol; //Por defecto 2 (Cliente)
    private boolean dobleFactorHabilitado = false;
    private String estado = "ACTIVO"; //Por defecto ACTIVO
}
