package org.parkcontrol.apiparkcontrol.dto.gestion_incidencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleSucursalesIncidenciasDTO {
    private Long idSucursal;
    private String nombreSucursal;
    private String direccionSucursal;
    private String telefonoSucursal;
    List<IncidenciasSucursalDTO> incidenciasSucursalDTOList;

}
