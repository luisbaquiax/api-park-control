package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaSuscripcionDTO {
    private Long idCliente;
    private Long idVehiculo;
    private Long idEmpresa;
    private Long idTipoPlanSuscripcion;
    private String periodoContratado;
    private String metodoPago;
    private String numeroTransaccion;
}
/*
Ejemplo json Nueva Suscriocion
{
    idCliente: 1,
    idVehiculo: 17,
    idEmpresa: 1,
    idTipoPlanSuscripcion: 5,
    periodoContratado: 'ANUAL',
    metodoPago: 'TARJETA_CREDITO',
    numeroTransaccion: 'TR-5124'
}
 */