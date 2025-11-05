package org.parkcontrol.apiparkcontrol.repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.parkcontrol.apiparkcontrol.models.TarifaSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TarifaSucursalRepository extends JpaRepository<TarifaSucursal, Long> {
    List<TarifaSucursal> findBySucursal_IdSucursalAndEstado(Long idSucursal, TarifaSucursal.EstadoTarifaSucursal estadoTarifaSucursal);

    List<TarifaSucursal> findBySucursal_IdSucursal(Long idSucursal);

    /**
     * Busca la tarifa vigente para una sucursal en una fecha y hora específicas.
     * @param idSucursal ID de la sucursal.
     * @param fechaHora La fecha y hora para validar la vigencia.
     * @return La tarifa vigente en ese momento.
     */
    @Query("SELECT t FROM TarifaSucursal t WHERE t.sucursal.idSucursal = :idSucursal " +
            "AND t.estado = 'VIGENTE' " +
            "AND t.fechaVigenciaInicio <= :fechaHora " +
            "AND (t.fechaVigenciaFin IS NULL OR t.fechaVigenciaFin >= :fechaHora)")
    Optional<TarifaSucursal> findVigenteBySucursalIdAndFecha(
            @Param("idSucursal") Long idSucursal,
            @Param("fechaHora") LocalDateTime fechaHora
    );
}
