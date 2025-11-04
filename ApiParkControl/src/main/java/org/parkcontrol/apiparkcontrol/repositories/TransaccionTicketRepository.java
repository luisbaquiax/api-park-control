package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.TransaccionTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionTicketRepository extends JpaRepository<TransaccionTicket, Long> {
    List<TransaccionTicket> findByTicket_Sucursal_Empresa_IdEmpresa(Long ticketSucursalEmpresaIdEmpresa);
    List<TransaccionTicket> findByTicket_Sucursal_IdSucursal(Long ticketSucursalIdSucursal);

    TransaccionTicket findByTicket_Id(Long id);
}
