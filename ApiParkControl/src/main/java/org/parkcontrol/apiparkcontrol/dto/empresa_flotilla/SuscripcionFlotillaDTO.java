package org.parkcontrol.apiparkcontrol.dto.empresa_flotilla;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionFlotillaDTO {
    private Long idPlanCorporativo;
    private Long idVehiculo;
}
/*
Ejemplo de JSON para SuscripcionFlotillaDTO:
{
    "idPlanCorporativo": 1,
    "idVehiculo": 10
    }
 */