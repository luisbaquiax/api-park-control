package org.parkcontrol.apiparkcontrol.dto.comercio_afiliado;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.UsuarioSucursalDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSucursalesConvenioDTO {
    //Detalle de la sucursal
    private Long idSucursal;
    private String nombre;
    private String direccionCompleta;
    private String ciudad;
    private String departamento;
    private String horaApertura;
    private String horaCierre;
    private Integer capacidad2Ruedas;
    private Integer capacidad4Ruedas;
    private String estado;
    private Long idUsuarioSucursal;
    private UsuarioSucursalDTO usuario;
    private List<ConvenioSucursalDTO> convenios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConvenioSucursalDTO {
        private Long idConvenio;
        private String horasGratisMaximo;
        private String periodoCorte;
        private String tarifaPorHora;
        private String fechaInicioConvenio;
        private String fechaFinConvenio;
        private String estado;
        private String fechaCreacion;
        ComercioAfiliadoDTO comercioAfiliado;
    }
}
