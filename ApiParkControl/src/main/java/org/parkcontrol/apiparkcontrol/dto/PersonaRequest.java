package org.parkcontrol.apiparkcontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PersonaRequest {
    //Direccion
    private String direccionCompleta;
    private String ciudad;
    private String pais = "Guatemala";
    private String codigoPostal;

    //Persona

    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento; // LocalDate
    private String dpi;
    private String correo;
    private String telefono;
}
