package org.parkcontrol.apiparkcontrol.controllers.empresa;

import org.parkcontrol.apiparkcontrol.dtoempresa.UsuarioPersonaRolResponse;
import org.parkcontrol.apiparkcontrol.mapper.UsuarioPersonaRolMap;
import org.parkcontrol.apiparkcontrol.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user-managment")
public class UserController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioPersonaRolMap userMapper;

    @GetMapping("/get-users-by-rol/{nameRol}")
    public List<UsuarioPersonaRolResponse> getUsersByRol(@PathVariable String nameRol) {
        return usuarioService.getUsersByRol(nameRol).stream().map(userMapper::map).toList();
    }
}
