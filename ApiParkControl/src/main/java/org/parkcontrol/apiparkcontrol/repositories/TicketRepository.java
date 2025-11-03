package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Ticket;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEstado(Ticket.EstadoTicket estado);

    List<Ticket> findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket estado, Vehiculo.TipoVehiculo vehiculoTipoVehiculo);

    List<Ticket> findByVehiculo_IdAndEstado(Long vehiculoId, Ticket.EstadoTicket estado);
}
