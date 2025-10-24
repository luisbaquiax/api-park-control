package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePlanesSuscripcionDTO {
    private Long idCliente;
    private String nombreCliente;
    private List<SuscripcionClienteDTO> suscripcionCliente;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuscripcionClienteDTO {
        private Long idSuscripcion;
        private String periodoContratado;
        private Double descuentoAplicado;
        private Double precioPlan;
        private Integer horasMensualesIncluidas;
        private Double horasConsumidas;
        private String fechaInicio;
        private String fechaFin;
        private String fechaCompra;
        private String estadoSuscripcion;
        private Double tarifaBaseReferencia;
        private VehiculoClienteDTO vehiculoClienteDTO;
        private DetalleTipoPlanDTO tipoPlanSuscripcionDTO;
        private List<ObtenerSucursalesEmpresaDTO.SucursalDTO> sucursalesDisponibles;

    }

}
