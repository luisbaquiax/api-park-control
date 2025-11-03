package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.PermisoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public interface PermisoTemporalRepository extends JpaRepository<PermisoTemporal, Long> {
    Optional<PermisoTemporal> findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(String placaTemporal, PermisoTemporal.TipoVehiculo tipoVehiculoPermitido, PermisoTemporal.EstadoPermiso estado, LocalDateTime fechaInicioBefore, LocalDateTime fechaFinAfter);
}
