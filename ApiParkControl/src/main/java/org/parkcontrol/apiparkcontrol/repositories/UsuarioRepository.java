package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> getByRol_NombreRol(String rolNombreRol);
    Usuario findByNombreUsuario(String nombreUsuario);
    Usuario findByPersona(Persona persona);
    Usuario findByIdUsuarioAndEstado(Long idCliente, Usuario.EstadoUsuario estadoUsuario);
}
