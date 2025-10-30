package org.parkcontrol.apiparkcontrol.dto.gestion_backoffice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolverSolicitudTemporalDTO {
    private Long idSolicitudTemporal;
    private String estado; //APROBADO o RECHAZADO
    private Long aprobadoPor;
    private String observaciones;
    private String fechaInicio;
    private String fechaFin;
    private Integer usosMaximos;
    private String sucursalesAsignadas = null; //IDs de sucursales separadas por comas
}
