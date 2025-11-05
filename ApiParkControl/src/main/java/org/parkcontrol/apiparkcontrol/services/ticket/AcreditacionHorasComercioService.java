package org.parkcontrol.apiparkcontrol.services.ticket;

import org.parkcontrol.apiparkcontrol.repositories.AcreditacionHorasComercioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AcreditacionHorasComercioService {
    @Autowired
    private AcreditacionHorasComercioRepository acreditacionHorasComercioRepository;
}
