package org.parkcontrol.apiparkcontrol.dto.autenticacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FADTO {
    private String token;
    private String codigoVerificacion;
}
