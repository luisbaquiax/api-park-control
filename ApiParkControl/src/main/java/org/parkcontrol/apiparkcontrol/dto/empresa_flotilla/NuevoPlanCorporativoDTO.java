package org.parkcontrol.apiparkcontrol.dto.empresa_flotilla;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevoPlanCorporativoDTO {
    private Long idEmpresaFlotilla;
    private Long idTipoPlan;
    private String nombrePlanCorporativo;
    private Integer numeroPlacasContratadas;
    private Double descuentoCorporativoAdicional;
    private Double precioPlanCorporativo;
    private String fechaInicio;
    private String fechaFin;
    private Long idCreadoPor;
}
/*
Ejemplo de JSON para NuevoPlanCorporativoDTO:
{
    "idEmpresaFlotilla": 1,
    "idTipoPlan": 2,
    "nombrePlanCorporativo": "Plan Corporativo Premium",
    "numeroPlacasContratadas": 50,
    "descuentoCorporativoAdicional": 10.5,
    "precioPlanCorporativo": 1500.00,
    "fechaInicio": "2024-07-01T00:00:00",
    "fechaFin": "2025-06-30T23:59:59",
    "idCreadoPor": 3
}
 */