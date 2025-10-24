package org.parkcontrol.apiparkcontrol.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BitacoraTarifaBaseResponse {
    private Long idBitacoraTarifa;

    private Long idTarifaBase;

    private BitacoraTarifaBase.Accion accion;

    private BigDecimal precioAnterior;

    private BigDecimal precioNuevo;

    private Long idUsuarioResponsable;

    private LocalDateTime fechaCambio;

    private String observaciones;
}
