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
@RequestMapping("/api/cliente/cambio-placa")
class CambioPlacaClienteController {

    private final GestionCambioPlacaClienteService gestionCambioPlacaClienteService;

    @Autowired
    public CambioPlacaClienteController(GestionCambioPlacaClienteService gestionCambioPlacaClienteService) {
        this.gestionCambioPlacaClienteService = gestionCambioPlacaClienteService;
    }

    //El cliente solicita el cambio de placa de su vehículo asociado a una suscripción
    @PostMapping(value = "/solicitar", consumes = {"multipart/form-data"})
    public ResponseEntity<?> solicitarCambioPlaca(
            @RequestParam("data") String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile archivoEvidencia
    ) {
        try {
            // Convertir el String JSON a objeto
            ObjectMapper objectMapper = new ObjectMapper();
            SolicitudCambioPlacaDTO solicitarCambioPlacaDTO = objectMapper.readValue(dataJson, SolicitudCambioPlacaDTO.class);
            String mensaje = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitarCambioPlacaDTO, archivoEvidencia);
            Map<String,String> map = new HashMap<>();
            map.put("message", mensaje);
            map.put("status", "success");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al solicitar el cambio de placa: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    // El cliente consulta el estado de sus solicitudes de cambio de placa
    @GetMapping("/solicitudes/{idCliente}")
    public ResponseEntity<?> obtenerSolicitudesCambioPlacaCliente(@PathVariable Long idCliente) {
        try {
            List<DetalleSolicitudesCambioPlacaDTO> solicitudes = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(idCliente);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las solicitudes de cambio de placa: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
