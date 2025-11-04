package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteSuscripcionesDTO {
    private Long idTipoPlan;
    private String nombrePlan;
    private String codigoPlan;
    private String descripcion;
    private Double precioPlan;
    private Integer horasDia;
    private Integer horasMensuales;
    private String diasAplicables;
    private String coberturaHoraria;
    private Integer ordenBeneficio;
    private String estadoPlan;
    private String fechaCreacion;
    private DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configuracionDescuento;
    private List<DetalleSuscriptoresDTO> detallesSuscriptores;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalleSuscriptoresDTO {
        private Long idSuscripcion;
        private String nombreSuscriptor;
        private String placaVehiculo;
        private Double montoTarifaReferenciada;
        private Integer horasMensualesIncluidas;
        private Integer horasUtilizadasMes;
        private String fechaInicioSuscripcion;
        private String fechaFinSuscripcion;
        private Integer totalHorasUtilizadas;
        private Double totalExcedentePagado;
        private String estadoSuscripcion;
    }




}
