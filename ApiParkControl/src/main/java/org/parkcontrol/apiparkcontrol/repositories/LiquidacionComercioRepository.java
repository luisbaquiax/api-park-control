package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.LiquidacionComercio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidacionComercioRepository extends JpaRepository<LiquidacionComercio, Long> {
    List<LiquidacionComercio> findByCorteCaja_IdCorteCaja(Long idCorteCaja);

    List<LiquidacionComercio> findByComercio_Id(Long comercioId);
}
