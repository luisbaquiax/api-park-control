package org.parkcontrol.apiparkcontrol.controllers.suscripcion_cliente;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/cliente/suscripciones")
class SuscripcionClienteController {

    private final SuscripcionClienteService suscripcionClienteService;

    @Autowired
    public SuscripcionClienteController(SuscripcionClienteService suscripcionClienteService) {
        this.suscripcionClienteService = suscripcionClienteService;
    }

    //Obtenemos todos los planes de suscripción disponibles para el cliente
    @GetMapping("/planes")
    public ResponseEntity<?> getPlanesDisponiblesParaCliente() {
        try {
            return ResponseEntity.ok(suscripcionClienteService.obtenerPlanesSuscripcion());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los planes de suscripción disponibles: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtenemos los vehículos asociados al cliente
    @GetMapping("/vehiculos/{idCliente}")
    public ResponseEntity<?> getVehiculosPorCliente(@PathVariable Long idCliente) {
        try {
            return ResponseEntity.ok(suscripcionClienteService.obtenerVehiculosCliente(idCliente));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los vehículos del cliente: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtenemos los planes de suscripción contratados por el cliente
    @GetMapping("/planes/{idCliente}")
    public ResponseEntity<?> getPlanesContratadosPorCliente(@PathVariable Long idCliente) {
        try {
            return ResponseEntity.ok(suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(idCliente));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los planes de suscripción contratados: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Creamos una nueva suscripción para el cliente
    @PostMapping("/nueva-suscripcion")
    public ResponseEntity<?> crearNuevaSuscripcion(@RequestBody NuevaSuscripcionDTO nuevaSuscripcionDTO) {
        try {
            String mensaje = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear la nueva suscripción: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Renovamos una suscripción existente para el cliente
    @PostMapping("/renovar-suscripcion")
    public ResponseEntity<?> renovarSuscripcion(@RequestBody RenovacionSuscripcionDTO renovacionSuscripcionDTO) {
        try {
            String mensaje = suscripcionClienteService.renovarSuscripcionCliente(renovacionSuscripcionDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al renovar la suscripción: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
