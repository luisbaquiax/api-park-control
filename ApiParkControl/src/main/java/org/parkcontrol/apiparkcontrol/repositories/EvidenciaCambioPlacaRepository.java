package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.EvidenciaCambioPlaca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenciaCambioPlacaRepository extends JpaRepository<EvidenciaCambioPlaca, Long> {
    EvidenciaCambioPlaca findBySolicitudCambioPlac_Id(Long id);
}
