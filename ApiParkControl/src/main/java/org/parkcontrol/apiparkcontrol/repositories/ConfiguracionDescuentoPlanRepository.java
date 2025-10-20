package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.ConfiguracionDescuentoPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionDescuentoPlanRepository extends JpaRepository<ConfiguracionDescuentoPlan, Long> {
    ConfiguracionDescuentoPlan findByTipoPlan_IdAndEstado(Long id, ConfiguracionDescuentoPlan.EstadoConfiguracion estadoConfiguracion);
}
