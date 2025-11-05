package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.AcreditacionHorasComercio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AcreditacionHorasComercioRepository extends JpaRepository<AcreditacionHorasComercio, Long> {
   
    AcreditacionHorasComercio findByTicket_Id(Long ticketId);
  
    List<AcreditacionHorasComercio> findByConvenio_ComercioAfiliado_Id(Long comercioAfiliadoId);
}
