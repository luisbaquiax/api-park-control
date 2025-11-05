package org.parkcontrol.apiparkcontrol.controllers.gestion_backoffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.services.gestion_backoffice.*;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import  org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cliente/solicitud-temporal")
class SolicitudTemporalClienteController {
    private final SolicitudTemporalClienteService gestionSolicitudTemporalClienteService;

    @Autowired
    public SolicitudTemporalClienteController(SolicitudTemporalClienteService gestionSolicitudTemporalClienteService) {
        this.gestionSolicitudTemporalClienteService = gestionSolicitudTemporalClienteService;
    }

    //Nueva solicitud temporal por parte del cliente
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarPermisoTemporal(@RequestBody SolicitarPermisoTemporalDTO solicitarPermisoTemporalDTO) {
        try {
            String mensaje = gestionSolicitudTemporalClienteService.solicitarPermisoTemporal(solicitarPermisoTemporalDTO);
            Map<String, String> map = new HashMap<>();
            map.put("message", mensaje);
            map.put("status", "success");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al solicitar el permiso temporal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Obtener el detalle de las solicitudes temporales de un cliente
    @GetMapping("/solicitudes/{idCliente}")
    public ResponseEntity<?> obtenerSolicitudesTemporalesCliente(@PathVariable Long idCliente) {
        try {
            List<DetalleSolicitudesTemporalDTO> solicitudes = gestionSolicitudTemporalClienteService.obtenerDetallesPermisosTemporales(idCliente);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las solicitudes temporales: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }





}
