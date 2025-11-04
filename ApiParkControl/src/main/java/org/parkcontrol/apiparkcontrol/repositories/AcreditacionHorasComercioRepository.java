package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.AcreditacionHorasComercio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcreditacionHorasComercioRepository extends JpaRepository<AcreditacionHorasComercio, Long> {
    List<AcreditacionHorasComercio>  findByConvenio_ComercioAfiliado_Id(Long comercioAfiliadoId);
}
