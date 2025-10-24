package org.parkcontrol.apiparkcontrol.controllers.empresa;

import org.parkcontrol.apiparkcontrol.dto.empresa.EmpresaResponse;
import org.parkcontrol.apiparkcontrol.dto.empresa.RegisterEmpresa;
import org.parkcontrol.apiparkcontrol.mapper.EmpresaMap;
import org.parkcontrol.apiparkcontrol.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies-managment")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private EmpresaMap mapper;

    @PostMapping("/create")
    public EmpresaResponse create(@RequestBody RegisterEmpresa registerEmpresa) {
        return mapper.map(empresaService.create(registerEmpresa));
    }

    @PutMapping("/update/{id}")
    public EmpresaResponse update(@RequestBody RegisterEmpresa registerEmpresa, @PathVariable Long id) {
        return mapper.map(empresaService.update(registerEmpresa, id));
    }

    @GetMapping("/all")
    public List<EmpresaResponse> getAll() {
        return empresaService.getAll().stream().map(mapper::map).toList();
    }

    @GetMapping("/get-by-user-company/{idUser}")
    public List<EmpresaResponse> getCompaniesByUser(@PathVariable Long idUser) {
        return empresaService.getComapniesByUser(idUser).stream().map(mapper::map).toList();
    }


}
