package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Empresa findByNit(String nit);

    List<Empresa> findByUsuarioEmpresa_IdUsuario(Long usuarioEmpresaIdUsuario);
}
