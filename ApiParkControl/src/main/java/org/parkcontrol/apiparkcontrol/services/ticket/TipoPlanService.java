package org.parkcontrol.apiparkcontrol.services.ticket;

import org.parkcontrol.apiparkcontrol.repositories.TipoPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TipoPlanService {
    @Autowired
    private TipoPlanRepository tipoPlanRepository;


}
