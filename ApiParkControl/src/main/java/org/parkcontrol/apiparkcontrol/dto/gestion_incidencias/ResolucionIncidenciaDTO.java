package org.parkcontrol.apiparkcontrol.dto.gestion_incidencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolucionIncidenciaDTO {
    Long idIncidencia;
    Long idUsuarioResuelve;
    String observacionesResolucion;
}

/*
json
{
    "idIncidencia": 1,
    "idUsuarioResuelve": 2,
    "observacionesResolucion": "Incidencia resuelta correctamente."
}
 */