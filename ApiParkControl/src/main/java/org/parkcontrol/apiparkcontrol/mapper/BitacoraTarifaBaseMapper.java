package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dto.empresa.BitacoraTarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;

@Mapper(componentModel = "spring")
public interface BitacoraTarifaBaseMapper {

    @Mapping(source = "tarifaBase.idTarifaBase", target = "idTarifaBase")
    @Mapping(source = "usuarioResponsable.idUsuario", target = "idUsuarioResponsable")
    BitacoraTarifaBaseResponse map(BitacoraTarifaBase bitacoraTarifaBase);
}
