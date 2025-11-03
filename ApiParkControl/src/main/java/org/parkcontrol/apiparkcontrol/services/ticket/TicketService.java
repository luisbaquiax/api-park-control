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
    private PermisoTemporalRepository permisoTemporalRepository;
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
        //buscar al cliente propietario del vehículo
        Persona cliente = vehiculo.getPropietario();
        Usuario usarioCliente = usuarioRepository.findByPersona(cliente);
        if(usarioCliente == null) {
            throw new ErrorApi(404,"El propietario del vehiculo no tiene una cuenta de usuario asociada");
        }

        //revisar si existe permiso temporal activo para el vehículo
        PermisoTemporal permisoAplicado = permisoTemporalRepository
                .findByPlacaTemporalAndTipoVehiculoPermitidoAndEstadoAndFechaInicioBeforeAndFechaFinAfter(
                        vehiculo.getPlaca(),
                        PermisoTemporal.TipoVehiculo.valueOf(vehiculo.getTipoVehiculo().toString()),
                        PermisoTemporal.EstadoPermiso.ACTIVO,
                        ticketRequest.getFechaHoraEntrada(),
                        ticketRequest.getFechaHoraEntrada()
                )
                .stream()
                // Filtramos si el permiso tiene un límite de usos y no se ha agotado
                .filter(p -> p.getUsosMaximos() == null || p.getUsosRealizados() < p.getUsosMaximos())
                .findFirst()
                .orElse(null);



        //revisamos si el vehículo tiene una suscripción activa
        /*
        Suscripcion suscripcionActual = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), Suscripcion.EstadoSuscripcion.ACTIVA)
                .stream()
                .findFirst()
                .orElse(null);
                */
        Suscripcion suscripcionAplicada = null;
        PermisoTemporal permisoTemporalFinal = null;

        if(permisoAplicado != null) {
            suscripcionAplicada = permisoAplicado.getSuscripcion();
            permisoTemporalFinal = permisoAplicado;
        } else {
            suscripcionAplicada = suscripcionRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), Suscripcion.EstadoSuscripcion.ACTIVA)
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        //revisar si el vehiculo tiene un ticket activo
        List<Ticket> ticketActivos = ticketRepository.findByVehiculo_IdAndEstado(vehiculo.getId(), Ticket.EstadoTicket.ACTIVO);
        if(!ticketActivos.isEmpty()) {
            throw new ErrorApi(400, "El vehículo ya tiene un ticket activo. No se puede generar un nuevo ticket hasta que se cierre el ticket actual.");
        }

        //crear ticket
        Ticket ticket = new Ticket();
        ticket.setEstado(Ticket.EstadoTicket.ACTIVO);
        //definir tipo cliente según tenga o no suscripción activa
        Ticket.TipoCliente tipoCliente = suscripcionAplicada != null ? Ticket.TipoCliente.SUSCRIPTOR : Ticket.TipoCliente.SIN_SUSCRIPCION;
        ticket.setTipoCliente(tipoCliente);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setFechaHoraEntrada(ticketRequest.getFechaHoraEntrada());
        //por ahora no se asigna permiso temporal al crear el ticket
        ticket.setPermisoTemporal(permisoTemporalFinal);
        ticket.setSucursal(sucursal);
        ticket.setSuscripcion(suscripcionAplicada);
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

        //actualizamos el uso realizado del permiso temporal si aplica
        if(permisoAplicado != null) {
            permisoAplicado.setUsosRealizados(permisoAplicado.getUsosRealizados() + 1);
            permisoTemporalRepository.save(permisoAplicado);
        }
        //guardar qr en redis
        //TODO: definir tiempo de expiración del código qr en redis según la política de la empresa
        //guardar ticket
        return getTicketResponseDTO(ticketRepository.save(ticket));
    }

    private TicketResponseDTO getTicketResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getFolioNumerico(),
                ticket.getSucursal().getIdSucursal(),
                ticket.getVehiculo().getId(),
                (ticket.getSuscripcion() == null) ? null : ticket.getSuscripcion().getId(),
                (ticket.getPermisoTemporal() == null) ? null : ticket.getPermisoTemporal().getId(),
                ticket.getTipoCliente().toString(),
                ticket.getFechaHoraEntrada(),
                ticket.getFechaHoraSalida(),
                ticket.getDuracionMinutos(),
                ticket.getCodigoQr(),
                ticket.getEnlaceSmsWhatsapp(),
                ticket.getEstado().toString(),
                ticket.getFechaCreacion()
        );
    }
}
