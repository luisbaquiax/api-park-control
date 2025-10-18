package org.parkcontrol.apiparkcontrol.dto.gestion_sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevaTarifaSucursalDTO {
    private Long idUsuarioSucursal;
    private String precioPorHora;
    private String moneda;
    private String fechaVigenciaInicio;
    private String fechaVigenciaFin;
    private boolean esTarifaBase;
}
/*Ejemplo json
{
    "idUsuarioSucursal": 2,
    "precioPorHora": "15.00",
    "moneda": "GTQ",
    "fechaVigenciaInicio": "2024-07-01T00:00:00",
    "fechaVigenciaFin": "2024-12-31T23:59:59",
    "esTarifaBase": false
   }
 */