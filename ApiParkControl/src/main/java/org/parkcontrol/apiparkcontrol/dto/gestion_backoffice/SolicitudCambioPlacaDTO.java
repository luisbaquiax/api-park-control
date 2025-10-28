package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudCambioPlacaDTO {
    private Long idCliente;
    private Long idSuscripcion;
    private Long idVehiculoActual;
    private String placaNueva;
    private String motivo;
    private String descripcionMotivo;

    //Campos para la evidencia
    private String tipoDocumento;
    private String descripcionEvidencia;
}

/*
Ejemplo json para crear una solicitud de cambio de placa:
{
    "idCliente": 1,
    "idSuscripcion": 2,
    "idVehiculoActual": 3,
    "placaNueva": "ABC-123",
    "motivo": "ROBO",
    "descripcionMotivo": "La placa fue robada el 28/10/2025",
    "tipoDocumento": "Denuncia Policial",
    "descripcionEvidencia": "Denuncia policial por robo de placa"
}
 */