package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackOfficeDetalleSolicitudes {
    //Detalle del cliente
    private Long idUsuario;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String cui;
    private  String direccion;

    //Detalle de las solicitudes de cambio de placa
    private List<DetalleSolicitudesCambioPlacaDTO> detalleSolicitudesCambioPlaca;

}
