package org.parkcontrol.apiparkcontrol.repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface TarifaBaseRepository extends JpaRepository<TarifaBase, Long> {
    /**
     * Verifica si existe un solapamiento en las tarifas base
     * @param idEmpresa
     * @param fechaInicio
     * @param fechaFin
     * @return existe solapamiento o no
     */
    @Query("""
                SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                FROM TarifaBase t
                WHERE t.empresa.idEmpresa = :idEmpresa
                AND t.estado = 'VIGENTE' OR t.estado = 'PROGRAMADO'
                AND (
                    (:fechaInicio BETWEEN t.fechaVigenciaInicio AND t.fechaVigenciaFin)
                    OR (:fechaFin BETWEEN t.fechaVigenciaInicio AND t.fechaVigenciaFin)
                    OR (t.fechaVigenciaInicio BETWEEN :fechaInicio AND :fechaFin)
                )
            """)
    boolean existeSolapamiento(
            @Param("idEmpresa") Long idEmpresa,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    TarifaBase findByEstadoAndEmpresa_IdEmpresa(TarifaBase.EstadoTarifaBase estado, Long idEmpresa);

}
