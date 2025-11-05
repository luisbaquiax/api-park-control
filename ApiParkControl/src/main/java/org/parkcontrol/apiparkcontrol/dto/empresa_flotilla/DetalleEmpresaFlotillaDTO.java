package org.parkcontrol.apiparkcontrol.dto.empresa_flotilla;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.PlanCorporativo;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEmpresaFlotillaDTO {
    private List<EmpresaFlotillaDTO> empresasFlotilla;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpresaFlotillaDTO {
        private Long idEmpresaFlotilla;
        private String nombreEmpresa;
        private String razonSocial;
        private String nit;
        private String telefono;
        private String correoContacto;
        private String direccion;
        private String estado;
        private LocalDateTime fechaRegistro;
        private List<PlanCorporativoDTO> planesCorporativos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanCorporativoDTO {
        private Long idPlanCorporativo;
        private String nombrePlanCorporativo;
        private Integer numeroPlacasContratadas;
        private String tipoPlan;
        private Double descuentoCorporativoAdicional;
        private Double precioPlanCorporativo;
        private String estado;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
        private String creadoPor;
        private LocalDateTime fechaCreacion;
        private List<SuscripcionVehiculoDTO> suscripcionesVehiculos;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuscripcionVehiculoDTO {
        private Long idSuscripcionFlotilla;
        private String placaVehiculo;
        private LocalDateTime fechaAsignacion;
        private String estado;
    }
}
