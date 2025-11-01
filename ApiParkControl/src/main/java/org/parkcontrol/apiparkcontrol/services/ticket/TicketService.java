package org.parkcontrol.apiparkcontrol.services.ticket;

import org.parkcontrol.apiparkcontrol.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
}
