package org.parkcontrol.apiparkcontrol.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CobroResultadoDTO {
    private BigDecimal horasCobradas;
    private BigDecimal tarifaAplicada;
    private BigDecimal subtotal;
    private BigDecimal totalAPagar;
    private BigDecimal horasAcreditadas;
    private LocalDate fecha;
    private LocalDate horaEntrada;
    private LocalDate horaSalida;
}
