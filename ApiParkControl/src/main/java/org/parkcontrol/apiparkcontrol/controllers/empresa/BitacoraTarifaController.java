package org.parkcontrol.apiparkcontrol.controllers.empresa;

import org.parkcontrol.apiparkcontrol.dto.empresa.BitacoraTarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.mapper.BitacoraTarifaBaseMapper;
import org.parkcontrol.apiparkcontrol.services.BitacoraTarifaBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresa/bitacora-tarifa")
public class BitacoraTarifaController {

    @Autowired
    private BitacoraTarifaBaseService bitacoraTarifaBaseService;
    @Autowired
    private BitacoraTarifaBaseMapper mapper;

    @GetMapping("/get-by-empresa/{idEmpresa}")
    public List<BitacoraTarifaBaseResponse> getByEmpresa(@PathVariable Long idEmpresa) {
        return bitacoraTarifaBaseService.getByEmpresa(idEmpresa).stream().map(mapper::map).toList();
    }
}
