package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
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
import java.util.List;

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

        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuarioResponsable).stream().findFirst()
                .orElseThrow(() -> new ErrorApi(403, "El usuario no tiene una empresa asociada"));

        if(!empresa.getUsuarioEmpresa().getIdUsuario().equals(idUsuarioResponsable)){
            throw new ErrorApi(403, "El usuario no tiene permisos para crear una tarifa en esta empresa");
        }

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
        tarifaBase.setPrecioPorHora(tarifaBaseResponse.getPrecioPorHora());
        tarifaBase.setFechaVigenciaInicio(tarifaBaseResponse.getFechaVigenciaInicio());
        tarifaBase.setFechaVigenciaFin(tarifaBaseResponse.getFechaVigenciaFin());

        if(tarifaBaseResponse.getFechaVigenciaInicio().equals(LocalDate.now())){
            tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        }else if(tarifaBaseResponse.getFechaVigenciaInicio().isAfter(LocalDate.now())){
            tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);
        }

        BitacoraTarifaBase bitacoraTarifaBase = new BitacoraTarifaBase();
        bitacoraTarifaBase.setTarifaBase(tarifaBase);
        Usuario usuario = usuarioRepository.findById(idUsuarioResponsable).orElseThrow(()-> new ErrorApi(404, "Usuario no encontrado"));
        bitacoraTarifaBase.setUsuarioResponsable(usuario);
        bitacoraTarifaBase.setAccion(BitacoraTarifaBase.Accion.CREACION);
        bitacoraTarifaBase.setPrecioAnterior(BigDecimal.ZERO);
        bitacoraTarifaBase.setPrecioNuevo(tarifaBase.getPrecioPorHora());
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

        if(!tarifaBaseResponse.getPrecioPorHora().equals(tarifaBase.getPrecioPorHora())){
            bitacora.setAccion(BitacoraTarifaBase.Accion.ACTUALIZACION);
            bitacora.setPrecioAnterior(tarifaBase.getPrecioPorHora());
            bitacora.setPrecioNuevo(tarifaBaseResponse.getPrecioPorHora());
            bitacora.setObservaciones(String.format("Cambio de precio de la tarifa #%s de Q.%s a Q.%s.",
                    tarifaBase.getIdTarifaBase().toString(),
                    tarifaBase.getPrecioPorHora().toString(),
                    tarifaBaseResponse.getPrecioPorHora().toString()
            ));

            tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.HISTORICO);

            TarifaBase nuevaTarifa = new TarifaBase();
            nuevaTarifa.setEmpresa(tarifaBase.getEmpresa());
            nuevaTarifa.setPrecioPorHora(tarifaBaseResponse.getPrecioPorHora());
            nuevaTarifa.setFechaVigenciaInicio(tarifaBaseResponse.getFechaVigenciaInicio());
            nuevaTarifa.setFechaVigenciaFin(tarifaBaseResponse.getFechaVigenciaFin());
            nuevaTarifa.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
            // Guardamos la nueva tarifa
            tarifaBaseRepository.save(nuevaTarifa);
            // Guardamos la bitacora de la actualizacion
            bitacoraTarifaBaseRepository.save(bitacora);
        }

        return tarifaBaseRepository.save(tarifaBase);
    }

    @Transactional
    public MessageSuccess activarTarifaBase(Long idTarifaBase, Long idUsuarioResponsable) {
        TarifaBase tarifaBase = tarifaBaseRepository.findById(idTarifaBase).orElseThrow(()-> new ErrorApi(404, "Tarifa no encontrada"));

        if(!tarifaBase.getFechaVigenciaInicio().equals(LocalDate.now())){
            throw new ErrorApi(409, "No se puede activar la tarifa antes de su fecha de inicio de vigencia.");
        }

        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        BitacoraTarifaBase bitacora = new BitacoraTarifaBase();
        bitacora.setTarifaBase(tarifaBase);
        bitacora.setUsuarioResponsable(usuarioRepository.findById(idUsuarioResponsable).orElseThrow(()-> new ErrorApi(404, "Usuario no encontrado")));
        bitacora.setAccion(BitacoraTarifaBase.Accion.ACTIVACION);
        bitacora.setPrecioAnterior(BigDecimal.ZERO);
        bitacora.setPrecioNuevo(tarifaBase.getPrecioPorHora());
        bitacora.setObservaciones(String.format("Activación de la tarifa #%s.", tarifaBase.getIdTarifaBase().toString()));
        bitacoraTarifaBaseRepository.save(bitacora);
        tarifaBaseRepository.save(tarifaBase);
        return new MessageSuccess(200, "Tarifa activada correctamente");
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
        bitacora.setObservaciones(String.format("Desactivación de la tarifa #%s.", tarifaBase.getIdTarifaBase().toString()));
        bitacoraTarifaBaseRepository.save(bitacora);
        return tarifaBaseRepository.save(tarifaBase);
    }

    /**
     * Para obtener la tarifa base de una empresa por su estado, principalmente para obtener la vigente
     * @param estado el estado de la tarifa base
     * @param idUsuario el identificador del usuario que realiza la consulta encargado de la empresa
     * @return TarifaBase
     */
    public TarifaBase findTarifaBaseByEmpresaIdByEstado(TarifaBase.EstadoTarifaBase estado, Long idUsuario) {
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario).stream().findFirst()
                .orElseThrow(() -> new ErrorApi(403, "El usuario no tiene una empresa asociada"));
        return tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(estado, empresa.getIdEmpresa());
    }

    public List<TarifaBase> findAllByEmpresa(Long idUsuario){
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario).stream().findFirst()
                .orElseThrow(() -> new ErrorApi(403, "El usuario no tiene una empresa asociada"));
        return tarifaBaseRepository.getTarifaBaseByEmpresa_IdEmpresa(empresa.getIdEmpresa());
    }

}
