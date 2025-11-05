package org.parkcontrol.apiparkcontrol.dto.empresa_flotilla;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevaEmpresaFlotillaDTO {
    private String nombreEmpresa;
    private String razonSocial;
    private String nit;
    private String telefono;
    private String correoContacto;
    private String direccion;

}
/*
Ejemplo de JSON para NuevaEmpresaFlotillaDTO:
{
    "nombreEmpresa": "Transporte XYZ",
    "razonSocial": "Transporte XYZ S.A.",
    "nit": "123456789-0",
    "telefono": "32457896",
    "correoContacto": empresaTransporte@gmail.com "
    "direccion": "Calle 123 #45-67, Ciudad, País"
}
 */