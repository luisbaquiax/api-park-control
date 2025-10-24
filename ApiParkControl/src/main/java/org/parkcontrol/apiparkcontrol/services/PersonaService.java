package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.repositories.PersonaRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonaService {
    @Autowired
    private PersonaRepository personaRepository;

    @Transactional
    public Persona create(Persona persona){
        Persona aux = personaRepository.findByDpi(persona.getDpi());
        if(aux!=null){
            throw new ErrorApi(401, String.format("El dpi %s ya se encuentra en uso.", persona.getDpi()));
        }
        if(personaRepository.findByCorreo(persona.getCorreo()) != null){
            throw new ErrorApi(401, String.format("El correo %s ya se encuentra en uso.", persona.getCorreo()));
        }
        Persona personaGuardar = new Persona();
        personaGuardar.setIdPersona(null);
        personaGuardar.setApellido(persona.getApellido());
        personaGuardar.setDpi(persona.getDpi());
        personaGuardar.setCiudad(persona.getCiudad());
        personaGuardar.setCorreo(persona.getCorreo());
        personaGuardar.setEstado(persona.getEstado());
        personaGuardar.setPais(persona.getPais());
        personaGuardar.setNombre(persona.getNombre());
        personaGuardar.setApellido(persona.getApellido());
        personaGuardar.setTelefono(persona.getTelefono());
        personaGuardar.setCodigoPostal(persona.getCodigoPostal());
        personaGuardar.setDireccionCompleta(persona.getDireccionCompleta());
        personaGuardar.setCiudad(persona.getCiudad());
        personaGuardar.setFechaNacimiento(persona.getFechaNacimiento());

        return personaRepository.save(personaGuardar);
    }

    @Transactional
    public Persona findByDpi(String dpi){
        return personaRepository.findByDpi(dpi);
    }


}
