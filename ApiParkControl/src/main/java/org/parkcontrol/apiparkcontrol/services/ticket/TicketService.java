package org.parkcontrol.apiparkcontrol.services.ticket;

import com.google.zxing.WriterException;
import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.ticket.CheckTicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.CobroResultadoDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketResponseDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.parkcontrol.apiparkcontrol.utils.GeneradorCodigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
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
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Autowired
    private HistorialConsumoSuscripcionRepository historialConsumoSuscripcionRepository;
    @Autowired
    private TarifaBaseService tarifaBaseService;
    @Autowired
    private TransaccionTicketRepository transaccionRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private GeneradorCodigo generadorCodigo;

    public TicketService() {
        this.generadorCodigo = new GeneradorCodigo();
    }

    @Transactional
    public TicketResponseDTO save(TicketRequestDTO ticketRequest) {
        //validar usuario sucursal
        Usuario usuario = usuarioRepository.findById(ticketRequest.getIdUsuario()).orElseThrow(() -> new ErrorApi(404, "Usuario no encontrado"));
        if (!usuario.getRol().getNombreRol().equalsIgnoreCase("SUCURSAL")) {
            throw new ErrorApi(404, "Rol de usuario no autorizado para generar tickets");
        }
        //obtener sucursal
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(usuario.getIdUsuario());
        if (sucursal == null) {
            throw new ErrorApi(404, "Sucursal no asignado para el usuario");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(ticketRequest.getIdVehiculo()).orElseThrow(() -> new ErrorApi(404, "Vehiculo no encontrado"));

        //revisar tipo vehiculo
        //revisar capacidad sucursal para el tipo de vehiculo ya sea 2 ruedas o 4 ruedas
        List<Ticket> ticketsActivosDosRuedas = ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.DOS_RUEDAS);
        List<Ticket> ticketsActivosCuatroRuedas = ticketRepository.findByEstadoAndVehiculo_TipoVehiculo(Ticket.EstadoTicket.ACTIVO, Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        if (vehiculo.getTipoVehiculo() == Vehiculo.TipoVehiculo.DOS_RUEDAS) {
            if (ticketsActivosDosRuedas.size() >= sucursal.getCapacidad2Ruedas()) {
                throw new ErrorApi(400, String.format(
                        "Capacidad máxima (%d) de vehículos de dos ruedas alcanzada en la sucursal %s.", sucursal.getCapacidad2Ruedas(), sucursal.getNombre()));
            }
        } else if (vehiculo.getTipoVehiculo() == Vehiculo.TipoVehiculo.CUATRO_RUEDAS) {
            if (ticketsActivosCuatroRuedas.size() >= sucursal.getCapacidad4Ruedas()) {
                throw new ErrorApi(400, String.format(
                        "Capacidad máxima (%d) de vehículos de cuatro ruedas alcanzada en la sucursal %s.", sucursal.getCapacidad4Ruedas(), sucursal.getNombre()));
            }
        } else {
            throw new ErrorApi(400, "Tipo de vehículo no reconocido");
        }
        //buscar al cliente propietario del vehículo
        Persona cliente = vehiculo.getPropietario();
        Usuario usarioCliente = usuarioRepository.findByPersona(cliente);
        if (usarioCliente == null) {
            throw new ErrorApi(404, "El propietario del vehiculo no tiene una cuenta de usuario asociada");
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

        if (permisoAplicado != null) {
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
        if (!ticketActivos.isEmpty()) {
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
        if (permisoAplicado != null) {
            permisoAplicado.setUsosRealizados(permisoAplicado.getUsosRealizados() + 1);
            permisoTemporalRepository.save(permisoAplicado);
        }
        //guardar qr en redis
        //TODO: definir tiempo de expiración del código qr en redis según la política de la empresa
        //guardar ticket
        return getTicketResponseDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public CobroResultadoDTO cobrarTicket(CheckTicketRequestDTO ticketRequest) {
        // 1. Identificar el ticket
        Ticket validatedTicket = identificarTicket(ticketRequest);
        if(!validatedTicket.getSucursal().getUsuarioSucursal().getIdUsuario().equals(ticketRequest.getIdUsuario())){
            throw new ErrorApi(403, String.format("El usuario no está autorizado para cobrar tickets en la sucursal %s.", validatedTicket.getSucursal().getNombre()));
        }
        // 2. Iniciar proceso de cobro si el ticket es válido
        // y enviar respuesta
        return calcularCobroYFinalizarTicket(validatedTicket, LocalDateTime.now());
    }

    private CobroResultadoDTO calcularCobroYFinalizarTicket(Ticket ticket, LocalDateTime fechaHoraSalida) {
        Duration duracion = Duration.between(ticket.getFechaHoraEntrada(), fechaHoraSalida);
        long duracionMinutosTotal = duracion.toMinutes();

        // Tipo de cobro inicial
        TransaccionTicket.TipoCobro tipoCobro = TransaccionTicket.TipoCobro.TARIFA_NORMAL;

        // 1. Aplicar descuento de horas acreditadas por comercios
        // Obtener el total de horas acreditadas por comercios
        BigDecimal horasComercioAcreditadas = BigDecimal.ZERO;
        AcreditacionHorasComercio acreditacionHorasComercio = acreditacionHorasComercioRepository.findByTicket_Id(ticket.getId());
        if (acreditacionHorasComercio != null) {
            horasComercioAcreditadas = acreditacionHorasComercio.getHorasOtorgadas();
        }
        long minutosComercioGratis = horasComercioAcreditadas.multiply(BigDecimal.valueOf(60)).longValue();

        // Minutos del parqueo que deben ser cubiertos por el cliente (tiempo total - horas gratis)
        long minutosPagablesBrutos = Math.max(0, duracionMinutosTotal - minutosComercioGratis);

        BigDecimal horasCobradasCliente = BigDecimal.ZERO;
        BigDecimal tarifaAplicable;

        // 2. Aplicar suscripción dependiendo del tipo de plan
        if (ticket.getSuscripcion() != null) {

            Suscripcion suscripcion = ticket.getSuscripcion();
            TipoPlan plan = suscripcion.getTipoPlan();

            // --- 2.1. CÁLCULO DE TIEMPO PAGABLE Y CONSUMIBLE, revisar minuto a minuto

            LocalDateTime horaIteradora = ticket.getFechaHoraEntrada();
            long minutosPagablesPorRegla = 0; // Minutos que NO cumplen el día/hora del plan.
            long minutosIterados = 0;

            // Iterar SOLO el tiempo que no fue cubierto por el comercio.
            while (minutosIterados < minutosPagablesBrutos) {
                // se revisar minuto a minuto si está cubierto por el plan
                if (!estaCubiertoPorPlan(plan, horaIteradora)) {
                    // El minuto está fuera del horario/día del plan. ¡COBRO INMEDIATO!
                    minutosPagablesPorRegla++;
                }

                horaIteradora = horaIteradora.plusMinutes(1);
                minutosIterados++;
            }

            // El tiempo que SÍ cumplió la regla y es potencial consumo del bolsón
            long minutosPotencialConsumo = minutosPagablesBrutos - minutosPagablesPorRegla;

            // --- 2.2. APLICAR CONSUMO DEL BOLSÓN
            BigDecimal horasPotencialConsumo = BigDecimal.valueOf(minutosPotencialConsumo)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.CEILING);

            BigDecimal horasExcedentesCobro = BigDecimal.ZERO;
            BigDecimal horasConsumidasDelPlan = BigDecimal.ZERO;

            // Horas disponibles en el plan
            BigDecimal horasDisponibles = BigDecimal.valueOf(suscripcion.getHorasMensualesIncluidas())
                    .subtract(suscripcion.getHorasConsumidas());

            if (horasDisponibles.compareTo(horasPotencialConsumo) >= 0) {
                // El plan cubre todo.
                horasConsumidasDelPlan = horasPotencialConsumo;
            } else {
                // Hay excedente: el plan se agota y el resto se cobra.
                // Consume el saldo restante
                horasConsumidasDelPlan = horasDisponibles;
                // El resto es excedente
                horasExcedentesCobro = horasPotencialConsumo.subtract(horasDisponibles);
            }

            // Minutos No cubiertos (por Regla) + Minutos Excedente (por Bolsón)
            long minutosPagablesPorTotal = minutosPagablesPorRegla +
                    (horasExcedentesCobro.multiply(BigDecimal.valueOf(60)).longValue());

            // Horas a cobrar al cliente (ya redondeado para el cobro)
            horasCobradasCliente = BigDecimal.valueOf(minutosPagablesPorTotal)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.CEILING);


            if (horasCobradasCliente.compareTo(BigDecimal.ZERO) > 0) {
                //cobro cuando se existe excedente
                tipoCobro = TransaccionTicket.TipoCobro.EXCEDENTE_SUSCRIPCION;
                // Se cobra la Tarifa Vigente (Sucursal -> Base)
                tarifaAplicable = tarifaBaseService.tarifaVigenteSucursal(ticket.getSucursal());

            } else {
                // No hay cobro (todo fue cubierto por el plan y comercio)
                tipoCobro = TransaccionTicket.TipoCobro.TARIFA_NORMAL;
                tarifaAplicable = BigDecimal.ZERO;
            }

            // 4. Actualizar suscripción (consumo de horas)
            suscripcion.setHorasConsumidas(suscripcion.getHorasConsumidas().add(horasConsumidasDelPlan));
            suscripcionRepository.save(suscripcion);

            // El registro de historial usa las horas que el plan absorbió.

            registrarHistorialConsumoSuscripcion(suscripcion, ticket, horasConsumidasDelPlan, horasComercioAcreditadas);

        } else {
            // 3. CLIENTE SIN SUSCRIPCIÓN (Se cobra el tiempo restante)

            // Se cobra el tiempo total después del descuento de comercio.
            tarifaAplicable = tarifaBaseService.tarifaVigenteSucursal(ticket.getSucursal());

            // Convertir minutosPagablesBrutos a horas para el cobro
            horasCobradasCliente = BigDecimal.valueOf(minutosPagablesBrutos)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.CEILING);
        }

        // 5. CÁLCULO FINAL DE LA TRANSACCIÓN
        // Aplica para Suscriptor (solo excedente) y No Suscriptor (horas brutas)
        BigDecimal subtotal = horasCobradasCliente.multiply(tarifaAplicable);
        //descuentos adicionales, no hay pero quedo aquí por si hay otro descuento
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal totalAPagar = subtotal.subtract(descuento);

        // 6. REGISTRAR TRANSACCIÓN Y FINALIZAR TICKET
        TransaccionTicket transaccion = crearTransaccion(
                ticket,
                tipoCobro,
                subtotal,
                totalAPagar,
                tarifaAplicable,
                horasCobradasCliente.doubleValue(),
                horasComercioAcreditadas.doubleValue(),
                "EFECTIVO"
        );
        transaccionRepository.save(transaccion);

        ticket.setFechaHoraSalida(fechaHoraSalida);
        ticket.setDuracionMinutos((int) duracionMinutosTotal);
        ticket.setEstado(Ticket.EstadoTicket.FINALIZADO);
        ticketRepository.save(ticket);

        return new CobroResultadoDTO(
                horasCobradasCliente,
                tarifaAplicable,
                subtotal,
                totalAPagar,
                horasComercioAcreditadas,
                LocalDate.now(),
                ticket.getFechaHoraEntrada().toLocalDate(),
                ticket.getFechaHoraSalida().toLocalDate()
        );
    }

    private void registrarHistorialConsumoSuscripcion(Suscripcion suscripcion, Ticket ticket, BigDecimal horasConsumidas, BigDecimal horasComercio) {
        HistorialConsumoSuscripcion historial = new HistorialConsumoSuscripcion();
        historial.setSuscripcion(suscripcion);
        historial.setTicket(ticket);
        historial.setHorasConsumidas(horasConsumidas);
        historial.setHorasComercio(horasComercio);
        historialConsumoSuscripcionRepository.save(historial);
    }

    private TransaccionTicket crearTransaccion(
            Ticket ticket,
            TransaccionTicket.TipoCobro tipoCobro,
            BigDecimal subtotal,
            BigDecimal total,
            BigDecimal tarifaAplicada,
            Double horasCobradas,
            Double horasGratis,
            String metodoPago) {
        return new TransaccionTicket(
                null,
                ticket,
                tipoCobro,
                BigDecimal.valueOf(horasCobradas),
                BigDecimal.valueOf(horasGratis),
                tarifaAplicada,
                subtotal,
                BigDecimal.ZERO,
                total,
                metodoPago,
                generadorCodigo.getCode(),
                TransaccionTicket.Estado.PENDIENTE,
                LocalDateTime.now()
        );
    }

    private boolean estaCubiertoPorPlan(TipoPlan plan, LocalDateTime fechaHora) {
        // 1. VALIDAR DÍA DE LA SEMANA
        DayOfWeek diaSemana = fechaHora.getDayOfWeek();
        //"L-M-X-J-V"
        String diaCodigo = switch (diaSemana) {
            case MONDAY -> "L";
            case TUESDAY -> "M";
            case WEDNESDAY -> "X";
            case THURSDAY -> "J";
            case FRIDAY -> "V";
            case SATURDAY -> "S";
            case SUNDAY -> "D";
        };
        if (!plan.getDiasAplicables().contains(diaCodigo)) {
            // No está cubierto por el plan (Cobro como excedente)
            return false;
        }

        // 2. VALIDAR HORA DEL DÍA
        // como tenemos "08:00 - 16:00"
        String[] partes = plan.getCoberturaHoraria().split(" - ");
        LocalTime inicio = LocalTime.parse(partes[0]);
        LocalTime fin = LocalTime.parse(partes[1]);
        LocalTime horaActual = fechaHora.toLocalTime();

        // Caso Nocturno (Ej: 20:00 - 06:00, cruza la medianoche)
        if (inicio.isAfter(fin)) {
            // La hora debe ser después del inicio O antes del final
            return horaActual.isAfter(inicio) || horaActual.isBefore(fin);
        }
        // Caso Diurno (Ej: 08:00 - 16:00, no cruza la medianoche)
        else {
            return (horaActual.equals(inicio) || horaActual.isAfter(inicio))
                    && horaActual.isBefore(fin);
        }
    }


    private Ticket identificarTicket(CheckTicketRequestDTO ticketRequest) {
        if (!ticketRequest.getCodigoQr().isBlank()) {
            return validarTicketPorCodigoQr(ticketRequest.getCodigoQr());
        }
        if (!ticketRequest.getFolio().isBlank()) {
            return validarTicketPorFolio(ticketRequest.getFolio());
        }
        if (!ticketRequest.getPlaca().isBlank()) {
            return validarTicketPorPlaca(ticketRequest.getPlaca());
        }
        throw new ErrorApi(404, "Ticket no encontrado. Verifique la llave de búsqueda proporcionada o el estado del mismo.");
    }


    private Ticket validarTicketPorFolio(String folioNumerico) {
        return ticketRepository.findByFolioNumericoAndEstado(folioNumerico, Ticket.EstadoTicket.ACTIVO).orElseThrow(() -> new ErrorApi(404, "Ticket no encontrado para el folio proporcionado"));
    }

    private Ticket validarTicketPorPlaca(String placa) {
        return ticketRepository.findByVehiculo_PlacaAndEstado(placa, Ticket.EstadoTicket.ACTIVO).orElseThrow(() -> new ErrorApi(404, "Ticket no encontrado para la placa proporcionada"));
    }

    private Ticket validarTicketPorCodigoQr(String codigoQr) {
        return ticketRepository.findByCodigoQrAndEstado(codigoQr, Ticket.EstadoTicket.ACTIVO).orElse(null);
    }

    @Transactional
    public List<TicketResponseDTO> getTicketsByCliente(Long idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente).orElseThrow(() -> new ErrorApi(404, "Usuario no encontrado"));
        if (!cliente.getRol().getNombreRol().equalsIgnoreCase("CLIENTE")) {
            throw new ErrorApi(400, "El usuario no es un cliente");
        }
        Persona persona = cliente.getPersona();
        List<Ticket> tickets = ticketRepository.findByVehiculo_Propietario_IdPersonaAndEstado(persona.getIdPersona(), Ticket.EstadoTicket.ACTIVO);
        return tickets.stream().map(this::getTicketResponseDTO).toList();
    }

    private TicketResponseDTO getTicketResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getFolioNumerico(),
                ticket.getSucursal().getIdSucursal(),
                ticket.getSucursal().getNombre(),
                ticket.getVehiculo().getId(),
                ticket.getVehiculo().getPlaca(),
                ticket.getVehiculo().getTipoVehiculo(),
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
