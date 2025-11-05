package org.parkcontrol.apiparkcontrol.services.ticket;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.models.OcupacionSucursal;
import org.parkcontrol.apiparkcontrol.models.Ticket;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.repositories.OcupacionSucursalRepository;
import org.parkcontrol.apiparkcontrol.repositories.SucursalRepository;
import org.parkcontrol.apiparkcontrol.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class OcupacionService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private OcupacionSucursalRepository ocupacionSucursalRepository;
    @Autowired
    private SucursalRepository sucursalRepository;

    // Cada 15 minutos, con un retraso inicial de 30 segundos después del inicio del servicio
    @Scheduled(fixedDelay = 900000, initialDelay = 30000)
    @Transactional
    public void registrarOcupacionPeriodica() {
        var horaActual = LocalTime.now();
        var sucursales = sucursalRepository.findAll();
        for (var sucursal : sucursales) {

            if(horaActual.isBefore(sucursal.getHoraApertura()) || horaActual.isBefore(sucursal.getHoraCierre())){
                continue;
            }

            var ocupacion2R = ticketRepository.countBySucursal_IdSucursalAndEstadoAndVehiculo_TipoVehiculo(
                    sucursal.getIdSucursal(),
                    Ticket.EstadoTicket.ACTIVO,
                    Vehiculo.TipoVehiculo.DOS_RUEDAS
            );
            var ocupacion4R = ticketRepository.countBySucursal_IdSucursalAndEstadoAndVehiculo_TipoVehiculo(
                    sucursal.getIdSucursal(),
                    Ticket.EstadoTicket.ACTIVO,
                    Vehiculo.TipoVehiculo.CUATRO_RUEDAS
            );
            var capacidad2R = sucursal.getCapacidad2Ruedas();
            var capacidad4R = sucursal.getCapacidad4Ruedas();

            var porcentajeOcupacion2R = capacidad2R == 0 ? 0 :
                    (ocupacion2R * 100.0) / capacidad2R;
            var porcentajeOcupacion4R = capacidad4R == 0 ? 0 :
                    (ocupacion4R * 100.0) / capacidad4R;

            var ocupacionSucursal = new OcupacionSucursal();
            ocupacionSucursal.setSucursal(sucursal);
            ocupacionSucursal.setOcupacion2R(ocupacion2R);
            ocupacionSucursal.setCapacidad2R(capacidad2R);
            ocupacionSucursal.setOcupacion4R(ocupacion4R);
            ocupacionSucursal.setCapacidad4R(capacidad4R);
            ocupacionSucursal.setPorcentajeOcupacion2R(
                    BigDecimal.valueOf(porcentajeOcupacion2R).setScale(2, RoundingMode.HALF_UP)
            );
            ocupacionSucursal.setPorcentajeOcupacion4R(
                    BigDecimal.valueOf(porcentajeOcupacion4R).setScale(2, RoundingMode.HALF_UP)
            );

            ocupacionSucursalRepository.save(ocupacionSucursal);
        }
    }
}
