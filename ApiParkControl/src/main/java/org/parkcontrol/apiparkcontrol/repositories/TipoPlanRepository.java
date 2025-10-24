package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.ConfiguracionDescuentoPlan;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.models.TipoPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPlanRepository extends JpaRepository<TipoPlan, Long> {
    List<TipoPlan> findByEmpresa_IdEmpresaAndActivo(Long empresaIdEmpresa, TipoPlan.EstadoConfiguracion activo);
}
