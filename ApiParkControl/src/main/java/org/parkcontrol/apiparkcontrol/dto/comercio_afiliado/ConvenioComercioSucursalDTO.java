package org.parkcontrol.apiparkcontrol.dto.comercio_afiliado;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioComercioSucursalDTO {
    private Long idConvenio;
    private Long idComercio;
    private Long idSucursal;
    private String horasGratisMaximo;
    private String periodoCorte;
    private String tarifaPorHora;
    private String fechaInicioConvenio;
    private String fechaFinConvenio;
    private String estado;
    private Long creadoPor;
    private String fechaCreacion;
}
