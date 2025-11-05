package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.CorteCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorteCajaRepository extends JpaRepository<CorteCaja, Long> {
    List<CorteCaja> findBySucursal_IdSucursal(Long idSucursal);

    CorteCaja findTopBySucursal_IdSucursalOrderByFechaFinDesc(Long idSucursal);

    CorteCaja findTopBySucursal_IdSucursalAndPeriodoOrderByFechaFinDesc(Long idSucursal, CorteCaja.Periodo periodo);
}
