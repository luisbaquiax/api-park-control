package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.PersonaRequest;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.mapper.VehicleMap;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.repositories.VehiculoRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository repository;
    @Autowired
    private PersonaService personaService;
    @Autowired
    private VehicleMap vehicleMap;

    @Transactional
    public MessageSuccess create(PersonaRequest persona, VehiculoRequestDTO vehiculoRequest) {
        Vehiculo vehiculoAux = repository.findByPlaca(vehiculoRequest.getPlaca());
        if (vehiculoAux != null) {
            throw new ErrorApi(401, String.format("El vehiculo ya existe con placa: %s", vehiculoRequest.getPlaca()));
        }
        Persona personaEncargado = personaService.findByDpi(persona.getDpi());
        if (personaEncargado == null) {
            personaEncargado = new Persona();
            personaEncargado.setIdPersona(null);
            personaEncargado.setFechaNacimiento(persona.getFechaNacimiento());
            personaEncargado.setCiudad(persona.getCiudad());
            personaEncargado.setTelefono(persona.getTelefono());
            personaEncargado.setNombre(persona.getNombre());
            personaEncargado.setApellido(persona.getApellido());
            personaEncargado.setCorreo(persona.getCorreo());
            personaEncargado.setDireccionCompleta(persona.getDireccionCompleta());
            personaEncargado.setCodigoPostal(persona.getCodigoPostal());
            personaEncargado.setPais(persona.getPais());
            personaEncargado.setDpi(persona.getDpi());

            personaEncargado = personaService.create(personaEncargado);
        }
        vehiculoAux = new Vehiculo();
        vehiculoAux.setPropietario(personaEncargado);
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
                        personaEncargado.getNombre(),
                        personaEncargado.getApellido()));
    }

    @Transactional
    public MessageSuccess changeStatus(String status, Long idVehiculo){
        Vehiculo vehiculo = repository.findById(idVehiculo).orElseThrow(() -> new ErrorApi(401, "El vehiculo no existe"));
        vehiculo.setEstado(Vehiculo.EstadoVehiculo.valueOf(status));
        repository.save(vehiculo);
        return new MessageSuccess(201, "El vehiculo se ha actualizado correctamente");
    }

    public List<VehicleResponsDTO> getAllByPersona(Long idPersona){
        return repository.findByPropietarioIdPersona(idPersona).stream().map(vehicleMap::map).toList();
    }

    public List<VehicleResponsDTO> getAll(){
        return repository.findAll().stream().map(vehicleMap::map).toList();
    }
}
