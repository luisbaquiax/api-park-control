package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetallesLiquidacionesDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteComercioAfiliadoDTO {
    //Primero la informacion del comercio afiliado
    private Long idComercioAfiliado;
    private String nombreComercial;
    private String razonSocial;
    private String nit;
    private String tipoComercio;
    private String correoContacto;
    private String estado;
    //Detalle del convenio
    private List<DetalleConvenioComercioDTO> detallesConvenioComercio;
    //Detalle de cortes de caja
    private List<DetallesLiquidacionesDTO.CortesDeCajaDTO.DetalleComercioLiquidacionDTO> detallesCorteCaja;
    private List<ClientesBeneficiadosDTO> clientesBeneficiados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleConvenioComercioDTO {
        private Long idConvenioComercio;
        private String nombreSucursal;
        private String horasGratisMaximo;
        private String periodoCorte;
        private String tarifaPorHora;
        private String fechaInicioConvenio;
        private String fechaFinConvenio;
        private String estado;
        private String fechaCreacion;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientesBeneficiadosDTO {
        private Long idCliente;
        private String nombreCliente;
        private String horasGratisOtorgadas;
        private String sucursalNombre;
        private String fechaBeneficio;
    }
}
