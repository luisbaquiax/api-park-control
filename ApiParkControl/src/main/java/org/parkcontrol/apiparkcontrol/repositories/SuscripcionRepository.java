package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Suscripcion;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    List<Suscripcion> findByUsuario_IdUsuario(Long idCliente);

    List<Suscripcion> findByVehiculo_IdAndEstado(Long vehiculoActual, Suscripcion.EstadoSuscripcion estadoSuscripcion);
}
