package org.parkcontrol.apiparkcontrol.dto.comercio_afiliado;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioAfiliadoDTO {
    private Long idComercio;
    private String nombreComercial;
    private String razonSocial;
    private String nit;
    private String tipoComercio;
    private String telefono;
    private String correoContacto;
    private String estado;
    private String fechaRegistro;
}
