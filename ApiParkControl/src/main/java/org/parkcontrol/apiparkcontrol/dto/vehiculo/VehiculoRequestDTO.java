package org.parkcontrol.apiparkcontrol.dto.vehiculo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehiculoRequestDTO {
    private String placa;
    private Vehiculo.TipoVehiculo tipoVehiculo;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private Vehiculo.EstadoVehiculo estadoVehiculo;
}
