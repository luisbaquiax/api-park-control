package org.parkcontrol.apiparkcontrol.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;

@Mapper(componentModel = "spring")
public interface VehicleMap {
    @Mapping(source = "propietario.idPersona", target = "idPersona")
    VehicleResponsDTO map(Vehiculo vehiculo);
}
