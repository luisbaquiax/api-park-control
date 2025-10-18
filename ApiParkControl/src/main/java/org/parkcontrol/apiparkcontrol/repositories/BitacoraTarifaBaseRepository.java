package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraTarifaBaseRepository extends JpaRepository<BitacoraTarifaBase, Long> {
    List<BitacoraTarifaBase> findByTarifaBase_Empresa_IdEmpresa(Long tarifaBaseEmpresaIdEmpresa);
}
