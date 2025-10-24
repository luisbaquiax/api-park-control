package org.parkcontrol.apiparkcontrol.controllers.empresa;

import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.mapper.TarifaBaseMap;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresa/tarifa")
public class GestionTarifaController {
    @Autowired
    private TarifaBaseService tarifaBaseService;
    @Autowired
    private TarifaBaseMap tarifaBaseMap;

    @PostMapping("/create/{idUsuario}")
    public TarifaBaseResponse create(@RequestBody TarifaBaseResponse tarifaBaseResponse, @PathVariable Long idUsuario) {
        return tarifaBaseMap.map(tarifaBaseService.create(tarifaBaseResponse, idUsuario));
    }

    @PutMapping("/update/{idUsuario}")
    public TarifaBaseResponse update(@RequestBody TarifaBaseResponse tarifaBaseResponse, @PathVariable Long idUsuario) {
        return tarifaBaseMap.map(tarifaBaseService.update(tarifaBaseResponse, idUsuario));
    }

    @GetMapping("/get-by-status/{estado}/{idEmpresa}")
    public TarifaBaseResponse getByStatus(@PathVariable TarifaBase.EstadoTarifaBase estado, @PathVariable Long idEmpresa) {
        return tarifaBaseMap.map(tarifaBaseService.findTarifaBaseByEmpresaIdByEstado(estado, idEmpresa));
    }

    @PutMapping("/desactivar/{idTarifa}/{idUsuario}")
    public MessageSuccess desactivar(@PathVariable Long idTarifa, @PathVariable Long idUsuario) {
        tarifaBaseService.desactivarTarifaBase(idTarifa, idUsuario);
        return new MessageSuccess(500,"Tarifa desactivada correctamente");
    }
}
