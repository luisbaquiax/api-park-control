package org.parkcontrol.apiparkcontrol.controllers.planes_suscripcion;

import org.parkcontrol.apiparkcontrol.services.planes_suscripcion.*;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas/planes-suscripcion")
class PlanesSuscripcionController {
    private final PlanesSuscripcionService planesSuscripcionService;

    @Autowired
    public PlanesSuscripcionController(PlanesSuscripcionService planesSuscripcionService) {
        this.planesSuscripcionService = planesSuscripcionService;
    }

    //Obtenemos los planes de suscripción por empresa
    @GetMapping("/planes/{idUsuario}")
    public ResponseEntity<?> getPlanesPorEmpresa(@PathVariable Long idUsuario) {
        try {
            return ResponseEntity.ok(planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(idUsuario));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los planes de suscripción: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    // Creamos un nuevo plan de suscripción
    @PostMapping("/planes")
    public ResponseEntity<?> crearNuevoPlan(@RequestBody NuevoPlanDTO nuevoPlanDTO) {
        try {
            String mensaje = planesSuscripcionService.crearNuevoPlanSuscripcion(nuevoPlanDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear el nuevo plan de suscripción: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Editamos un plan de suscripción existente
    @PutMapping("/planes")
    public ResponseEntity<?> editarPlanSuscripcion(@RequestBody NuevoPlanDTO editarPlanDTO) {
        try {
            String mensaje = planesSuscripcionService.editarPlanSuscripcion(editarPlanDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al editar el plan de suscripción: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }


}
