package org.parkcontrol.apiparkcontrol.controllers.liquidaciones;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.parkcontrol.apiparkcontrol.services.comercio_afliado.*;
import org.parkcontrol.apiparkcontrol.services.liquidaciones.*;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import org.parkcontrol.apiparkcontrol.dto.liquidaciones.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas/liquidaciones")
class GestionLiquidacionController {

    private final GestionLiquidacionService gestionLiquidacionService;

    @Autowired
    public GestionLiquidacionController(GestionLiquidacionService gestionLiquidacionService) {
        this.gestionLiquidacionService = gestionLiquidacionService;
    }

    @GetMapping("/detalles-liquidacion/{idUsuarioEmpresa}")
    public ResponseEntity<?> obtenerDetallesLiquidaciones(@PathVariable Long idUsuarioEmpresa) {
        try {
            DetallesLiquidacionesDTO detallesLiquidaciones = gestionLiquidacionService.obtenerDetallesLiquidacionesPorEmpresa(idUsuarioEmpresa);
            return ResponseEntity.ok(detallesLiquidaciones);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener los detalles de liquidaciones: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    //detalles de pagos de suscripciones
    @GetMapping("/detalles-pagos-suscripciones/{idUsuarioEmpresa}")
    public ResponseEntity<?> obtenerDetallesPagosSuscripciones(@PathVariable Long idUsuarioEmpresa) {
        try {
            List<DetallePagosSuscripcionDTO> detallesPagos = gestionLiquidacionService.obtenerDetallesPagosSuscripcion(idUsuarioEmpresa);
            return ResponseEntity.ok(detallesPagos);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener los detalles de pagos de suscripciones: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    //detales de transacciones por ticket
    @GetMapping("/detalles-transacciones-tickets/{idUsuarioEmpresa}")
    public ResponseEntity<?> obtenerDetallesTransaccionesTickets(@PathVariable Long idUsuarioEmpresa) {
        try {
            List<DetalleTransaccionTicketDTO> detallesTransacciones = gestionLiquidacionService.obtenerDetalleTransaccionesTicket(idUsuarioEmpresa);
            return ResponseEntity.ok(detallesTransacciones);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener los detalles de transacciones por ticket: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    //Actualizar periodos de corte de caja
    @PutMapping("/actualizar-periodo-corte-caja/{idUsuarioEmpresa}")
    public ResponseEntity<?> actualizarPeriodoCorteCaja(@PathVariable Long idUsuarioEmpresa) {
        try {
            String mensaje = gestionLiquidacionService.actualizarPeriodosCortesDeCaja(idUsuarioEmpresa);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", mensaje);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al actualizar el periodo de corte de caja: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
