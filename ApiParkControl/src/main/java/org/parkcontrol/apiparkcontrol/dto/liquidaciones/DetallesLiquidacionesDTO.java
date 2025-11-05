package org.parkcontrol.apiparkcontrol.dto.liquidaciones;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallesLiquidacionesDTO {
    private List<CortesDeCajaDTO> cortesDeCaja;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CortesDeCajaDTO {
        private Long idCorteCaja;
        private String sucursalNombre;
        private String periodo;
        private String fechaInicio;
        private String fechaFin;
        //private String totalIngresosTarifas;
        //private String totalIngresosExcedentes;
        private String totalHorasComercio;
        private String totalLiquidacionComercios;
        private String totalNeto;
        private String generadoPorNombreUsuario;
        private String fechaGeneracion;
        private String estado;

        private List<DetalleComercioLiquidacionDTO> detallesComercios;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DetalleComercioLiquidacionDTO {
            private Long idLiquidacion;
            private String comercioNombre;
            private String totalHorasOtorgadas;
            private String tarifaPorHora;
            private String montoTotal;
            private String estado;
            private String fechaFacturacion;
            private String fechaPago;
            private String observaciones;
        }

    }
}
