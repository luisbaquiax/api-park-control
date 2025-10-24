package org.parkcontrol.apiparkcontrol.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.Empresa;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEmpresa {
    private Long idUsuario;
    private String nombreComercial;
    private String razonSocial;
    private String nit;
    private String direccionFiscal;
    private String telefonoPrincipal;
    private String correoPrincipal;
    private Empresa.EstadoEmpresa estado;
}
