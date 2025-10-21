package org.parkcontrol.apiparkcontrol.services.empresa_sucursal;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmpresaSucursalService {
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

    private Encriptation passwordEncoder = new Encriptation();

   // Métodos para gestionar empresas, sucursales, usuarios y tarifas

    // Crear usuario de sucursal
    @Transactional
    public Usuario crearUsuarioSucursal(UsuarioSucursalDTO userDTO) {
        Persona persona = new Persona();
        persona.setNombre(userDTO.getNombre());
        persona.setApellido(userDTO.getApellido());
        persona.setFechaNacimiento(LocalDate.parse(userDTO.getFechaNacimiento()));
        persona.setDpi(userDTO.getDpi());
        persona.setCorreo(userDTO.getCorreo());
        persona.setTelefono(userDTO.getTelefono());
        persona.setDireccionCompleta(userDTO.getDireccionCompleta());
        persona.setCiudad(userDTO.getCiudad());
        persona.setPais(userDTO.getPais());
        persona.setCodigoPostal(userDTO.getCodigoPostal());
        persona.setEstado(Persona.Estado.ACTIVO);
        personaRepository.save(persona);

        // Crear y guardar el usuario
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        Rol rol = rolRepository.findByNombreRol("SUCURSAL");
        usuario.setRol(rol);
        usuario.setNombreUsuario(userDTO.getNombreUsuario());
        usuario.setContraseniaHash(passwordEncoder.encrypt(userDTO.getContraseniaHash()));
        usuario.setDobleFactorHabilitado(userDTO.isDobleFactorHabilitado()); // Usar el valor del DTO
        usuario.setEstado(Usuario.EstadoUsuario.valueOf(userDTO.getEstado()));
        usuario.setDebeCambiarContrasenia(true);
        usuario.setEsPrimeraVez(true);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);

        return usuarioRepository.save(usuario);
    }

    //Obtener usuarios de sucursal por empresa
    public List<ObtenerSucursalesEmpresaDTO> obtenerUsuariosSucursalPorEmpresa(Long idEmpresa) {
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(idEmpresa);
        List<ObtenerSucursalesEmpresaDTO> resultado = new ArrayList<>();

        for (Sucursal sucursal : sucursales) {
            Usuario usuarioSucursal = sucursal.getUsuarioSucursal();
            Persona persona = usuarioSucursal.getPersona();

            UsuarioSucursalDTO usuarioDTO = new UsuarioSucursalDTO();
            usuarioDTO.setNombre(persona.getNombre());
            usuarioDTO.setApellido(persona.getApellido());
            usuarioDTO.setFechaNacimiento(persona.getFechaNacimiento().toString());
            usuarioDTO.setDpi(persona.getDpi());
            usuarioDTO.setCorreo(persona.getCorreo());
            usuarioDTO.setTelefono(persona.getTelefono());
            usuarioDTO.setDireccionCompleta(persona.getDireccionCompleta());
            usuarioDTO.setCiudad(persona.getCiudad());
            usuarioDTO.setPais(persona.getPais());
            usuarioDTO.setCodigoPostal(persona.getCodigoPostal());
            usuarioDTO.setNombreUsuario(usuarioSucursal.getNombreUsuario());
            usuarioDTO.setDobleFactorHabilitado(usuarioSucursal.isDobleFactorHabilitado());
            usuarioDTO.setEstado(usuarioSucursal.getEstado().name());

            ObtenerSucursalesEmpresaDTO.SucursalDTO sucursalDTO = new ObtenerSucursalesEmpresaDTO.SucursalDTO();
            sucursalDTO.setIdSucursal(sucursal.getIdSucursal());
            sucursalDTO.setNombre(sucursal.getNombre());
            sucursalDTO.setDireccionCompleta(sucursal.getDireccionCompleta());
            sucursalDTO.setCiudad(sucursal.getCiudad());
            sucursalDTO.setDepartamento(sucursal.getDepartamento());
            sucursalDTO.setHoraApertura(sucursal.getHoraApertura().toString());
            sucursalDTO.setHoraCierre(sucursal.getHoraCierre().toString());
            sucursalDTO.setCapacidad2Ruedas(sucursal.getCapacidad2Ruedas());
            sucursalDTO.setCapacidad4Ruedas(sucursal.getCapacidad4Ruedas());
            sucursalDTO.setEstado(sucursal.getEstado().name());
            sucursalDTO.setUsuario(usuarioDTO);

            ObtenerSucursalesEmpresaDTO dto = new ObtenerSucursalesEmpresaDTO();
            dto.setSucursalDTO(sucursalDTO);

            resultado.add(dto);
        }

        return resultado;
    }

    //Creamos la sucursal
    @Transactional
    public String crearNuevaSucursal(CreateSucursalDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        UsuarioSucursalDTO usuarioDTO = new UsuarioSucursalDTO();
        usuarioDTO.setNombre(dto.getNombre());
        usuarioDTO.setApellido(dto.getApellido());
        usuarioDTO.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioDTO.setDpi(dto.getDpi());
        usuarioDTO.setCorreo(dto.getCorreo());
        usuarioDTO.setTelefono(dto.getTelefono());
        usuarioDTO.setDireccionCompleta(dto.getDireccionCompleta());
        usuarioDTO.setCiudad(dto.getCiudad());
        usuarioDTO.setPais(dto.getPais());
        usuarioDTO.setCodigoPostal(dto.getCodigoPostal());
        usuarioDTO.setNombreUsuario(dto.getNombreUsuario());
        usuarioDTO.setContraseniaHash(dto.getContraseniaHash());
        usuarioDTO.setDobleFactorHabilitado(dto.isDobleFactorHabilitado());
        usuarioDTO.setEstado("ACTIVO");

        Usuario usuarioSucursal = crearUsuarioSucursal(usuarioDTO);

        Sucursal sucursal = new Sucursal();
        sucursal.setEmpresa(empresa);
        sucursal.setUsuarioSucursal(usuarioSucursal);
        sucursal.setNombre(dto.getNombreSucursal());
        sucursal.setDireccionCompleta(dto.getDireccionCompletaSucursal());
        sucursal.setCiudad(dto.getCiudadSucursal());
        sucursal.setDepartamento(dto.getDepartamentoSucursal());
        sucursal.setHoraApertura(java.time.LocalTime.parse(dto.getHoraApertura()));
        sucursal.setHoraCierre(java.time.LocalTime.parse(dto.getHoraCierre()));
        sucursal.setCapacidad2Ruedas(dto.getCapacidad2Ruedas());
        sucursal.setCapacidad4Ruedas(dto.getCapacidad4Ruedas());
        sucursal.setLatitud(BigDecimal.valueOf(dto.getLatitud()));
        sucursal.setLongitud(BigDecimal.valueOf(dto.getLongitud()));
        sucursal.setTelefonoContacto(dto.getTelefonoContactoSucursal());
        sucursal.setCorreoContacto(dto.getCorreoContactoSucursal());
        sucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);

        sucursalRepository.save(sucursal);

        return "Sucursal creada exitosamente";
    }

}
