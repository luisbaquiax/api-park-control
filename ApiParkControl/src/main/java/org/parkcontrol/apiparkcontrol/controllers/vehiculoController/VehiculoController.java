package org.parkcontrol.apiparkcontrol.controllers.vehiculoController;

import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.RegisterVehicleDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestion-vehiculos")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @PostMapping("/create")
    public MessageSuccess create(@RequestBody RegisterVehicleDTO register){
        return vehiculoService.create(register.getPersona(), register.getVehiculo());
    }
    @PutMapping("/change-status/{status}/{idVehiculo}")
    public MessageSuccess changeStatus(@PathVariable String status, @PathVariable Long idVehiculo){
        return vehiculoService.changeStatus(status, idVehiculo);
    }

    @GetMapping("/get-by-idPersona/{idPersona}")
    public List<VehicleResponsDTO> getByIdPersona(@PathVariable Long idPersona){
        return vehiculoService.getAllByPersona(idPersona);
    }

    @GetMapping("/get-all")
    public List<VehicleResponsDTO> getAll(){
        return vehiculoService.getAll();
    }
}
