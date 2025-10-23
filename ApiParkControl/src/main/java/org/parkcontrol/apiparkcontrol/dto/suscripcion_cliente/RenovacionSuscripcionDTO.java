package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenovacionSuscripcionDTO {
    private Long idSuscripcion;
    private String nuevoPeriodoContratado;
    private String metodoPago;
    private String numeroTransaccion;
}
/*Ejemplo json Renovacion Suscriocion
{
    "idSuscripcion": 10,
    "nuevoPeriodoContratado": "MENSUAL",
    "metodoPago": "TARJETA_DEBITO",
    "numeroTransaccion": "TR-7890"
}
*/