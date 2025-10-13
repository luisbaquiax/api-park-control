package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.TransaccionTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionTicketRepository extends JpaRepository<TransaccionTicket, Long> {
}
