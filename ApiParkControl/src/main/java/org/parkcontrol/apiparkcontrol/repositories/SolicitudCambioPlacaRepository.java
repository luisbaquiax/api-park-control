package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.SolicitudCambioPlaca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudCambioPlacaRepository extends JpaRepository<SolicitudCambioPlaca, Long> {
    List<SolicitudCambioPlaca> findBySuscripcion_IdAndEstado(Long id, SolicitudCambioPlaca.EstadoSolicitud estadoSolicitud);

    List<SolicitudCambioPlaca> findBySuscripcion_Usuario_IdUsuarioAndEstado(Long idCliente, SolicitudCambioPlaca.EstadoSolicitud estadoSolicitud);

    List<SolicitudCambioPlaca> findBySuscripcion_Usuario_IdUsuario(Long idCliente);

    List<SolicitudCambioPlaca> findBySuscripcion_Empresa_IdEmpresa(Long idEmpresa);
}
