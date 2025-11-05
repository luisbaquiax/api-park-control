package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.PermisoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

public interface PermisoTemporalRepository extends JpaRepository<PermisoTemporal, Long> {
  
    Optional<PermisoTemporal> findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(String placaTemporal, PermisoTemporal.TipoVehiculo tipoVehiculoPermitido, PermisoTemporal.EstadoPermiso estado, LocalDateTime fechaInicioBefore, LocalDateTime fechaFinAfter);
  
    List<PermisoTemporal> findBySuscripcion_Id(Long id);

    List<PermisoTemporal> findBySuscripcion_Empresa_IdEmpresa(Long idEmpresa);
}
