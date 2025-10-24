package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dto.bitacoraSistema.BitacoraSistemaResponse;
import org.parkcontrol.apiparkcontrol.models.BitacoraSistema;

@Mapper(componentModel = "spring")
public interface BitacoraSistemaMap {

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    BitacoraSistemaResponse map(BitacoraSistema bitacoraSistema);
}
