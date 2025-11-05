package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.PersonaRequest;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculosPropietarioDTO;
import org.parkcontrol.apiparkcontrol.mapper.UsuarioPersonaRolMap;
import org.parkcontrol.apiparkcontrol.mapper.VehicleMap;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.repositories.VehiculoRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository repository;
    @Autowired
    private PersonaService personaService;
    @Autowired
    private VehicleMap vehicleMap;
    @Autowired
    private UsuarioPersonaRolMap usuarioPersonaRolMap;
    @Autowired
    private UsuarioService usuarioService;

    @Transactional
    public MessageSuccess create(String dpi, VehiculoRequestDTO vehiculoRequest) {
        Vehiculo vehiculoAux = repository.findByPlaca(vehiculoRequest.getPlaca());
        if (vehiculoAux != null) {
            throw new ErrorApi(401, String.format("El vehiculo ya existe con placa: %s", vehiculoRequest.getPlaca()));
        }
        Persona propietario = personaService.findByDpi(dpi);
        if (propietario == null) {
            throw new ErrorApi(401, String.format("El propietario con dpi: %s aún no se encuentra registrado.", dpi));
        }
        vehiculoAux = new Vehiculo();
        vehiculoAux.setPropietario(propietario);
        vehiculoAux.setAnio(vehiculoRequest.getAnio());
        vehiculoAux.setTipoVehiculo(vehiculoRequest.getTipoVehiculo());
        vehiculoAux.setColor(vehiculoRequest.getColor());
        vehiculoAux.setEstado(vehiculoRequest.getEstadoVehiculo());
        vehiculoAux.setMarca(vehiculoRequest.getMarca());
        vehiculoAux.setPlaca(vehiculoRequest.getPlaca());
        vehiculoAux.setModelo(vehiculoRequest.getModelo());

        repository.save(vehiculoAux);
        return new MessageSuccess(201,
                String.format(
                        "Se ha registrado correctamente el vehiculo con placa %s a nombre de la persona %s %s",
                        vehiculoAux.getPlaca(),
                        propietario.getNombre(),
                        propietario.getApellido()));
    }

    @Transactional
    public MessageSuccess update(Long id, VehiculoRequestDTO vehiculo){
        Vehiculo vehiculoAux = repository.findById(id).orElseThrow(() -> new ErrorApi(401, "El vehiculo no existe"));
        vehiculoAux.setAnio(vehiculo.getAnio());
        vehiculoAux.setTipoVehiculo(vehiculo.getTipoVehiculo());
        vehiculoAux.setColor(vehiculo.getColor());
        vehiculoAux.setEstado(vehiculo.getEstadoVehiculo());
        vehiculoAux.setMarca(vehiculo.getMarca());
        //vehiculoAux.setPlaca(vehiculo.getPlaca());
        vehiculoAux.setModelo(vehiculo.getModelo());
        repository.save(vehiculoAux);
        return new MessageSuccess(201, "El vehiculo se ha actualizado correctamente");
    }

    @Transactional
    public MessageSuccess changeStatus(String status, Long idVehiculo){
        Vehiculo vehiculo = repository.findById(idVehiculo).orElseThrow(() -> new ErrorApi(401, "El vehiculo no existe"));
        vehiculo.setEstado(Vehiculo.EstadoVehiculo.valueOf(status));
        repository.save(vehiculo);
        return new MessageSuccess(201, "El vehiculo se ha actualizado correctamente");
    }

    public List<VehiculosPropietarioDTO> getAllByPersona(){
        List<Usuario> users = usuarioService.getUsersByRol("CLIENTE");
        List<VehiculosPropietarioDTO> reporte = new ArrayList<>();
        for (Usuario user : users) {
            List<Vehiculo> vehiculos = repository.findByPropietarioIdPersona(user.getPersona().getIdPersona());
            reporte.add(new VehiculosPropietarioDTO(usuarioPersonaRolMap.map(user), vehiculos.stream().map(vehicleMap::map).toList()));
        }
        return reporte;
    }

    public List<VehicleResponsDTO> getAll(){
        return repository.findAll().stream().map(vehicleMap::map).toList();
    }
}
