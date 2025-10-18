package org.parkcontrol.apiparkcontrol.services;

import org.parkcontrol.apiparkcontrol.models.Rol;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.repositories.RolRepository;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;

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
