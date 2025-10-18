package org.parkcontrol.apiparkcontrol.services.gestion_sucursal;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.*;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GestionSucursalService {
    @Autowired
    private  EmpresaRepository empresaRepository;
    @Autowired
    private  SucursalRepository sucursalRepository;
    @Autowired
    private  UsuarioRepository usuarioRepository;
    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PersonaRepository personaRepository;

    //Obtener la sucursal del usuario de sucursal
    public GetSucursalDTO obtenerSucursalPorUsuario(Long idUsuarioSucursal) {
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        if (sucursal == null) {
            throw new RuntimeException("Sucursal no encontrada para el usuario con ID: " + idUsuarioSucursal);
        }

        GetSucursalDTO.EmpresaSucursalDTO empresaDTO = new GetSucursalDTO.EmpresaSucursalDTO(
                sucursal.getEmpresa().getIdEmpresa(),
                sucursal.getEmpresa().getNombreComercial(),
                sucursal.getEmpresa().getRazonSocial(),
                sucursal.getEmpresa().getNit(),
                sucursal.getEmpresa().getDireccionFiscal(),
                sucursal.getEmpresa().getTelefonoPrincipal(),
                sucursal.getEmpresa().getCorreoPrincipal()
        );


        GetSucursalDTO sucursalDTO = new GetSucursalDTO(
                sucursal.getIdSucursal(),
                sucursal.getNombre(),
                sucursal.getDireccionCompleta(),
                sucursal.getCiudad(),
                sucursal.getDepartamento(),
                sucursal.getHoraApertura().toString(),
                sucursal.getHoraCierre().toString(),
                sucursal.getCapacidad2Ruedas(),
                sucursal.getCapacidad4Ruedas(),
                sucursal.getLatitud().doubleValue(),
                sucursal.getLongitud().doubleValue(),
                sucursal.getTelefonoContacto(),
                sucursal.getCorreoContacto(),
                sucursal.getEstado().name(),
                empresaDTO
        );

        return sucursalDTO;
    }

    //Editar sucursal
    @Transactional
    public String editarSucursal(EditarSucursalDTO editarSucursalDTO) {
        Sucursal sucursal = sucursalRepository.findById(editarSucursalDTO.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + editarSucursalDTO.getIdSucursal()));

        sucursal.setNombre(editarSucursalDTO.getNombreSucursal());
        sucursal.setDireccionCompleta(editarSucursalDTO.getDireccionCompletaSucursal());
        sucursal.setCiudad(editarSucursalDTO.getCiudadSucursal());
        sucursal.setDepartamento(editarSucursalDTO.getDepartamentoSucursal());
        sucursal.setHoraApertura(java.time.LocalTime.parse(editarSucursalDTO.getHoraApertura()));
        sucursal.setHoraCierre(java.time.LocalTime.parse(editarSucursalDTO.getHoraCierre()));
        sucursal.setCapacidad2Ruedas(editarSucursalDTO.getCapacidad2Ruedas());
        sucursal.setCapacidad4Ruedas(editarSucursalDTO.getCapacidad4Ruedas());
        sucursal.setLatitud(BigDecimal.valueOf(editarSucursalDTO.getLatitud()));
        sucursal.setLongitud(BigDecimal.valueOf(editarSucursalDTO.getLongitud()));
        sucursal.setTelefonoContacto(editarSucursalDTO.getTelefonoContactoSucursal());
        sucursal.setCorreoContacto(editarSucursalDTO.getCorreoContactoSucursal());
        sucursal.setEstado(Sucursal.EstadoSucursal.valueOf(editarSucursalDTO.getEstadoSucursal()));

        sucursalRepository.save(sucursal);

        return "Sucursal editada exitosamente.";
    }

}
