package org.parkcontrol.apiparkcontrol.services.empresa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TarifaBaseServiceTest {

    @InjectMocks
    private TarifaBaseService tarifaBaseService;

    @Mock
    private TarifaBaseRepository tarifaBaseRepository;
    @Mock
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    private Usuario usuario;
    private Empresa empresa;
    private TarifaBase tarifaBase;
    private TarifaBaseResponse tarifaBaseResponse;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);

        empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        empresa.setUsuarioEmpresa(usuario);

        tarifaBase = new TarifaBase();
        tarifaBase.setIdTarifaBase(1L);
        tarifaBase.setEmpresa(empresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusDays(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);

        tarifaBaseResponse = new TarifaBaseResponse();
        tarifaBaseResponse.setPrecioPorHora(BigDecimal.valueOf(60));
        tarifaBaseResponse.setFechaVigenciaInicio(LocalDate.now());
        tarifaBaseResponse.setFechaVigenciaFin(LocalDate.now().plusDays(2));
    }

    @Test
    void testCreateTarifaBaseSuccess() {
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.existeSolapamiento(anyLong(), any(), any()))
                .thenReturn(false);
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);

        TarifaBase created = tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario());

        assertNotNull(created);
        assertEquals(BigDecimal.valueOf(60), created.getPrecioPorHora());
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, created.getEstado());

        verify(bitacoraTarifaBaseRepository, times(1)).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testCreateTarifaBaseFailsIfUserNoEmpresa() {
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(Collections.emptyList());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(403, exception.getStatus());
    }

    @Test
    void testUpdateTarifaBaseSuccess() {
        tarifaBaseResponse.setIdTarifaBase(tarifaBase.getIdTarifaBase());

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase())).thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bitacoraTarifaBaseRepository.save(any(BitacoraTarifaBase.class))).thenReturn(null);

        TarifaBase updated = tarifaBaseService.update(tarifaBaseResponse, usuario.getIdUsuario());

        assertNotNull(updated);
        assertEquals(TarifaBase.EstadoTarifaBase.HISTORICO, updated.getEstado());

        verify(bitacoraTarifaBaseRepository, times(1)).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testActivarTarifaBaseSuccess() {
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));

        var response = tarifaBaseService.activarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario());

        assertEquals(200, response.getCode());
        assertEquals("Tarifa activada correctamente", response.getMessage());
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, tarifaBase.getEstado());
    }

    @Test
    void testActivarTarifaBaseFailsIfFechaInicioNoHoy() {
        tarifaBase.setFechaVigenciaInicio(LocalDate.now().plusDays(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.activarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario()));
        assertEquals(409, exception.getStatus());
    }


    @Test
    void desactivarTarifaBase_tarifaNoEncontrada_deberiaLanzarErrorApi() {
        when(tarifaBaseRepository.findById(1L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.desactivarTarifaBase(1L, 2L));
        assertEquals(404, exception.getStatus());
        assertEquals("Tarifa no encontrada", exception.getMessage());
    }

    @Test
    void desactivarTarifaBase_usuarioNoEncontrado_deberiaLanzarErrorApi() {
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setIdTarifaBase(1L);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));

        when(tarifaBaseRepository.findById(1L)).thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.desactivarTarifaBase(1L, 2L));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

}
