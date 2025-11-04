package org.parkcontrol.apiparkcontrol.controllers.reportes;

import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.DetalleEmpresaFlotillaDTO;
import org.parkcontrol.apiparkcontrol.services.reportes.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.dto.reportes.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa/reportes")
class ReportesController {

    private final ReportesService reportesService;
    @Autowired
    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }
    //generarReporteOcupacionPorSucursal
    @GetMapping("/ocupacion/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeOcupacionPorSucursal( @PathVariable Long idUsuarioEmpresa ) {
        try {
            List<ReporteOcupacionDTO> reporte = reportesService.generarReporteOcupacionPorSucursal(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de ocupación por sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //generarReporteFacturacionPorSucursal
    @GetMapping("/facturacion/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeFacturacionPorSucursal( @PathVariable Long idUsuarioEmpresa ) {
        try {
            List<ReportesFacturacionSucursalDTO> reporte = reportesService.generarReporteFacturacionPorSucursal(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de facturación por sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //generarReporteSuscripciones
    @GetMapping("/suscripciones/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeSuscripciones( @PathVariable Long idUsuarioEmpresa ) {
        try {
            List<ReporteSuscripcionesDTO> reporte = reportesService.generarReporteSuscripciones(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de suscripciones: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //generarReporteComercioAfiliado
    @GetMapping("/comercios-afiliados/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeComerciosAfiliados( @PathVariable Long idUsuarioEmpresa ) {
        try {
            List<ReporteComercioAfiliadoDTO> reporte = reportesService.generarReporteComercioAfiliado(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de comercios afiliados: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //generarReporteCortesDeCaja
    @GetMapping("/cortes-caja/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeCortesDeCaja( @PathVariable Long idUsuarioEmpresa ) {
        try {
            List<ReporteCorteDeCajaDTO> reporte = reportesService.generarReporteCortesDeCaja(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de cortes de caja: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtenemos reporte de incidencias
    @GetMapping("/incidencias/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeIncidencias( @PathVariable Long idUsuarioEmpresa ) {
        try {
            ReportesDeIncidenciasDTO reporte = reportesService.generarReporteIncidencias(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de incidencias: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //generarReporteFlotasEmpresariales
    @GetMapping("/flotas-empresariales/{idUsuarioEmpresa}")
    public ResponseEntity<?> getReporteDeFlotasEmpresariales( @PathVariable Long idUsuarioEmpresa ) {
        try {
            DetalleEmpresaFlotillaDTO reporte = reportesService.generarReporteFlotasEmpresariales(idUsuarioEmpresa);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el reporte de flotas empresariales: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

}
