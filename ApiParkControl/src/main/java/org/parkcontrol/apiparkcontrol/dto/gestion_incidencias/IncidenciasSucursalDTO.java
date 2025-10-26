package org.parkcontrol.apiparkcontrol.dto.gestion_incidencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciasSucursalDTO {
    //Primero informacion del ticket asociado a la incidencia
    Long idTicket;
    String folioNumerico;
    String tipoCliente;
    String estadoTicket;
    String placaVehiculo;
    String modeloVehiculo;
    String colorVehiculo;
    String nombrePropietario;
    String telefonoPropietario;
    //Luego informacion de la incidencia
    IncidenciasTicketDTO incidencias;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidenciasTicketDTO {
        Long idIncidencia;
        String tipoIncidencia;
        String descripcion;
        String fechaIncidencia;
        boolean resuelto;
        String fechaResolucion;
        Long resueltoPor;
        String observacionesResolucion;
        List<EvidenciasIncidenciaDTO> evidencias;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenciasIncidenciaDTO {
        Long idEvidenciaIncidencia;
        String tipoEvidencia;
        String nombreArchivo;
        String urlEvidencia;
        String descripcion;
        String fechaCarga;
    }

}
