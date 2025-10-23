package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findByEmpresaIdEmpresa(Long idEmpresa);

    Sucursal findByUsuarioSucursal_IdUsuario(Long idUsuarioSucursal);


}
