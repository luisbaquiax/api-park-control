package org.parkcontrol.apiparkcontrol.controllers.ticket;

import org.parkcontrol.apiparkcontrol.dto.ticket.TicketRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.ticket.TicketResponseDTO;
import org.parkcontrol.apiparkcontrol.services.ticket.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @PostMapping("/create")
    public TicketResponseDTO createTicket(@RequestBody TicketRequestDTO ticketRequestDTO) {
        return ticketService.save(ticketRequestDTO);
    }
}
