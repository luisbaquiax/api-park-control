package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.SuscripcionFlotilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuscripcionFlotillaRepository extends JpaRepository<SuscripcionFlotilla, Long> {
    SuscripcionFlotilla findByPlanCorporativo_IdPlanCorporativoAndVehiculo_Id(Long idPlanCorporativo, Long id);

    SuscripcionFlotilla findByVehiculo_IdAndEstado(Long vehiculoId, SuscripcionFlotilla.EstadoSuscripcion estado);

    List<SuscripcionFlotilla> findByPlanCorporativo_IdPlanCorporativo(Long idPlanCorporativo);
}
