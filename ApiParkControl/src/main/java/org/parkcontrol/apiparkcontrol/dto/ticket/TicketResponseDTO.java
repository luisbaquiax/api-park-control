package org.parkcontrol.apiparkcontrol.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private String folioNumerico;
    private Long idSucursal;
    private Long idVehiculo;
    private Long idSuscripcion;
    private Long idPermisoTemporal;
    private String tipoCliente;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;
    private Integer duracionMinutos;
    private String codigoQr;
    private String enlaceSmsWhatsapp;
    private String estado;
    private LocalDateTime fechaCreacion;
}
