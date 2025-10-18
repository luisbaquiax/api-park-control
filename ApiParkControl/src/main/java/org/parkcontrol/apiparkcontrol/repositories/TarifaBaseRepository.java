package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaBaseRepository extends JpaRepository<TarifaBase, Long> {
    TarifaBase findByEmpresa_IdEmpresaAndEstado(Long idEmpresa, TarifaBase.EstadoTarifaBase estadoTarifaBase);
}
