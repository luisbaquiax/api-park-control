package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenovacionSuscripcionDTO {
    private Long idSuscripcion;
    private String nuevoPeriodoContratado;
    private String metodoPago;
    private String numeroTransaccion;
}
