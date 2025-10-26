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
@RequestMapping("/api/empresa/incidencias")
class ResolucionIncidenciasController {

    private  final ResolucionIncidenciasService resolucionIncidenciasService;

    @Autowired
    public ResolucionIncidenciasController(ResolucionIncidenciasService resolucionIncidenciasService) {
        this.resolucionIncidenciasService = resolucionIncidenciasService;
    }

    //Obtenemos todas las incidencias de las sucursales de una empresa
    @GetMapping("/ver-incidencias/{idUsuarioEmpresa}")
    public  ResponseEntity<?> getDetalleIncidenciasPorEmpresa(@PathVariable Long idUsuarioEmpresa) {
        try {
            List<DetalleSucursalesIncidenciasDTO> detalleIncidencias = resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(idUsuarioEmpresa);
            return ResponseEntity.ok(detalleIncidencias);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las incidencias de las sucursales: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Resolver una incidencia de ticket de sucursal
    @PostMapping("/resolver-incidencia")
    public ResponseEntity<?> resolverIncidenciaSucursal(@RequestBody ResolucionIncidenciaDTO resolucionIncidenciaDTO) {
        try {
            String mensaje = resolucionIncidenciasService.resolverIncidencia(resolucionIncidenciaDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al resolver la incidencia: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
