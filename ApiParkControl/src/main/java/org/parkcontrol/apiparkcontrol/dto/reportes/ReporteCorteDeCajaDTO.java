package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetalleTransaccionTicketDTO;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.DetallesLiquidacionesDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteCorteDeCajaDTO {
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

    //Detalle del corte de caja
    private List<DetallesLiquidacionesDTO.CortesDeCajaDTO> cortesDeCajas;
    //Detalles de ingresos por tarifas y excedentes
    private List<DetalleTransaccionTicketDTO> detallesDeIngresosPorTarifasYExcedentes;

}
