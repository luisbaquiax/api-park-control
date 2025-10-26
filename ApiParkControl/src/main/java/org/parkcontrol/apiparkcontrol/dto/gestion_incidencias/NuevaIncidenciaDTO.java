package org.parkcontrol.apiparkcontrol.dto.gestion_incidencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevaIncidenciaDTO {
    //Variables para crear una nueva incidencia
    Long idTicket;
    String tipoIncidencia;
    String descripcion;

    //Variables para crear una nueva evidencia asociada a la incidencia
    String tipoEvidencia;
    String descripcionEvidencia;

}

/*
Json de ejemplo para crear una nueva incidencia con evidencia
{
    "idTicket": 1,
    "tipoIncidencia": "OTRO",
    "descripcion": "El vehículo sufrió un daño en el parquímetro.",
    "tipoEvidencia": "FOTO_VEHICULO",
    "descripcionEvidencia": "Foto del daño en el parquímetro."
}
 */
