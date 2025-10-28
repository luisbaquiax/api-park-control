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
@RequestMapping("/api/backoffice/gestion")
class GestionBackofficeController {
    private final GestionBackOfficeService gestionBackOfficeService;

    @Autowired
    public GestionBackofficeController(GestionBackOfficeService gestionBackOfficeService) {
        this.gestionBackOfficeService = gestionBackOfficeService;
    }

    //Obtener el detalle de las solicitudes de cambio de placa de todos los clientes
    @GetMapping("/detalle-solicitudes/{idBackoffice}")
    public ResponseEntity<?> obtenerDetalleSolicitudesCambioPlacaCliente(@PathVariable Long idBackoffice) {
        try {
            List<BackOfficeDetalleSolicitudes> detalleSolicitudes = gestionBackOfficeService.obtenerTodasSolicitudesCambioPlaca(idBackoffice);
            return ResponseEntity.ok(detalleSolicitudes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el detalle de las solicitudes de cambio de placa: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Revisar y aprobar o rechazar una solicitud de cambio de placa
    @PostMapping("/revisar-solicitud")
    public ResponseEntity<?> revisarSolicitudCambioPlaca(
            @RequestBody ResolverSolicitudCambioDTO revisarSolicitudDTO
    ) {
        try {
            String mensaje = gestionBackOfficeService.revisarSolicitudCambioPlaca(revisarSolicitudDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al revisar la solicitud de cambio de placa: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
