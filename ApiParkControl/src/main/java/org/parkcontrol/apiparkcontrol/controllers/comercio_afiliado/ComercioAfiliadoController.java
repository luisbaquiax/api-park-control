package org.parkcontrol.apiparkcontrol.controllers.comercio_afiliado;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.parkcontrol.apiparkcontrol.services.comercio_afliado.*;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sucursal/comercio-afiliado")
class ComercioAfiliadoController {

    private final GestionComercioAfiliadoService gestionComercioAfiliadoService;

    @Autowired
    public ComercioAfiliadoController(GestionComercioAfiliadoService gestionComercioAfiliadoService) {
        this.gestionComercioAfiliadoService = gestionComercioAfiliadoService;
    }

    //Obtenemos todas las empresas
    @GetMapping("/comercio")
    public ResponseEntity<?> getTodasLasEmpresas() {
        try {
            return ResponseEntity.ok(gestionComercioAfiliadoService.getComercioAfiliado());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener las empresas afiliadas: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Obtenemos el detalle de una empresa afiliada por sucursal
    @GetMapping("/comercio/{idUsuarioSucursal}")
    public ResponseEntity<?> getDetalleEmpresaConvenioPorSucursal(@PathVariable Long idUsuarioSucursal) {
        try {
            List<DetalleEmpresaConvenioDTO> detalle = gestionComercioAfiliadoService.getDetalleEmpresaConvenio(idUsuarioSucursal);
            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener el detalle de la empresa afiliada: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Nuevo comercio afiliado
    @PostMapping("/comercio")
    public ResponseEntity<?> crearNuevoComercioAfiliado(@RequestBody ComercioAfiliadoDTO comercioAfiliadoDTO) {
        try {
            String nuevoComercio = gestionComercioAfiliadoService.crearComercioAfiliado(comercioAfiliadoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", nuevoComercio);
            response.put("status", "success");
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear el nuevo comercio afiliado: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Actualizar comercio afiliado
    @PutMapping("/comercio")
    public ResponseEntity<?> actualizarComercioAfiliado(@RequestBody ComercioAfiliadoDTO comercioAfiliadoDTO) {
        try {
            String mensaje = gestionComercioAfiliadoService.actualizarComercioAfiliado(comercioAfiliadoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al actualizar el comercio afiliado: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Eliminar comercio afiliado
    @DeleteMapping("/comercio/{idComercio}")
    public ResponseEntity<?> eliminarComercioAfiliado(@PathVariable Long idComercio) {
        try {
            String mensaje = gestionComercioAfiliadoService.eliminarComercioAfiliado(idComercio);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al eliminar el comercio afiliado: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Nuevo convenio comercio sucursal
    @PostMapping("/convenio-comercio-sucursal")
    public ResponseEntity<?> crearNuevoConvenioComercioSucursal(@RequestBody ConvenioComercioSucursalDTO convenioComercioSucursalDTO) {
        try {
            String nuevoConvenio = gestionComercioAfiliadoService.crearConvenioComercioSucursal(convenioComercioSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", nuevoConvenio);
            response.put("status", "success");
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear el nuevo convenio comercio sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Actualizar convenio comercio sucursal
    @PutMapping("/convenio-comercio-sucursal")
    public ResponseEntity<?> actualizarConvenioComercioSucursal(@RequestBody ConvenioComercioSucursalDTO convenioComercioSucursalDTO) {
        try {
            String mensaje = gestionComercioAfiliadoService.actualizarConvenioComercioSucursal(convenioComercioSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al actualizar el convenio comercio sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    //Cambiar estado del convenio comercio sucursal
    @PutMapping("/estado-convenio")
    public ResponseEntity<?> cambiarEstadoConvenioComercioSucursal(@RequestBody ConvenioComercioSucursalDTO convenioComercioSucursalDTO) {
        try {
            String mensaje = gestionComercioAfiliadoService.cambiarEstadoConvenio(convenioComercioSucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al cambiar el estado del convenio comercio sucursal: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }


}
