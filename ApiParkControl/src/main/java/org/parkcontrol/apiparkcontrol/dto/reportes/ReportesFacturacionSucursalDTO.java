package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetalleTransaccionTicketDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportesFacturacionSucursalDTO {
    //Detalle  de la sucursal
    private Long idSucursal;
    private String nombreSucursal;
    private String direccionCompleta;
    private String ciudad;
    private String departamento;
    private String horaApertura;
    private String horaCierre;
    private Integer capacidad2Ruedas;
    private Integer capacidad4Ruedas;

    //Detalle de facturacion
    private List<DetalleTransaccionTicketDTO> detallesFacturacion;
}
