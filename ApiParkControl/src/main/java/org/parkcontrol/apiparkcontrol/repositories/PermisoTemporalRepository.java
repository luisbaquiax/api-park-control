package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.PermisoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoTemporalRepository extends JpaRepository<PermisoTemporal, Long> {
}
