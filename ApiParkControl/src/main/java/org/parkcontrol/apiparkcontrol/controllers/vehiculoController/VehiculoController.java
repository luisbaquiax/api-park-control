package org.parkcontrol.apiparkcontrol.controllers.vehiculoController;

import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.RegisterVehicleDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculosPropietarioDTO;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestion-vehiculos")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @PostMapping("/create/{dpi}")
    public MessageSuccess create(@PathVariable String dpi, @RequestBody VehiculoRequestDTO vehiculo){
        return vehiculoService.create(dpi, vehiculo);
    }

    @PutMapping("/update/{id}")
    public MessageSuccess update(@PathVariable Long id, @RequestBody VehiculoRequestDTO vehiculo){
        return vehiculoService.update(id, vehiculo);
    }

    @PutMapping("/change-status/{status}/{idVehiculo}")
    public MessageSuccess changeStatus(@PathVariable String status, @PathVariable Long idVehiculo){
        return vehiculoService.changeStatus(status, idVehiculo);
    }

    @GetMapping("/get-by-client")
    public List<VehiculosPropietarioDTO> getByIdPersona(){
        return vehiculoService.getAllByPersona();
    }

    @GetMapping("/get-all")
    public List<VehicleResponsDTO> getAll(){
        return vehiculoService.getAll();
    }
}
