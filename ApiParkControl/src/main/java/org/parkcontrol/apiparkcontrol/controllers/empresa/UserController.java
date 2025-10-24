package org.parkcontrol.apiparkcontrol.controllers.empresa;

import org.parkcontrol.apiparkcontrol.dto.autenticacion.RegisterUserDTO;
import org.parkcontrol.apiparkcontrol.dto.empresa.UsuarioPersonaRolResponse;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.mapper.UsuarioPersonaRolMap;
import org.parkcontrol.apiparkcontrol.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user-managment")
public class UserController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioPersonaRolMap userMapper;

    @PostMapping("/create-user-company")
    public UsuarioPersonaRolResponse createUserTypeCompany(@RequestBody RegisterUserDTO userDTO) {
        return userMapper.map(usuarioService.registerUser(userDTO));
    }

    @GetMapping("/get-users-by-rol/{nameRol}")
    public List<UsuarioPersonaRolResponse> getUsersByRol(@PathVariable String nameRol) {
        return usuarioService.getUsersByRol(nameRol).stream().map(userMapper::map).toList();
    }
}
