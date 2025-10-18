package org.parkcontrol.apiparkcontrol.controllers.empresa_sucursal;

import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.CreateSucursalDTO;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.services.empresa_sucursal.EmpresaSucursalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.services.autenticacion.authenticateService;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/empresa-sucursal")
class EmpresaSucursalController {

    private final EmpresaSucursalService empresaSucursalService;
    @Autowired
    public EmpresaSucursalController(EmpresaSucursalService empresaSucursalService) {
        this.empresaSucursalService = empresaSucursalService;
    }

    //Obtenemos usuarios de sucursal por empresa
    @GetMapping("/sucursales/{idEmpresa}")
    public ResponseEntity<?> getUsuariosSucursalByEmpresa(@PathVariable Long idEmpresa) {
        try {
            return ResponseEntity.ok(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(idEmpresa));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al obtener los usuarios de sucursal: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }

    //Creamos una nueva sucursal
    @PostMapping("/sucursales")
    public ResponseEntity<?> createSucursal(@RequestBody CreateSucursalDTO sucursalDTO) {
        try {
            String mensaje = empresaSucursalService.crearNuevaSucursal(sucursalDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", mensaje);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error al crear la sucursal: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }


}
