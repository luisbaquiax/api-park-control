package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.ClientePlanesSuscripcionDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSolicitudesTemporalDTO {
    private Long idPermisoTemporal;
    private String placaTemporal;
    private String tipoVehiculoPermitido;
    private String motivo;
    private String fechaInicio;
    private String fechaFin;
    private Integer usosMaximos;
    private Integer usosRealizados;
    private String estado;
    private String aprobadoPor;
    private String fechaAprobacion;
    private String observaciones;

    //Detalles de la suscripcion
    ClientePlanesSuscripcionDTO.SuscripcionClienteDTO SuscripcionCliente;

    //Sucursales validas
    private List<ObtenerSucursalesEmpresaDTO.SucursalDTO> sucursalesDisponiblesPermiso;


}
