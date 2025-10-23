package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehiculoClienteDTO {
    private Long idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private String color;
    private String tipoVehiculo;
}
