package org.parkcontrol.apiparkcontrol.dto.vehiculo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.PersonaRequest;

@Data
@NoArgsConstructor
public class RegisterVehicle {
    private VehiculoRequest vehiculo;
    private PersonaRequest persona;
}
