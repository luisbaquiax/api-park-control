package org.parkcontrol.apiparkcontrol.services.empresa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.parkcontrol.apiparkcontrol.dto.empresa.RegisterEmpresa;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.repositories.EmpresaRepository;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.services.EmpresaService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmpresaService empresaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmpresa_Success() {
        RegisterEmpresa register = new RegisterEmpresa();
        register.setIdUsuario(1L);
        register.setNit("123456789");
        register.setNombreComercial("Comercial S.A.");
        register.setRazonSocial("Comercial Sociedad Anónima");
        register.setDireccionFiscal("Calle 1");
        register.setTelefonoPrincipal("5555-5555");
        register.setCorreoPrincipal("correo@empresa.com");
        register.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(empresaRepository.findByNit("123456789")).thenReturn(null);

        Empresa savedEmpresa = new Empresa();
        savedEmpresa.setNit("123456789");
        when(empresaRepository.save(any(Empresa.class))).thenReturn(savedEmpresa);

        Empresa result = empresaService.create(register);

        assertEquals("123456789", result.getNit());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(empresaRepository, times(1)).findByNit("123456789");
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void testCreateEmpresa_UserNotFound() {
        RegisterEmpresa register = new RegisterEmpresa();
        register.setIdUsuario(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> empresaService.create(register));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void testCreateEmpresa_NitExists() {
        RegisterEmpresa register = new RegisterEmpresa();
        register.setIdUsuario(1L);
        register.setNit("123456789");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(empresaRepository.findByNit("123456789")).thenReturn(new Empresa());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> empresaService.create(register));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya esta registrado"));
    }

    @Test
    void testUpdateEmpresa_Success() {
        Long idEmpresa = 10L;
        RegisterEmpresa register = new RegisterEmpresa();
        register.setNombreComercial("Nueva Comercial");
        register.setRazonSocial("Nueva Razón");
        register.setDireccionFiscal("Nueva calle");
        register.setTelefonoPrincipal("1111-1111");
        register.setCorreoPrincipal("nuevo@correo.com");
        register.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        Empresa existing = new Empresa();
        existing.setIdEmpresa(idEmpresa);

        when(empresaRepository.findById(idEmpresa)).thenReturn(Optional.of(existing));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(existing);

        Empresa result = empresaService.update(register, idEmpresa);

        assertEquals(idEmpresa, result.getIdEmpresa());
        verify(empresaRepository, times(1)).findById(idEmpresa);
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void testUpdateEmpresa_NotFound() {
        Long idEmpresa = 99L;
        RegisterEmpresa register = new RegisterEmpresa();

        when(empresaRepository.findById(idEmpresa)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> empresaService.update(register, idEmpresa));
        assertEquals(404, exception.getStatus());
        assertEquals("Empresa no encontrada", exception.getMessage());
    }

    @Test
    void testGetAll() {
        Empresa e1 = new Empresa();
        Empresa e2 = new Empresa();

        when(empresaRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Empresa> result = empresaService.getAll();
        assertEquals(2, result.size());
        verify(empresaRepository, times(1)).findAll();
    }

    @Test
    void testGetCompaniesByUser() {
        Long idUser = 1L;
        Empresa e1 = new Empresa();
        Empresa e2 = new Empresa();

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(idUser)).thenReturn(List.of(e1, e2));

        List<Empresa> result = empresaService.getComapniesByUser(idUser);
        assertEquals(2, result.size());
        verify(empresaRepository, times(1)).findByUsuarioEmpresa_IdUsuario(idUser);
    }
}

