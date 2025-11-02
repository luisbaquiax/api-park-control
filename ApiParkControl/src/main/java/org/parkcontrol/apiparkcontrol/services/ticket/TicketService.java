package org.parkcontrol.apiparkcontrol.services.ticket;

import com.google.zxing.WriterException;
import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketResponseDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.parkcontrol.apiparkcontrol.utils.GeneradorCodigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private GeneradorCodigo generadorCodigo;

    public TicketService() {
        this.generadorCodigo = new GeneradorCodigo();
    }

    @Transactional
    public TicketResponseDTO save(TicketRequestDTO ticketRequest) {
        //validar usuario sucursal
        Usuario usuario = usuarioRepository.findById(ticketRequest.getIdUsuario()).orElseThrow(() -> new ErrorApi(404,"Usuario no encontrado"));
        if(!usuario.getRol().getNombreRol().equalsIgnoreCase("SUCURSAL")) {
            throw new ErrorApi(404,"Rol de usuario no autorizado para generar tickets");
        }
        //obtener sucursal
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(usuario.getIdUsuario());
        if(sucursal == null) {
            throw new ErrorApi(404,"Sucursal no asignado para el usuario");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(ticketRequest.getIdVehiculo()).orElseThrow(() -> new ErrorApi(404,"Vehiculo no encontrado"));

        //revisar tipo vehiculo
        //revisar capacidad sucursal para el tipo de vehiculo ya sea 2 ruedas o 4 ruedas
        List<Ticket> ticketsActivosDosRuedas = ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.DOS_RUEDAS);
        List<Ticket> ticketsActivosCuatroRuedas = ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        if(vehiculo.getTipoVehiculo() == Vehiculo.TipoVehiculo.DOS_RUEDAS) {
            if(ticketsActivosDosRuedas.size() >= sucursal.getCapacidad2Ruedas()) {
                throw new ErrorApi(400,String.format(
                        "Capacidad máxima (%d) de vehículos de dos ruedas alcanzada en la sucursal %s.", sucursal.getCapacidad2Ruedas(), sucursal.getNombre()));
            }
        } else if(vehiculo.getTipoVehiculo() == Vehiculo.TipoVehiculo.CUATRO_RUEDAS) {
            if(ticketsActivosCuatroRuedas.size() >= sucursal.getCapacidad4Ruedas()) {
                throw new ErrorApi(400,String.format(
                        "Capacidad máxima (%d) de vehículos de cuatro ruedas alcanzada en la sucursal %s.", sucursal.getCapacidad4Ruedas(), sucursal.getNombre()));
            }
        } else {
            throw new ErrorApi(400,"Tipo de vehículo no reconocido");
        }

        Persona cliente = vehiculo.getPropietario();
        Usuario usarioCliente = usuarioRepository.findByPersona(cliente);
        if(usarioCliente == null) {
            throw new ErrorApi(404,"El propietario del vehiculo no tiene una cuenta de usuario asociada");
        }

        //revisamos si el vehículo tiene una suscripción activa
        Suscripcion suscripcionActual = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), Suscripcion.EstadoSuscripcion.ACTIVA)
                .stream()
                .findFirst()
                .orElse(null);

        Ticket ticket = new Ticket();
        ticket.setEstado(Ticket.EstadoTicket.ACTIVO);
        //definir tipo cliente según tenga o no suscripción activa
        Ticket.TipoCliente tipoCliente = suscripcionActual != null ? Ticket.TipoCliente.SUSCRIPTOR : Ticket.TipoCliente.SIN_SUSCRIPCION;
        ticket.setTipoCliente(tipoCliente);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setFechaHoraEntrada(ticketRequest.getFechaHoraEntrada());
        ticket.setPermisoTemporal(null);
        ticket.setSucursal(sucursal);
        ticket.setSuscripcion(suscripcionActual);
        ticket.setVehiculo(vehiculo);
        ticket.setEnlaceSmsWhatsapp("");

        String folioNumerico = generadorCodigo.getFolio(sucursal.getIdSucursal());
        String codigoQr = "";
        try {
            codigoQr = generadorCodigo.generateQR(folioNumerico);
        } catch (IOException | WriterException e) {
            throw new ErrorApi(400, "Error al generar el código QR para el ticket. " + e.getMessage());
        }

        ticket.setFolioNumerico(folioNumerico);
        ticket.setCodigoQr(codigoQr);
        //guardar qr en redis
        //TODO: definir tiempo de expiración del código qr en redis según la política de la empresa
        //guardar ticket
        Ticket ticketGuardado = ticketRepository.save(ticket);
        Long idSuscripcion = (ticketGuardado.getSuscripcion() == null) ? null : ticketGuardado.getSuscripcion().getId();
        Long idPermiso = (ticketGuardado.getPermisoTemporal() == null) ? null : ticketGuardado.getPermisoTemporal().getId();
        return new TicketResponseDTO(
                ticketGuardado.getId(),
                ticketGuardado.getFolioNumerico(),
                ticketGuardado.getSucursal().getIdSucursal(),
                ticketGuardado.getVehiculo().getId(),
                idSuscripcion,
                idPermiso,
                ticketGuardado.getTipoCliente().toString(),
                ticketGuardado.getFechaHoraEntrada(),
                ticketGuardado.getFechaHoraSalida(),
                ticketGuardado.getDuracionMinutos(),
                ticketGuardado.getCodigoQr(),
                ticketGuardado.getEnlaceSmsWhatsapp(),
                ticketGuardado.getEstado().toString(),
                ticketGuardado.getFechaCreacion()
        );
    }
}
