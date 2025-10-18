package org.parkcontrol.apiparkcontrol.controllers.gestion_sucursal;


import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.services.gestion_sucursal.*;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sucursal")
class GestionSucursalController {
    private final GestionSucursalService gestionSucursalService;
    private final GestionTarifaSucursalService gestionTarifaSucursalService;

    @Autowired
    public GestionSucursalController(GestionSucursalService gestionSucursalService, GestionTarifaSucursalService gestionTarifaSucursalService) {
        this.gestionSucursalService = gestionSucursalService;
        this.gestionTarifaSucursalService = gestionTarifaSucursalService;
    }

    //Obtenemos la sucursal por usuario de sucursal
    @GetMapping("/mi-sucursal/{idUsuarioSucursal}")
    public ResponseEntity<?> getMiSucursal(@PathVariable Long idUsuarioSucursal) {
        try {
            return ResponseEntity.ok(gestionSucursalService.obtenerSucursalPorUsuario(idUsuarioSucursal));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener la sucursal: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Editamos la sucursal
    @PutMapping("/mi-sucursal")
    public ResponseEntity<?> editarMiSucursal(@RequestBody EditarSucursalDTO editarSucursalDTO) {
        try {
            String mensaje = gestionSucursalService.editarSucursal(editarSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al editar la sucursal: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Creamos una nueva tarifa para la sucursal
    @PostMapping("/mi-sucursal/tarifas")
    public ResponseEntity<?> crearNuevaTarifaSucursal(@RequestBody NuevaTarifaSucursalDTO nuevaTarifaSucursalDTO) {
        try {
            String mensaje = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear la nueva tarifa de sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Editar tarifa sucursal
    @PutMapping("/mi-sucursal/tarifas")
    public ResponseEntity<?> editarTarifaSucursal(@RequestBody TarifaSucursalDTO tarifaSucursalDTO) {
        try {
            String mensaje = gestionTarifaSucursalService.editarTarifaSucursal(tarifaSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al editar la tarifa de sucursal: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtener tarifas de la sucursal por usuario de sucursal
    @GetMapping("/mi-sucursal/tarifas/{idUsuarioSucursal}")
    public ResponseEntity<?> getTarifasSucursalPorUsuario(@PathVariable Long idUsuarioSucursal) {
        try {
            return ResponseEntity.ok(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(idUsuarioSucursal));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las tarifas de sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }


}
