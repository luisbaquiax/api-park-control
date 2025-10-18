package org.parkcontrol.apiparkcontrol.dtoempresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifaBaseResponse {
    private Long idTarifaBase;
    private Long idEmpresa;
    private BigDecimal precioPorHora;
    private String moneda;
    private LocalDateTime fechaVigenciaInicio;
    private LocalDateTime fechaVigenciaFin;
    private TarifaBase.EstadoTarifaBase estado;
    private LocalDateTime fechaCreacion;
}
