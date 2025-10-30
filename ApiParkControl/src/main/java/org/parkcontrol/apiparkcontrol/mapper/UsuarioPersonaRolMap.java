package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dto.empresa.UsuarioPersonaRolResponse;
import org.parkcontrol.apiparkcontrol.models.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioPersonaRolMap {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.nombreUsuario", target = "nombreUsuario")
    @Mapping(source = "usuario.rol.nombreRol", target = "nombreRol")
    @Mapping(source = "usuario.persona.nombre", target = "nombre")
    @Mapping(source = "usuario.persona.apellido", target = "apellido")
    @Mapping(source = "usuario.persona.idPersona", target = "idPersona")
    @Mapping(source = "usuario.persona.dpi", target = "dpi")
    UsuarioPersonaRolResponse map(Usuario usuario);
}
