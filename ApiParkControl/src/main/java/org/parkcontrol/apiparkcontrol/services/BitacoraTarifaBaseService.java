package org.parkcontrol.apiparkcontrol.services;

import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.parkcontrol.apiparkcontrol.repositories.BitacoraTarifaBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitacoraTarifaBaseService {
    @Autowired
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;

    public List<BitacoraTarifaBase> getByEmpresa(Long idEmpresa){
        return bitacoraTarifaBaseRepository.findByTarifaBase_Empresa_IdEmpresa(idEmpresa);
    }
}
