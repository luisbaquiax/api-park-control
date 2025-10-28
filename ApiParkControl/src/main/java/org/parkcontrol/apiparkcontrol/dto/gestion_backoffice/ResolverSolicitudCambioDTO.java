package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolverSolicitudCambioDTO {
    private Long idUsuarioBackoffice;
    private Long idSolicitudCambio;
    private String estado; // Aprobado o Rechazado
    private String observacionesRevision;
}

/*
ejemplo json para resolver una solicitud de cambio de placa:
{
    "idUsuarioBackoffice": 5,
    "idSolicitudCambio": 1,
    "estado": "Aprobado",
    "observacionesRevision": "Solicitud aprobada. La nueva placa ha sido registrada."
}
 */