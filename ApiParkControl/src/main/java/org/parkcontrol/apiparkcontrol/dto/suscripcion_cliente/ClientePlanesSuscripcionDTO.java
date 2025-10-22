package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePlanesSuscripcionDTO {
    private Long idCliente;
    private String nombreCliente;
    private List<SuscripcionClienteDTO> suscripcionCliente;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
        private PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaSuscripcionDTO;

    }

}
