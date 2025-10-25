package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.ComercioAfiliado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComercioAfiliadoRepository extends JpaRepository<ComercioAfiliado, Long> {
    List<ComercioAfiliado> findComercioAfiliadoByEstado(ComercioAfiliado.Estado estado);

    boolean existsByNit(String nit);
}
