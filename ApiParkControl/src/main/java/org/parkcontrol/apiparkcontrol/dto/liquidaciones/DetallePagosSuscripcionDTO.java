package org.parkcontrol.apiparkcontrol.dto.liquidaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetallePagosSuscripcionDTO {
    private Long idHistorialPago;
    private Long idSuscripcion;
    private String nombreCliente;
    private String fechaPago;
    private String montoPagado;
    private String metodoPago;
    private String numeroTransaccion;
    private String estadoPago;
    private String motivoPago;
}
