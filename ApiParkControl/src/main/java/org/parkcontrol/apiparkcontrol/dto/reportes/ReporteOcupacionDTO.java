package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteOcupacionDTO {
    //Detalle  de la sucursal
    private Long idSucursal;
    private String nombreSucursal;
    private String direccionCompleta;
    private String ciudad;
    private String departamento;
    private String horaApertura;
    private String horaCierre;
    private Integer capacidad2Ruedas;
    private Integer capacidad4Ruedas;

    //Detalle de ocupacion
    private DetalleOcupacionDTO detallesOcupacion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleOcupacionDTO {
        private String fechaHora;
        private Integer ocupacion2R;
        private Integer capacidad2R;
        private Integer ocupacion4R;
        private Integer capacidad4R;
        private String porcentajeOcupacion2R;
        private String porcentajeOcupacion4R;
    }


}
