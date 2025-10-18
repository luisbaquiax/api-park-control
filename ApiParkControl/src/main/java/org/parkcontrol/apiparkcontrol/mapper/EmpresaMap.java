package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dtoempresa.EmpresaResponse;
import org.parkcontrol.apiparkcontrol.models.Empresa;

@Mapper(componentModel = "spring")
public interface EmpresaMap {

    @Mapping(target = "idUsuarioEmpresa", source = "usuarioEmpresa.idUsuario")
    EmpresaResponse map(Empresa empresa);
}
