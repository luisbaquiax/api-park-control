package org.parkcontrol.apiparkcontrol.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.DetalleSucursalesIncidenciasDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportesDeIncidenciasDTO {
    private List<DetalleSucursalesIncidenciasDTO> detalleSucursalesIncidenciasDTO;
}
