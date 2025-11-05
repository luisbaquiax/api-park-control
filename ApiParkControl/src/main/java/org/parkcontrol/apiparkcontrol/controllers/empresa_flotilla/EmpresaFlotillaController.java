package org.parkcontrol.apiparkcontrol.controllers.empresa_flotilla;

import org.parkcontrol.apiparkcontrol.services.empresa_flotilla.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa/flotilla")
class EmpresaFlotillaController {

    private final GestionEmpresaFlotillaService gestionEmpresaFlotillaService;
    @Autowired
    public EmpresaFlotillaController(GestionEmpresaFlotillaService gestionEmpresaFlotillaService) {
        this.gestionEmpresaFlotillaService = gestionEmpresaFlotillaService;
    }

    //Obtenemos detalle de empresas flotilla
    @GetMapping("/detalle/{idUsuarioEmpresa}")
    public ResponseEntity<?> getDetalleEmpresasFlotilla( @PathVariable Long idUsuarioEmpresa ) {
        try {
            DetalleEmpresaFlotillaDTO detalle = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(idUsuarioEmpresa);
            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el detalle de las empresas flotilla: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtener vehiculos
    @GetMapping("/vehiculos")
    public ResponseEntity<?> getVehiculos() {
        try {
            return ResponseEntity.ok(gestionEmpresaFlotillaService.obtenerVehiculos());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los vehículos: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Nueva empresa flotilla
    @PostMapping("/nueva-empresa-flotilla")
    public ResponseEntity<?> createEmpresaFlotilla(@RequestBody NuevaEmpresaFlotillaDTO empresaFlotillaDTO) {
        try {
            String mensaje = gestionEmpresaFlotillaService.nuevaEmpresaFlotilla(empresaFlotillaDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear la empresa flotilla: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Nuevo Plan Corporativo
    @PostMapping("/nuevo-plan-corporativo")
    public ResponseEntity<?> createPlanCorporativo(@RequestBody NuevoPlanCorporativoDTO planCorporativoDTO) {
        try {
            String mensaje = gestionEmpresaFlotillaService.nuevoPlanCorporativo(planCorporativoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear el plan corporativo: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Activar plan corporativo
    @PostMapping("/activar-plan-corporativo/{idPlanCorporativo}")
    public ResponseEntity<?> activarPlanCorporativo(@PathVariable Long idPlanCorporativo) {
        try {
            String mensaje = gestionEmpresaFlotillaService.activarPlanCorporativo(idPlanCorporativo);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al activar el plan corporativo: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Desactivar plan corporativo
    @PostMapping("/desactivar-plan-corporativo/{idPlanCorporativo}")
    public ResponseEntity<?> desactivarPlanCorporativo(@PathVariable Long idPlanCorporativo) {
        try {
            String mensaje = gestionEmpresaFlotillaService.desactivarPlanCorporativo(idPlanCorporativo);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al desactivar el plan corporativo: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Suscribir vehículo a plan corporativo
    @PostMapping("/suscribir-vehiculo")
    public ResponseEntity<?> suscribirVehiculo(@RequestBody SuscripcionFlotillaDTO suscribirVehiculoDTO) {
        try {
            String mensaje = gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscribirVehiculoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al suscribir el vehículo: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Cancelar suscripción de vehículo a plan corporativo
    @PostMapping("/cancelar-suscripcion-vehiculo/{idSuscripcion}")
    public ResponseEntity<?> cancelarSuscripcionVehiculo(@PathVariable Long idSuscripcion) {
        try {
            String mensaje = gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(idSuscripcion);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al cancelar la suscripción del vehículo: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }
}
