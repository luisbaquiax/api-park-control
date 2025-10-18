package org.parkcontrol.apiparkcontrol.dto.empresa_sucursal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSucursalDTO {

    //INFORMACION DEL USUARIO DE LA SUCURSAL
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

    //INFORMACION DE LA SUCURSAL
    private Long idEmpresa;
    private String nombreSucursal;
    private String direccionCompletaSucursal;
    private String ciudadSucursal;
    private String departamentoSucursal;
    private String horaApertura; // LocalTime
    private String horaCierre; // LocalTime
    private Integer capacidad2Ruedas;
    private Integer capacidad4Ruedas;
    private Double latitud;
    private Double longitud;
    private String telefonoContactoSucursal;
    private String correoContactoSucursal;
    private String estadoSucursal = "ACTIVO"; //Por defecto ACTIVO

}

/*
Ejemplo json
{
    "direccionCompleta": "Calle 1, Zona 1",
    "ciudad": "Ciudad de Guatemala",
    "pais": "Guatemala",
    "codigoPostal": "01001",
    "nombre": "Juan",
    "apellido": "Perez",
    "fechaNacimiento": "1990-01-01",
    "dpi": "1234567890101",
    "correo": "juanperez@gmail.com",
    "telefono": "55551234",
    "nombreUsuario": "juanperez",
    "contraseniaHash": "juanperez123",
    "dobleFactorHabilitado": false,
    "estado": "ACTIVO",
    "idEmpresa": 1,
    "nombreSucursal": "Sucursal Central",
    "direccionCompletaSucursal": "Avenida Central 123, Zona 1",
    "ciudadSucursal": "Ciudad de Guatemala",
    "departamentoSucursal": "Guatemala",
    "horaApertura": "08:00",
    "horaCierre": "18:00",
    "capacidad2Ruedas": 50,
    "capacidad4Ruedas": 100,
    "latitud": 14.634915,
    "longitud": -90.506882,
    "telefonoContactoSucursal": "55559876",
    "correoContactoSucursal": "sucursalC@gmail.com",
    "estadoSucursal": "ACTIVO"
}
 */