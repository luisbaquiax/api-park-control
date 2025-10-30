package org.parkcontrol.apiparkcontrol.dto.vehiculo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa.UsuarioPersonaRolResponse;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehiculosPropietarioDTO {
    private UsuarioPersonaRolResponse usuario;
    private List<VehicleResponsDTO> vehiculos;
}
