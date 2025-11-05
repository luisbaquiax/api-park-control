package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.AcreditacionHorasComercio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface AcreditacionHorasComercioRepository extends JpaRepository<AcreditacionHorasComercio, Long> {
    AcreditacionHorasComercio findByTicket_Id(Long ticketId);
}
