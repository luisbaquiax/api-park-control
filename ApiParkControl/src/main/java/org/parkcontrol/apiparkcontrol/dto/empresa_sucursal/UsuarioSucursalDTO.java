package org.parkcontrol.apiparkcontrol.dto.empresa_sucursal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSucursalDTO {
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
    private boolean dobleFactorHabilitado = false;
    private String estado = "ACTIVO"; //Por defecto ACTIVO
}
