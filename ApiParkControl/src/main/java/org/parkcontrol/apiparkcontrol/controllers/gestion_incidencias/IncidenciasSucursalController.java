package org.parkcontrol.apiparkcontrol.controllers.gestion_incidencias;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.parkcontrol.apiparkcontrol.services.gestion_incidencias.*;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/sucursal/incidencias")
class IncidenciasSucursalController {

    private  final IncidenciaTicketSucursalService incidenciaTicketSucursalService;

    @Autowired
    public IncidenciasSucursalController(IncidenciaTicketSucursalService incidenciaTicketSucursalService) {
        this.incidenciaTicketSucursalService = incidenciaTicketSucursalService;
    }

    //Obtener todas las incidencias de una sucursal
    @GetMapping("/ver-incidencias/{idUsuario}")
    public  ResponseEntity<?> getTodasLasIncidenciasSucursal(@PathVariable Long idUsuario) {
        try {
            List<IncidenciasSucursalDTO> incidencias = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(idUsuario);
            return ResponseEntity.ok(incidencias);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las incidencias de la sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Crear una nueva incidencia en un ticket de sucursal
    @PostMapping(value = "/nueva-incidencia", consumes = {"multipart/form-data"})
    public ResponseEntity<?> crearNuevaIncidenciaSucursal(
            @RequestParam("data") String dataJson,  // Cambiado de @RequestPart a @RequestParam
            @RequestPart(value = "file", required = false) MultipartFile archivoEvidencia
    ) {
        try {
            // Convertir el String JSON a objeto
            ObjectMapper objectMapper = new ObjectMapper();
            NuevaIncidenciaDTO nuevaIncidencia = objectMapper.readValue(dataJson, NuevaIncidenciaDTO.class);

            String mensaje = incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidencia, archivoEvidencia);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear la nueva incidencia: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
