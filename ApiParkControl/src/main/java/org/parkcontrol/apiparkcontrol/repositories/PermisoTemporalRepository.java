package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.PermisoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermisoTemporalRepository extends JpaRepository<PermisoTemporal, Long> {
    List<PermisoTemporal> findBySuscripcion_Id(Long id);

    List<PermisoTemporal> findBySuscripcion_Empresa_IdEmpresa(Long idEmpresa);
}
