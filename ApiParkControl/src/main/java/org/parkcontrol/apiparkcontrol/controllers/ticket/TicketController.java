package org.parkcontrol.apiparkcontrol.controllers.ticket;

import org.parkcontrol.apiparkcontrol.dto.ticket.CheckTicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.CobroResultadoDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketResponseDTO;
import org.parkcontrol.apiparkcontrol.models.Ticket;
import org.parkcontrol.apiparkcontrol.services.ticket.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @PostMapping("/create")
    public TicketResponseDTO createTicket(@RequestBody TicketRequestDTO ticketRequestDTO) {
        return ticketService.save(ticketRequestDTO);
    }

    @PostMapping("/close")
    public CobroResultadoDTO closeTicket(@RequestBody CheckTicketRequestDTO ticketRequestDTO) {
        return ticketService.cobrarTicket(ticketRequestDTO);
    }

    @GetMapping("/client/get-tickets-by-cliente/{idCliente}")
    public List<TicketResponseDTO> getTicketsByCliente(@PathVariable Long idCliente) {
        return ticketService.getTicketsByCliente(idCliente);
    }
}
