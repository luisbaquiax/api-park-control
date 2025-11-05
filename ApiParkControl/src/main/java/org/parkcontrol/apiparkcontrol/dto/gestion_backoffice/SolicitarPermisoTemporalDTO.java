package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitarPermisoTemporalDTO {
    private Long idSuscripcion;
    private String placaTemporal;
    private String tipoVehiculoPermitido;
    private String motivo;
}

/*
Ejemplo JSON:
{
    "idSuscripcion": 123,
    "placaTemporal": "ABC-1234",
    "tipoVehiculoPermitido": "CUATRO_RUEDAS",
    "motivo": "Visita de negocios"
    }
 */