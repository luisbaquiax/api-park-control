package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dtoempresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.repositories.BitacoraTarifaBaseRepository;
import org.parkcontrol.apiparkcontrol.repositories.EmpresaRepository;
import org.parkcontrol.apiparkcontrol.repositories.TarifaBaseRepository;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TarifaBaseService {

    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Transactional
    public TarifaBase create(TarifaBaseResponse tarifaBaseResponse, Long idUsuarioResponsable) {
        Empresa empresa = empresaRepository.findById(tarifaBaseResponse.getIdEmpresa()).orElseThrow(()-> new ErrorApi(404, "Empresa no encontrada"));

        boolean existeSolapamiento = tarifaBaseRepository.existeSolapamiento(
                empresa.getIdEmpresa(),
                tarifaBaseResponse.getFechaVigenciaInicio(),
                tarifaBaseResponse.getFechaVigenciaFin());
        if(existeSolapamiento){
            throw new ErrorApi(400, String.format("Ya existe la tarifa de %s %s de la fecha del %s al %s.",
                    tarifaBaseResponse.getMoneda(),
                    tarifaBaseResponse.getPrecioPorHora().toString(),
                    tarifaBaseResponse.getFechaVigenciaInicio().toString(),
                    tarifaBaseResponse.getFechaVigenciaFin().toString()));
        }

        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(empresa);
        tarifaBase.setMoneda(tarifaBaseResponse.getMoneda());
        tarifaBase.setPrecioPorHora(tarifaBaseResponse.getPrecioPorHora());
        tarifaBase.setFechaVigenciaInicio(tarifaBaseResponse.getFechaVigenciaInicio());
        tarifaBase.setFechaVigenciaFin(tarifaBaseResponse.getFechaVigenciaFin());
        tarifaBase.setEstado(tarifaBaseResponse.getEstado());

        BitacoraTarifaBase bitacoraTarifaBase = new BitacoraTarifaBase();
        bitacoraTarifaBase.setTarifaBase(tarifaBase);
        Usuario usuario = usuarioRepository.findById(idUsuarioResponsable).orElseThrow(()-> new ErrorApi(404, "Usuario no encontrado"));
        bitacoraTarifaBase.setUsuarioResponsable(usuario);
        bitacoraTarifaBase.setAccion(BitacoraTarifaBase.Accion.CREACION);
        bitacoraTarifaBaseRepository.save(bitacoraTarifaBase);

        return tarifaBaseRepository.save(tarifaBase);
    }


    @Transactional
    public TarifaBase update(TarifaBaseResponse tarifaBaseResponse, Long idUsuarioResponsable) {
        TarifaBase tarifaBase = tarifaBaseRepository.findById(tarifaBaseResponse.getIdTarifaBase()).orElseThrow(()-> new ErrorApi(404, "Tarifa no encontrada"));

        //creamos una bitacora
        BitacoraTarifaBase bitacora = new BitacoraTarifaBase();
        bitacora.setTarifaBase(tarifaBase);
        Usuario usuario = usuarioRepository.findById(idUsuarioResponsable).orElseThrow(()-> new ErrorApi(404, "Usuario no encontrado"));
        bitacora.setUsuarioResponsable(usuario);

        if(tarifaBase.getFechaVigenciaInicio().equals(tarifaBaseResponse.getFechaVigenciaInicio())
                && tarifaBase.getFechaVigenciaFin().equals(tarifaBaseResponse.getFechaVigenciaFin())) {
            tarifaBase.setPrecioPorHora(tarifaBaseResponse.getPrecioPorHora());
            //guardamos la bitacora
            bitacora.setPrecioAnterior(tarifaBase.getPrecioPorHora());
            bitacora.setPrecioNuevo(tarifaBaseResponse.getPrecioPorHora());
            bitacora.setAccion(BitacoraTarifaBase.Accion.ACTUALIZACION);
        }else{
            if(tarifaBaseRepository.existeSolapamiento(tarifaBaseResponse.getIdEmpresa(), tarifaBaseResponse.getFechaVigenciaInicio(), tarifaBaseResponse.getFechaVigenciaFin())) {
                throw new ErrorApi(400, String.format("Ya existe la tarifa de %s %s de la fecha del %s al %s.",
                        tarifaBase.getMoneda(),
                        tarifaBase.getPrecioPorHora().toString(),
                        tarifaBase.getFechaVigenciaInicio().toString(),
                        tarifaBase.getFechaVigenciaFin().toString()));
            }
        }

        tarifaBase.setFechaVigenciaInicio(tarifaBaseResponse.getFechaVigenciaInicio());
        tarifaBase.setFechaVigenciaFin(tarifaBaseResponse.getFechaVigenciaFin());
        //activar tarifa programado
        if(tarifaBaseResponse.getFechaVigenciaInicio().isAfter(LocalDate.now())){
            tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);
            bitacora.setAccion(BitacoraTarifaBase.Accion.ACTUALIZACION);
        }else{
            tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
            bitacora.setAccion(BitacoraTarifaBase.Accion.ACTIVACION);
        }
        //guardamos la bitacora
        bitacoraTarifaBaseRepository.save(bitacora);

        return tarifaBaseRepository.save(tarifaBase);
    }

    @Transactional
    public TarifaBase desactivarTarifaBase(Long idTarifaBase, Long idUsuarioResponsable) {
        TarifaBase tarifaBase = tarifaBaseRepository.findById(idTarifaBase).orElseThrow(()-> new ErrorApi(404, "Tarifa no encontrada"));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.HISTORICO);
        BitacoraTarifaBase bitacora = new BitacoraTarifaBase();
        bitacora.setTarifaBase(tarifaBase);
        bitacora.setUsuarioResponsable(usuarioRepository.findById(idUsuarioResponsable).orElseThrow(()-> new ErrorApi(404, "Usuario no encontrado")));
        bitacora.setAccion(BitacoraTarifaBase.Accion.DESACTIVACION);
        bitacora.setPrecioAnterior(tarifaBase.getPrecioPorHora());
        bitacora.setPrecioNuevo(BigDecimal.ZERO);
        bitacoraTarifaBaseRepository.save(bitacora);
        return tarifaBaseRepository.save(tarifaBase);
    }

    public TarifaBase findTarifaBaseByEmpresaIdByEstado(TarifaBase.EstadoTarifaBase estado, Long idEmpresa) {
        return tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(estado, idEmpresa);
    }

}
