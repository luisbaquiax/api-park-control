package org.parkcontrol.apiparkcontrol.dto.liquidaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleTransaccionTicketDTO {
    private Long idTransaccion;
    private Long idTicket;
    private String nombreCliente;
    private String tipoCobro;
    private String horasCobradas;
    private String horasGratisComercio;
    private String tarifaAplicada;
    private String subtotal;
    private String descuento;
    private String total;
    private String metodoPago;
    private String numeroTransaccion;
    private String estado;
    private String fechaTransaccion;
}
