package org.parkcontrol.apiparkcontrol.dto.gestion_sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditarSucursalDTO {
    private Long idSucursal;
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
    private String estadoSucursal;
}

/*{
    "idSucursal": 1,
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
    "correoContactoSucursal": null,
    "estadoSucursal": "ACTIVA"
   }*/

