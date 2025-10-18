package org.parkcontrol.apiparkcontrol.services;

import org.parkcontrol.apiparkcontrol.models.BitacoraSistema;
import org.parkcontrol.apiparkcontrol.repositories.BitacoraSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitacoraSistemaService {

    @Autowired
    private BitacoraSistemaRepository bitacoraSistemaRepository;

    public BitacoraSistema create(BitacoraSistema bitacoraSistema) {
        return bitacoraSistemaRepository.save(bitacoraSistema);
    }

    /**
     *
     * @return Logs del sistema
     */
    public List<BitacoraSistema> findAll() {
        return bitacoraSistemaRepository.findAll();
    }
}
