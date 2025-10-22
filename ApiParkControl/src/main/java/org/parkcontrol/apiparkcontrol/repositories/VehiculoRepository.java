package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByPropietario_IdPersona(Long idPersona);
}
