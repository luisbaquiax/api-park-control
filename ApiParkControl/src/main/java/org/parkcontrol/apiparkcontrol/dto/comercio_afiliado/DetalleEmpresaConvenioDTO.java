package org.parkcontrol.apiparkcontrol.dto.comercio_afiliado;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleEmpresaConvenioDTO {
    private ComercioAfiliadoDTO comercioAfiliado;
    private ConvenioComercioSucursalDTO convenioComercioSucursal;
}
