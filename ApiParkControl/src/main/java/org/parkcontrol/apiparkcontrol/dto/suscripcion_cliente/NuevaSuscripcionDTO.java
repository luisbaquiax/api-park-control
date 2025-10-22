package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevaSuscripcionDTO {
    private Long idCliente;
    private Long idVehiculo;
    private Long idEmpresa;
    private Long idTipoPlanSuscripcion;
    private String periodoContratado;
    private String metodoPago;
    private String numeroTransaccion;
}
