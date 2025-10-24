package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.RegisterUserDTO;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.models.Rol;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.repositories.PersonaRepository;
import org.parkcontrol.apiparkcontrol.repositories.RolRepository;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PersonaService personaService;

    private final Encriptation passwordEncoder = new Encriptation();

    @Transactional
    public Usuario registerUser(RegisterUserDTO userDTO) {
        if(usuarioRepository.findByNombreUsuario(userDTO.getNombreUsuario()) != null){
            throw new ErrorApi(401, String.format("El nombre de usuario %s ya se encuentra en uso.", userDTO.getNombreUsuario()));
        }
        // Crear y guardar la persona
        Persona persona = new Persona();
        persona.setNombre(userDTO.getNombre());
        persona.setApellido(userDTO.getApellido());
        persona.setFechaNacimiento(LocalDate.parse(userDTO.getFechaNacimiento()));
        persona.setDpi(userDTO.getDpi());
        persona.setCorreo(userDTO.getCorreo());
        persona.setTelefono(userDTO.getTelefono());
        persona.setDireccionCompleta(userDTO.getDireccionCompleta());
        persona.setCiudad(userDTO.getCiudad());
        persona.setPais(userDTO.getPais());
        persona.setCodigoPostal(userDTO.getCodigoPostal());
        persona.setEstado(Persona.Estado.ACTIVO);

        //Buscamos por el nombre del rol, si no existe lanza una excepción
        Rol rol = rolRepository.findByNombreRol("EMPRESA");
        // Crear y guardar el usuario
        Usuario usuario = new Usuario();
        usuario.setPersona(personaService.create(persona));
        usuario.setRol(rol);
        usuario.setNombreUsuario(userDTO.getNombreUsuario());
        usuario.setContraseniaHash(passwordEncoder.encrypt(userDTO.getContraseniaHash()));
        usuario.setDobleFactorHabilitado(userDTO.isDobleFactorHabilitado());
        usuario.setEstado(Usuario.EstadoUsuario.valueOf(userDTO.getEstado()));
        usuario.setDebeCambiarContrasenia(false);
        usuario.setEsPrimeraVez(false);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        return usuarioRepository.save(usuario);
    }

    /**
     * Get user with rol name
     * @param nameRol name of rol
     * @return list of user
     */
    public List<Usuario> getUsersByRol(String nameRol) {
        Rol rol = rolRepository.findByNombreRol(nameRol);
        if (rol == null) {
            throw new ErrorApi(404, "Rol no encontrado");
        }
        return usuarioRepository.getByRol_NombreRol(rol.getNombreRol());
    }
}
