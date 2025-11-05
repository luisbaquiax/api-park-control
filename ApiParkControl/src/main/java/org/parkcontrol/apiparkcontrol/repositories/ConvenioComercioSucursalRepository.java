package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.ConvenioComercioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConvenioComercioSucursalRepository extends JpaRepository<ConvenioComercioSucursal, Long> {
    List<ConvenioComercioSucursal> findBySucursal_IdSucursalAndEstado(Long idUsuarioSucursal, ConvenioComercioSucursal.Estado estado);

    boolean existsByComercioAfiliado_IdAndSucursal_IdSucursalAndEstado(Long id, Long idSucursal, ConvenioComercioSucursal.Estado estado);

    List<ConvenioComercioSucursal> findBySucursal_Empresa_IdEmpresa(Long idEmpresa);

    List<ConvenioComercioSucursal> findBySucursal_Empresa_IdEmpresaAndEstado(Long sucursalEmpresaIdEmpresa, ConvenioComercioSucursal.Estado estado);

    List<ConvenioComercioSucursal> findByComercioAfiliado_IdAndEstado(Long idComercioAfiliado, ConvenioComercioSucursal.Estado estado);
}
