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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class GestionTarifaSucursalService {
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
    @Autowired
    private TarifaSucursalRepository tarifaRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private BitacoraTarifaSucursalRepository bitacoraTarifaSucursal;

    @Transactional
    public String crearNuevaTarifaSucursal(NuevaTarifaSucursalDTO nuevaTarifaSucursalDTO) throws Exception {
        // Lógica para crear una nueva tarifa de sucursal
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(nuevaTarifaSucursalDTO.getIdUsuarioSucursal());
        if (sucursal == null) {
            throw new Exception("Sucursal no encontrada para el usuario proporcionado.");
        }

        //Obtenemos las tarifas vigentes de la sucursal
        List<TarifaSucursal> tarifasVigentes = tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(
                sucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        // Marcamos las tarifas vigentes como historicas
        for (TarifaSucursal tarifa : tarifasVigentes) {
            tarifa.setEstado(TarifaSucursal.EstadoTarifaSucursal.HISTORICO);
            tarifaSucursalRepository.save(tarifa);
        }

        TarifaSucursal nuevaTarifa = new TarifaSucursal();
        nuevaTarifa.setSucursal(sucursal);
        if(nuevaTarifaSucursalDTO.isEsTarifaBase()){
            TarifaBase tarifaBase = tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(sucursal.getEmpresa().getIdEmpresa(), TarifaBase.EstadoTarifaBase.VIGENTE);
            if(tarifaBase == null){
                throw new Exception("No existe una tarifa base para la empresa de la sucursal.");
            }
            nuevaTarifa.setPrecioPorHora(tarifaBase.getPrecioPorHora());
        } else {
            nuevaTarifa.setPrecioPorHora(new BigDecimal(nuevaTarifaSucursalDTO.getPrecioPorHora()));
        }
        nuevaTarifa.setMoneda(nuevaTarifaSucursalDTO.getMoneda());
        nuevaTarifa.setFechaVigenciaInicio(LocalDate.parse(nuevaTarifaSucursalDTO.getFechaVigenciaInicio()).atStartOfDay());
        if (nuevaTarifaSucursalDTO.getFechaVigenciaFin() != null && !nuevaTarifaSucursalDTO.getFechaVigenciaFin().isEmpty()) {
            nuevaTarifa.setFechaVigenciaFin(LocalDate.parse(nuevaTarifaSucursalDTO.getFechaVigenciaFin()).atStartOfDay());
        }
        nuevaTarifa.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);

        TarifaSucursal nuevaT = tarifaSucursalRepository.save(nuevaTarifa);
        BitacoraTarifaSucursal bitacora = new BitacoraTarifaSucursal();
        bitacora.setTarifaSucursal(nuevaT);
        bitacora.setPrecioNuevo(nuevaT.getPrecioPorHora());
        bitacora.setObservaciones("Creación de nueva tarifa de sucursal.");
        bitacora.setAccion(BitacoraTarifaSucursal.Accion.CREACION);
        bitacoraTarifaSucursal.save(bitacora);

        return "Nueva tarifa de sucursal creada exitosamente.";
    }

    //Editar tarifa sucursal
    @Transactional
    public String editarTarifaSucursal(TarifaSucursalDTO tarifaSucursal) throws Exception {
        TarifaSucursal tarifaExistente = tarifaSucursalRepository.findById(tarifaSucursal.getIdTarifaSucursal())
                .orElseThrow(() -> new Exception("Tarifa de sucursal no encontrada."));


        BitacoraTarifaSucursal bitacora = new BitacoraTarifaSucursal();
        bitacora.setTarifaSucursal(tarifaExistente);
        bitacora.setPrecioAnterior(tarifaExistente.getPrecioPorHora());
        bitacora.setPrecioNuevo(BigDecimal.valueOf(tarifaSucursal.getPrecioPorHora()));
        bitacora.setObservaciones("Edición de tarifa de sucursal.");
        bitacora.setAccion(BitacoraTarifaSucursal.Accion.ACTUALIZACION);
        bitacoraTarifaSucursal.save(bitacora);

        tarifaExistente.setPrecioPorHora(BigDecimal.valueOf(tarifaSucursal.getPrecioPorHora()));
        tarifaExistente.setMoneda(tarifaSucursal.getMoneda());
        tarifaExistente.setFechaVigenciaInicio(LocalDate.parse(tarifaSucursal.getFechaVigenciaInicio()).atStartOfDay());
        tarifaExistente.setFechaVigenciaFin(LocalDate.parse(tarifaSucursal.getFechaVigenciaFin()).atStartOfDay());
        tarifaExistente.setEstado(TarifaSucursal.EstadoTarifaSucursal.valueOf(tarifaSucursal.getEstado()));

        tarifaSucursalRepository.save(tarifaExistente);

        return "Tarifa de sucursal editada exitosamente.";
    }

    // Obtener tarifas de sucursal por ID de usuario
    public List<TarifaSucursalDTO> obtenerTarifasPorIdUsuario(Long idUsuarioSucursal) throws Exception {
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        if (sucursal == null) {
            throw new Exception("Sucursal no encontrada para el usuario proporcionado.");
        }
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(sucursal.getIdSucursal());
        List<TarifaSucursalDTO> tarifasDTO = new ArrayList<>();
        for (TarifaSucursal tarifa : tarifas) {
            TarifaSucursalDTO dto = new TarifaSucursalDTO();
            dto.setIdUsuarioSucursal(idUsuarioSucursal);
            dto.setIdTarifaSucursal(tarifa.getIdTarifaSucursal());
            dto.setPrecioPorHora(tarifa.getPrecioPorHora().doubleValue());
            dto.setMoneda(tarifa.getMoneda());
            dto.setFechaVigenciaInicio(tarifa.getFechaVigenciaInicio().toString());
            if (tarifa.getFechaVigenciaFin() != null) {
                dto.setFechaVigenciaFin(tarifa.getFechaVigenciaFin().toString());
            }
            dto.setEstado(tarifa.getEstado().name());
            tarifasDTO.add(dto);
        }
        return tarifasDTO;
    }

}
