package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dtoempresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;

@Mapper(componentModel = "spring")
public interface TarifaBaseMap {

    @Mapping(source = "empresa.idEmpresa", target = "idEmpresa")
    TarifaBaseResponse map(TarifaBase tarifaBase);
}
