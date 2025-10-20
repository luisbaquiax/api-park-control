package org.parkcontrol.apiparkcontrol.dto.planes_suscripcion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleTipoPlanDTO {
    private Long id;
    private Long idEmpresa;
    private String nombrePlan;
    private String codigoPlan;
    private String descripcion;
    private Integer horasMensuales;
    private String diasAplicables;
    private String coberturaHoraria;
    private Integer ordenBeneficio;
    private String activo;
    private String fechaCreacion;
    private ConfiguracionDescuentoDTO configuracionDescuento;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfiguracionDescuentoDTO {
        private Long idConfiguracionDescuento;
        private Double descuentoMensual;
        private Double descuentoAnualAdicional;
        private String fechaVigenciaInicio;
        private String fechaVigenciaFin;
        private String estadoConfiguracion;
        private Long idUsuarioCreacion;
        private String fechaCreacionDescuento;
    }

}
