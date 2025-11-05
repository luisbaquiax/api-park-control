package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.ClientePlanesSuscripcionDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.VehiculoClienteDTO;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleSolicitudesCambioPlacaDTO {
    private Long idSolicitudCambio;
    private String placaNueva;
    private String motivo;
    private String descripcionMotivo;
    private String fechaSolicitud;
    private String estado;
    private String fechaRevision;
    private String observacionesRevision;
    private String fechaEfectiva;

    //Detalles de la suscripcion
    ClientePlanesSuscripcionDTO.SuscripcionClienteDTO SuscripcionCliente;
    //Detalles del vehiculo actual
    VehiculoClienteDTO vehiculoActual;
    //Detalles del vehiculo nuevo
    private VehiculoClienteDTO vehiculoNuevo;

    //Detalles de la evidencia
    private DetalleEvidenciaCambioPlacaDTO evidenciaCambioPlaca;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalleEvidenciaCambioPlacaDTO {
        private Long idEvidencia;
        private String tipoDocumento;
        private String nombreArchivo;
        private String urlDocumento;
        private String descripcion;
        private String fechaCarga;
    }
}
