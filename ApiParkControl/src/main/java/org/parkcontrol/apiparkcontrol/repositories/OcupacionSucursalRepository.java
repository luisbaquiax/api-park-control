package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.OcupacionSucursal;
import org.parkcontrol.apiparkcontrol.models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcupacionSucursalRepository extends JpaRepository<OcupacionSucursal, Long> {
    OcupacionSucursal findTopBySucursalOrderByFechaHoraDesc(Sucursal sucursal);
}
