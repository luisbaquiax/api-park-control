package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.IncidenciaTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaTicketRepository extends JpaRepository<IncidenciaTicket, Long> {
    List<IncidenciaTicket> findByTicket_Sucursal_IdSucursal(Long idSucursal);
}
