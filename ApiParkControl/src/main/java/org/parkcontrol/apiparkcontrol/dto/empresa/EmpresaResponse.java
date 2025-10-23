package org.parkcontrol.apiparkcontrol.dto.empresa;

import org.parkcontrol.apiparkcontrol.models.Empresa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponse {
    private Long idEmpresa;
    private Long idUsuarioEmpresa;
    private String nombreComercial;
    private String razonSocial;
    private String nit;
    private String direccionFiscal;
    private String telefonoPrincipal;
    private String correoPrincipal;
    private Empresa.EstadoEmpresa estado;
    private LocalDateTime fechaRegistro;
}