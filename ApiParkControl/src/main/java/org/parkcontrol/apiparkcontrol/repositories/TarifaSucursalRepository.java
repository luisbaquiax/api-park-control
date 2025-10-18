package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.TarifaSucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifaSucursalRepository extends JpaRepository<TarifaSucursal, Long> {
    List<TarifaSucursal> findBySucursal_IdSucursalAndEstado(Long idSucursal, TarifaSucursal.EstadoTarifaSucursal estadoTarifaSucursal);

    List<TarifaSucursal> findBySucursal_IdSucursal(Long idSucursal);
}
