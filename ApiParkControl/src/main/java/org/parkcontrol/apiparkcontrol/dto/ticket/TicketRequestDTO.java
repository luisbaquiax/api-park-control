package org.parkcontrol.apiparkcontrol.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NotNull
public class TicketRequestDTO {
    private Long idUsuario;// ID del usuario que genera el ticket en la sucursal
    private Long idVehiculo;
    private String tipoCliente;
    private LocalDateTime fechaHoraEntrada;
}
