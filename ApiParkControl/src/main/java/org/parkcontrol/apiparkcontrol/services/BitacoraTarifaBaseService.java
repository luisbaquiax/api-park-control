package org.parkcontrol.apiparkcontrol.services;

import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.repositories.BitacoraTarifaBaseRepository;
import org.parkcontrol.apiparkcontrol.repositories.EmpresaRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitacoraTarifaBaseService {
    @Autowired
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public List<BitacoraTarifaBase> getByEmpresa(Long idUsuario){
        List<Empresa> list = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario);
        if(list.isEmpty()){
            throw new ErrorApi(403, "El usuario no tiene una empresa asociada");
        }
        Empresa empresa = list.getFirst();
        return bitacoraTarifaBaseRepository.findByTarifaBase_Empresa_IdEmpresa(empresa.getIdEmpresa());
    }
}
