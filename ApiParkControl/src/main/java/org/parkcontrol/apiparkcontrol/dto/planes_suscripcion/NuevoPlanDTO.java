package org.parkcontrol.apiparkcontrol.dto.planes_suscripcion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevoPlanDTO {
    //TIPO PLAN DTO
    private Long idTipoPlan;
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

    //CONFIGURACION DESCUENTO PLAN DTO
    private Long idConfiguracionDescuento;
    private Double descuentoMensual;
    private Double descuentoAnualAdicional;
    private String fechaVigenciaInicio;
    private String fechaVigenciaFin;
    private String estadoConfiguracion;
    private Long idUsuarioCreacion;
    private String fechaCreacionDescuento;


}
