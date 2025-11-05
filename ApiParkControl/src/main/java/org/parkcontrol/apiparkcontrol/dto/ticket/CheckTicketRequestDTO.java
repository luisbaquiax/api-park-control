package org.parkcontrol.apiparkcontrol.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckTicketRequestDTO {
    private Long idUsuario;// ID del usuario que realiza la verificación (sucursal o personal autorizado)
    private String placa;
    private String codigoQr;
    private String folio;
    private String metodoPago;// Efectivo, tarjeta, app, etc.
}
