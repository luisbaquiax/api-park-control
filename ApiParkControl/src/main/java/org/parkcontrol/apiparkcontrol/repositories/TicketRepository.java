package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Ticket;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEstado(Ticket.EstadoTicket estado);

    List<Ticket> findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket estado, Vehiculo.TipoVehiculo vehiculoTipoVehiculo);

    List<Ticket> findByVehiculo_IdAndEstado(Long vehiculoId, Ticket.EstadoTicket estado);

    Optional<Ticket> findByFolioNumerico(String folioNumerico);

    Optional<Ticket> findByCodigoQr(String codigoQr);

    List<Ticket> findByVehiculo_Propietario_IdPersonaAndEstado(Long vehiculoPropietarioIdPersona, Ticket.EstadoTicket estado);

    Optional<Ticket> findByVehiculo_Placa(String placa);

    Integer countBySucursal_IdSucursalAndEstadoAndVehiculo_TipoVehiculo(Long sucursalIdSucursal, Ticket.EstadoTicket estado, Vehiculo.TipoVehiculo vehiculoTipoVehiculo);

    Optional<Ticket> findByFolioNumericoAndEstado(String folioNumerico, Ticket.EstadoTicket estado);

    Optional<Ticket> findByCodigoQrAndEstado(String codigoQr, Ticket.EstadoTicket estado);

    Optional<Ticket> findByVehiculo_PlacaAndEstado(String placa, Ticket.EstadoTicket estado);
    List<Ticket> findBySuscripcion_Id(Long idSuscripcion);
  
}
