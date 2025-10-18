package org.parkcontrol.apiparkcontrol.dto.gestion_sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifaSucursalDTO {
    private Long idUsuarioSucursal;
    private Long idTarifaSucursal;
    private Double precioPorHora;
    private String moneda;
    private String fechaVigenciaInicio;
    private String fechaVigenciaFin;
    private String estado;
}

/*Ejemplo json
{
    "idUsuarioSucursal": 2,
    "idTarifaSucursal": 5,
    "precioPorHora": 15.00,
    "moneda": "GTQ",
    "fechaVigenciaInicio": "2024-07-01T00:00:00",
    "fechaVigenciaFin": "2024-12-31T23:59:59",
    "estado": "PROGRAMADO"
   }
 */