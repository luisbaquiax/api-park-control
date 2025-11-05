package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.PlanCorporativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanCorporativoRepository extends JpaRepository<PlanCorporativo, Long> {
    PlanCorporativo findByEmpresaFlotilla_IdEmpresaFlotillaAndEstado(Long idEmpresaFlotilla, PlanCorporativo.EstadoPlanCorporativo estadoPlanCorporativo);

    List<PlanCorporativo> findByEmpresaFlotilla_IdEmpresaFlotilla(Long idEmpresaFlotilla);

    List<PlanCorporativo> findByEmpresaFlotilla_IdEmpresaFlotillaAndTipoPlan_Empresa_IdEmpresa(Long empresaFlotillaIdEmpresaFlotilla, Long tipoPlanEmpresaIdEmpresa);
}
