package org.parkcontrol.apiparkcontrol.services;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dtoempresa.RegisterEmpresa;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.repositories.EmpresaRepository;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Empresa create(RegisterEmpresa registerEmpresa){
        Usuario usuarioOptional = usuarioRepository.findById(registerEmpresa.getIdUsuario()).orElseThrow( ()-> new ErrorApi(404,"Usuario no encontrado"));

        Empresa empresa = empresaRepository.findByNit(registerEmpresa.getNit());

        if(empresa != null){
            throw new ErrorApi(400,String.format("El nit %s ya esta registrado, en consecuencia no se puede crear la empresa",registerEmpresa.getNit()));
        }

        empresa = new Empresa();
        empresa.setIdEmpresa(null);
        empresa.setUsuarioEmpresa(usuarioOptional);
        empresa.setNombreComercial(registerEmpresa.getNombreComercial());
        empresa.setRazonSocial(registerEmpresa.getRazonSocial());
        empresa.setNit(registerEmpresa.getNit());
        empresa.setDireccionFiscal(registerEmpresa.getDireccionFiscal());
        empresa.setTelefonoPrincipal(registerEmpresa.getTelefonoPrincipal());
        empresa.setCorreoPrincipal(registerEmpresa.getCorreoPrincipal());
        empresa.setEstado(registerEmpresa.getEstado());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa update(RegisterEmpresa registerEmpresa, Long idEmpresa){
        Empresa empresaOptional = empresaRepository.findById(idEmpresa).orElseThrow(()-> new ErrorApi(404,"Empresa no encontrada"));

        empresaOptional.setNombreComercial(registerEmpresa.getNombreComercial());
        empresaOptional.setRazonSocial(registerEmpresa.getRazonSocial());
        //empresaOptional.setNit(registerEmpresa.getNit());
        empresaOptional.setDireccionFiscal(registerEmpresa.getDireccionFiscal());
        empresaOptional.setTelefonoPrincipal(registerEmpresa.getTelefonoPrincipal());
        empresaOptional.setCorreoPrincipal(registerEmpresa.getCorreoPrincipal());
        empresaOptional.setEstado(registerEmpresa.getEstado());
        return empresaRepository.save(empresaOptional);
    }

    /**
     * Get of all empresas
     * @return List of companies
     */
    public List<Empresa> getAll(){
        return empresaRepository.findAll();
    }

    /**
     * Get companies by user
     * @param idUser id of user
     * @return list of companies
     */
    public List<Empresa> getComapniesByUser(Long idUser){
        return empresaRepository.findByUsuarioEmpresa_IdUsuario(idUser);
    }

}
