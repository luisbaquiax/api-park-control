package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Backoffice;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackofficeRepository extends JpaRepository<Backoffice, Long> {
    List<Backoffice> findByUsuario_IdUsuario(Long usuarioIdUsuario);
}
