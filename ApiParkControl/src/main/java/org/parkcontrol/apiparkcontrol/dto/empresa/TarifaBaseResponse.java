package org.parkcontrol.apiparkcontrol.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifaBaseResponse {
    private Long idTarifaBase;
    private Long idEmpresa;
    private BigDecimal precioPorHora;
    private String moneda;
    private LocalDate fechaVigenciaInicio;
    private LocalDate fechaVigenciaFin;
    private TarifaBase.EstadoTarifaBase estado;
}
