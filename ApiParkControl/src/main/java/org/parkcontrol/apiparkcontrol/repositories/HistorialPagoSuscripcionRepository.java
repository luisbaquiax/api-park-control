package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.HistorialPagoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialPagoSuscripcionRepository extends JpaRepository<HistorialPagoSuscripcion, Long> {
    List<HistorialPagoSuscripcion> findBySuscripcion_Empresa_IdEmpresa(Long idEmpresa);
}
