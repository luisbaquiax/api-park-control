package org.parkcontrol.apiparkcontrol.services.comercio_afliado;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.*;

@Service
public class GestionComercioAfiliadoService {
    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;

    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;

    @Autowired
    SucursalRepository sucursalRepository;

    @Autowired
    TarifaSucursalRepository tarifaSucursalRepository;

    @Autowired
    UsuarioRepository usuarioRepository;
    //Obtemos todas las empresas

    public List<ComercioAfiliadoDTO> getComercioAfiliado() {
        List<ComercioAfiliado> comercios = comercioAfiliadoRepository.findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO);
        List<ComercioAfiliadoDTO> comerciosDTO = new ArrayList<>();
        for (ComercioAfiliado comercioAfiliado : comercios) {
            ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO(
                    comercioAfiliado.getId(),
                    comercioAfiliado.getNombreComercial(),
                    comercioAfiliado.getRazonSocial(),
                    comercioAfiliado.getNit(),
                    comercioAfiliado.getTipoComercio(),
                    comercioAfiliado.getTelefono(),
                    comercioAfiliado.getCorreoContacto(),
                    comercioAfiliado.getEstado().name(),
                    comercioAfiliado.getFechaRegistro().toString()
            );
            comerciosDTO.add(dto);

        }
        return comerciosDTO;

    }

    //Obtenemos todos los detalles de los convenios de una sucursal
    public List<DetalleEmpresaConvenioDTO> getDetalleEmpresaConvenio(Long idUsuarioSucursal) {
        List<DetalleEmpresaConvenioDTO> detalleList = new ArrayList<>();
        //Obtenemos la sucursal por el idUsuarioSucursal
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        if (sucursal == null) {
            throw new RuntimeException("Sucursal no encontrada para el idUsuarioSucursal: " + idUsuarioSucursal);
        }
        List<ConvenioComercioSucursal> convenios = convenioComercioSucursalRepository.findBySucursal_IdSucursalAndEstado(sucursal.getIdSucursal(), ConvenioComercioSucursal.Estado.ACTIVO);

        for (ConvenioComercioSucursal convenio : convenios) {
            ComercioAfiliado comercio = convenio.getComercioAfiliado();
            ComercioAfiliadoDTO comercioDTO = new ComercioAfiliadoDTO(
                    comercio.getId(),
                    comercio.getNombreComercial(),
                    comercio.getRazonSocial(),
                    comercio.getNit(),
                    comercio.getTipoComercio(),
                    comercio.getTelefono(),
                    comercio.getCorreoContacto(),
                    comercio.getEstado().name(),
                    comercio.getFechaRegistro().toString()
            );

            ConvenioComercioSucursalDTO convenioDTO = new ConvenioComercioSucursalDTO();
            convenioDTO.setIdConvenio(convenio.getId());
            convenioDTO.setHorasGratisMaximo(String.valueOf(convenio.getHorasGratisMaximo()));
            convenioDTO.setPeriodoCorte(convenio.getPeriodoCorte().name());
            convenioDTO.setTarifaPorHora(String.valueOf(convenio.getTarifaPorHora()));
            convenioDTO.setFechaInicioConvenio(convenio.getFechaInicioConvenio().toString());
            convenioDTO.setFechaFinConvenio(convenio.getFechaFinConvenio().toString());
            convenioDTO.setEstado(convenio.getEstado().name());


            DetalleEmpresaConvenioDTO detalleDTO = new DetalleEmpresaConvenioDTO(comercioDTO, convenioDTO);
            detalleList.add(detalleDTO);
        }
        return detalleList;

    }

    //Creamos un nuevo comercio afiliado
    @Transactional
    public String crearComercioAfiliado(ComercioAfiliadoDTO comercioAfiliadoDTO) {

        //Validamos que no exista un comercio con el mismo NIT
        if (comercioAfiliadoRepository.existsByNit(comercioAfiliadoDTO.getNit())) {
            throw new RuntimeException("Ya existe un comercio afiliado con el NIT: " + comercioAfiliadoDTO.getNit());
        }

        ComercioAfiliado comercioAfiliado = new ComercioAfiliado();
        comercioAfiliado.setNombreComercial(comercioAfiliadoDTO.getNombreComercial());
        comercioAfiliado.setRazonSocial(comercioAfiliadoDTO.getRazonSocial());
        comercioAfiliado.setNit(comercioAfiliadoDTO.getNit());
        comercioAfiliado.setTipoComercio(comercioAfiliadoDTO.getTipoComercio());
        comercioAfiliado.setTelefono(comercioAfiliadoDTO.getTelefono());
        comercioAfiliado.setCorreoContacto(comercioAfiliadoDTO.getCorreoContacto());
        comercioAfiliado.setEstado(ComercioAfiliado.Estado.ACTIVO);
        comercioAfiliado.setFechaRegistro(LocalDateTime.now());
        ComercioAfiliado nuevo = comercioAfiliadoRepository.save(comercioAfiliado);

        return "Comercio afiliado creado exitosamente con ID: " + nuevo.getId();

    }

    //Actualizamos un comercio afiliado
    @Transactional
    public String actualizarComercioAfiliado(ComercioAfiliadoDTO comercioAfiliadoDTO) {
        ComercioAfiliado comercioAfiliado = comercioAfiliadoRepository.findById(comercioAfiliadoDTO.getIdComercio())
                .orElseThrow(() -> new RuntimeException("Comercio afiliado no encontrado con ID: " + comercioAfiliadoDTO.getIdComercio()));

        comercioAfiliado.setNombreComercial(comercioAfiliadoDTO.getNombreComercial());
        comercioAfiliado.setRazonSocial(comercioAfiliadoDTO.getRazonSocial());
        comercioAfiliado.setNit(comercioAfiliadoDTO.getNit());
        comercioAfiliado.setTipoComercio(comercioAfiliadoDTO.getTipoComercio());
        comercioAfiliado.setTelefono(comercioAfiliadoDTO.getTelefono());
        comercioAfiliado.setCorreoContacto(comercioAfiliadoDTO.getCorreoContacto());

        comercioAfiliadoRepository.save(comercioAfiliado);

        return "Comercio afiliado actualizado exitosamente con ID: " + comercioAfiliado.getId();
    }

    // Eliminamos (desactivamos) un comercio afiliado
    @Transactional
    public String eliminarComercioAfiliado(Long idComercio) {
        ComercioAfiliado comercioAfiliado = comercioAfiliadoRepository.findById(idComercio)
                .orElseThrow(() -> new RuntimeException("Comercio afiliado no encontrado con ID: " + idComercio));
        comercioAfiliado.setEstado(ComercioAfiliado.Estado.INACTIVO);
        comercioAfiliadoRepository.save(comercioAfiliado);
        return "Comercio afiliado desactivado exitosamente con ID: " + comercioAfiliado.getId();
    }

    //Creamos un nuevo convenio entre comercio afiliado y sucursal
    @Transactional
    public String crearConvenioComercioSucursal(ConvenioComercioSucursalDTO convenioDTO) {
        ComercioAfiliado comercioAfiliado = comercioAfiliadoRepository.findById(convenioDTO.getIdComercio())
                .orElseThrow(() -> new RuntimeException("Comercio afiliado no encontrado con ID: " + convenioDTO.getIdComercio()));

        //Obtenemos el usuario sucursal
        Usuario usuario = usuarioRepository.findById(convenioDTO.getCreadoPor())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + convenioDTO.getCreadoPor()));


        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(usuario.getIdUsuario());
        if (sucursal == null) {
            throw new RuntimeException("Sucursal no encontrada para el usuario con ID: " + usuario.getIdUsuario());
        }


        //Validamos que no exista un convenio activo entre el comercio y la sucursal
        if (convenioComercioSucursalRepository.existsByComercioAfiliado_IdAndSucursal_IdSucursalAndEstado(
                comercioAfiliado.getId(),
                sucursal.getIdSucursal(),
                ConvenioComercioSucursal.Estado.ACTIVO)) {
            throw new RuntimeException("Ya existe un convenio activo entre el comercio afiliado y la sucursal.");
        }
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(comercioAfiliado);
        convenio.setSucursal(sucursal);
        convenio.setHorasGratisMaximo(new BigDecimal(convenioDTO.getHorasGratisMaximo()));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.valueOf(convenioDTO.getPeriodoCorte()));
        //Para la tarifa por hora la tomamos de la tarifa de la sucursal
        TarifaSucursal tarifaSucursal = tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(sucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.VIGENTE).getFirst();
        if (tarifaSucursal == null) {
            throw new RuntimeException("No se encontró una tarifa para la sucursal con ID: " + sucursal.getIdSucursal());
        }
        convenio.setTarifaPorHora(tarifaSucursal.getPrecioPorHora());
        convenio.setFechaInicioConvenio(LocalDate.parse(convenioDTO.getFechaInicioConvenio()).atStartOfDay());
        convenio.setFechaFinConvenio(LocalDate.parse(convenioDTO.getFechaFinConvenio()).atStartOfDay());
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(usuario);
        convenio.setFechaCreacion(LocalDateTime.now());
        ConvenioComercioSucursal nuevoC = convenioComercioSucursalRepository.save(convenio);

        return "Convenio creado exitosamente con ID: " + nuevoC.getId();
    }
    /*
    Ejemplo de JSON para crear un convenio
    {
        "idComercio": 1,
        "horasGratisMaximo": "10",
        "periodoCorte": "MENSUAL",
        "fechaInicioConvenio": "2024-07-01",
        "fechaFinConvenio": "2025-07-01",
        "creadoPor": 2
    }
     */

    // Actualizamos un convenio entre comercio afiliado y sucursal
    @Transactional
    public String actualizarConvenioComercioSucursal(ConvenioComercioSucursalDTO convenioDTO) {
        ConvenioComercioSucursal convenio = convenioComercioSucursalRepository.findById(convenioDTO.getIdConvenio())
                .orElseThrow(() -> new RuntimeException("Convenio no encontrado con ID: " + convenioDTO.getIdConvenio()));

        convenio.setHorasGratisMaximo(new BigDecimal(convenioDTO.getHorasGratisMaximo()));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.valueOf(convenioDTO.getPeriodoCorte()));
        convenio.setFechaInicioConvenio(LocalDate.parse(convenioDTO.getFechaInicioConvenio()).atStartOfDay());
        convenio.setFechaFinConvenio(LocalDate.parse(convenioDTO.getFechaFinConvenio()).atStartOfDay());
        convenioComercioSucursalRepository.save(convenio);

        return "Convenio actualizado exitosamente con ID: " + convenio.getId();

    }
    /*
    Ejemplo de JSON para actualizar un convenio
    {
        "idConvenio": 1,
        "horasGratisMaximo": "15",
        "periodoCorte": "MENSUAL",
        "fechaInicioConvenio": "2024-07-01",
        "fechaFinConvenio": "2025-07-01"
    }
     */

    //Cambiar estado de un convenio
    @Transactional
    public String cambiarEstadoConvenio(ConvenioComercioSucursalDTO convenioDTO) {
        Long idConvenio = convenioDTO.getIdConvenio();
        String nuevoEstado = convenioDTO.getEstado();
        ConvenioComercioSucursal convenio = convenioComercioSucursalRepository.findById(idConvenio)
                .orElseThrow(() -> new RuntimeException("Convenio no encontrado con ID: " + idConvenio));

        convenio.setEstado(ConvenioComercioSucursal.Estado.valueOf(nuevoEstado));
        convenioComercioSucursalRepository.save(convenio);

        return "Estado del convenio actualizado exitosamente a: " + nuevoEstado;
    }

}
