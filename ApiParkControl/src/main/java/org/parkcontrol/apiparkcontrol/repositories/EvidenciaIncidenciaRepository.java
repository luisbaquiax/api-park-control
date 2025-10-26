package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.EvidenciaIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaIncidenciaRepository extends JpaRepository<EvidenciaIncidencia, Long> {
    List<EvidenciaIncidencia> findByIncidencia_IdIncidencia(Long idIncidencia);
}
