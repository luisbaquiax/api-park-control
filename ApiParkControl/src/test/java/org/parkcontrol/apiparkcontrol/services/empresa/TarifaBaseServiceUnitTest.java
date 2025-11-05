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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarifaBaseServiceUnitTest {

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
    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;

    private Usuario usuario;
    private Empresa empresa;
    private TarifaBase tarifaBase;
    private TarifaBaseResponse tarifaBaseResponse;
    private Sucursal sucursal;
    private TarifaSucursal tarifaSucursal;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("testuser");

        empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        empresa.setUsuarioEmpresa(usuario);
        empresa.setNombreComercial("Test Company");
        empresa.setNit("1234567-8");

        tarifaBase = new TarifaBase();
        tarifaBase.setIdTarifaBase(1L);
        tarifaBase.setEmpresa(empresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusDays(30));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);

        tarifaBaseResponse = new TarifaBaseResponse();
        tarifaBaseResponse.setPrecioPorHora(BigDecimal.valueOf(60));
        tarifaBaseResponse.setMoneda("GTQ");
        tarifaBaseResponse.setFechaVigenciaInicio(LocalDate.now());
        tarifaBaseResponse.setFechaVigenciaFin(LocalDate.now().plusDays(30));

        sucursal = new Sucursal();
        sucursal.setIdSucursal(1L);
        sucursal.setEmpresa(empresa);
        sucursal.setNombre("Test Sucursal");

        tarifaSucursal = new TarifaSucursal();
        tarifaSucursal.setIdTarifaSucursal(1L);
        tarifaSucursal.setSucursal(sucursal);
        tarifaSucursal.setPrecioPorHora(BigDecimal.valueOf(55));
        tarifaSucursal.setFechaVigenciaInicio(LocalDate.now().atStartOfDay());
        tarifaSucursal.setFechaVigenciaFin(LocalDate.now().plusDays(30).atStartOfDay());
        tarifaSucursal.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);

        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setTarifaBaseReferencia(tarifaBase);
    }

    // ========== CREATE TESTS ==========
    
    @Test
    void testCreateTarifaBase_Success_FechaInicioHoy() {
        // Arrange
        tarifaBaseResponse.setFechaVigenciaInicio(LocalDate.now());
        
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.existeSolapamiento(anyLong(), any(), any()))
                .thenReturn(false);
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bitacoraTarifaBaseRepository.save(any(BitacoraTarifaBase.class)))
                .thenReturn(new BitacoraTarifaBase());

        // Act
        TarifaBase created = tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario());

        // Assert
        assertNotNull(created);
        assertEquals(BigDecimal.valueOf(60), created.getPrecioPorHora());
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, created.getEstado());
        verify(bitacoraTarifaBaseRepository, times(1)).save(any(BitacoraTarifaBase.class));
        verify(tarifaBaseRepository, times(1)).save(any(TarifaBase.class));
    }

    @Test
    void testCreateTarifaBase_Success_FechaInicioProgramado() {
        // Arrange
        tarifaBaseResponse.setFechaVigenciaInicio(LocalDate.now().plusDays(5));
        
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.existeSolapamiento(anyLong(), any(), any()))
                .thenReturn(false);
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TarifaBase created = tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario());

        // Assert
        assertNotNull(created);
        assertEquals(TarifaBase.EstadoTarifaBase.PROGRAMADO, created.getEstado());
    }

    @Test
    void testCreateTarifaBase_FailsIfUserNoEmpresa() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(403, exception.getStatus());
        assertEquals("El usuario no tiene una empresa asociada", exception.getMessage());
    }

    @Test
    void testCreateTarifaBase_FailsIfUserNotOwner() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(999L);
        empresa.setUsuarioEmpresa(otroUsuario);
        
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(403, exception.getStatus());
        assertEquals("El usuario no tiene permisos para crear una tarifa en esta empresa", exception.getMessage());
    }

    @Test
    void testCreateTarifaBase_FailsIfSolapamiento() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.existeSolapamiento(anyLong(), any(), any()))
                .thenReturn(true);

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ya existe la tarifa"));
    }

    @Test
    void testCreateTarifaBase_FailsIfUsuarioNotFound() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.existeSolapamiento(anyLong(), any(), any()))
                .thenReturn(false);
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void testUpdateTarifaBase_Success_ConCambioDePrecio() {
        // Arrange
        tarifaBaseResponse.setIdTarifaBase(tarifaBase.getIdTarifaBase());
        tarifaBaseResponse.setPrecioPorHora(BigDecimal.valueOf(70)); // Diferente al original

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bitacoraTarifaBaseRepository.save(any(BitacoraTarifaBase.class)))
                .thenReturn(new BitacoraTarifaBase());

        // Act
        TarifaBase updated = tarifaBaseService.update(tarifaBaseResponse, usuario.getIdUsuario());

        // Assert
        assertNotNull(updated);
        assertEquals(TarifaBase.EstadoTarifaBase.HISTORICO, updated.getEstado());
        verify(tarifaBaseRepository, times(2)).save(any(TarifaBase.class)); // Original + nueva
        verify(bitacoraTarifaBaseRepository, times(1)).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testUpdateTarifaBase_Success_SinCambioDePrecio() {
        // Arrange
        tarifaBaseResponse.setIdTarifaBase(tarifaBase.getIdTarifaBase());
        tarifaBaseResponse.setPrecioPorHora(BigDecimal.valueOf(50)); // Igual al original

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TarifaBase updated = tarifaBaseService.update(tarifaBaseResponse, usuario.getIdUsuario());

        // Assert
        assertNotNull(updated);
        verify(tarifaBaseRepository, times(1)).save(any(TarifaBase.class)); // Solo guarda el original
        verify(bitacoraTarifaBaseRepository, never()).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testUpdateTarifaBase_FailsIfTarifaNotFound() {
        // Arrange
        tarifaBaseResponse.setIdTarifaBase(999L);
        
        when(tarifaBaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.update(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(404, exception.getStatus());
        assertEquals("Tarifa no encontrada", exception.getMessage());
    }

    @Test
    void testUpdateTarifaBase_FailsIfUsuarioNotFound() {
        // Arrange
        tarifaBaseResponse.setIdTarifaBase(tarifaBase.getIdTarifaBase());
        tarifaBaseResponse.setPrecioPorHora(BigDecimal.valueOf(70));

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.update(tarifaBaseResponse, usuario.getIdUsuario()));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ========== ACTIVAR TESTS ==========

    @Test
    void testActivarTarifaBase_Success() {
        // Arrange
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(bitacoraTarifaBaseRepository.save(any(BitacoraTarifaBase.class)))
                .thenReturn(new BitacoraTarifaBase());
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenReturn(tarifaBase);

        // Act
        MessageSuccess response = tarifaBaseService.activarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario());

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Tarifa activada correctamente", response.getMessage());
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, tarifaBase.getEstado());
        verify(bitacoraTarifaBaseRepository).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testActivarTarifaBase_FailsIfFechaInicioNoHoy() {
        // Arrange
        tarifaBase.setFechaVigenciaInicio(LocalDate.now().plusDays(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);

        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.activarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario()));
        assertEquals(409, exception.getStatus());
        assertEquals("No se puede activar la tarifa antes de su fecha de inicio de vigencia.", exception.getMessage());
    }

    @Test
    void testActivarTarifaBase_FailsIfTarifaNotFound() {
        // Arrange
        when(tarifaBaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.activarTarifaBase(999L, usuario.getIdUsuario()));
        assertEquals(404, exception.getStatus());
        assertEquals("Tarifa no encontrada", exception.getMessage());
    }

    @Test
    void testActivarTarifaBase_FailsIfUsuarioNotFound() {
        // Arrange
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        
        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.activarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario()));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ========== DESACTIVAR TESTS ==========

    @Test
    void testDesactivarTarifaBase_Success() {
        // Arrange
        when(tarifaBaseRepository.findById(tarifaBase.getIdTarifaBase()))
                .thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(bitacoraTarifaBaseRepository.save(any(BitacoraTarifaBase.class)))
                .thenReturn(new BitacoraTarifaBase());
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenReturn(tarifaBase);

        // Act
        MessageSuccess response = tarifaBaseService.desactivarTarifaBase(tarifaBase.getIdTarifaBase(), usuario.getIdUsuario());

        // Assert
        assertEquals(201, response.getCode());
        assertEquals("Tarifa desactivada correctamente", response.getMessage());
        assertEquals(TarifaBase.EstadoTarifaBase.HISTORICO, tarifaBase.getEstado());
        verify(bitacoraTarifaBaseRepository).save(any(BitacoraTarifaBase.class));
    }

    @Test
    void testDesactivarTarifaBase_FailsIfTarifaNotFound() {
        // Arrange
        when(tarifaBaseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.desactivarTarifaBase(1L, 2L));
        assertEquals(404, exception.getStatus());
        assertEquals("Tarifa no encontrada", exception.getMessage());
    }

    @Test
    void testDesactivarTarifaBase_FailsIfUsuarioNotFound() {
        // Arrange
        when(tarifaBaseRepository.findById(1L)).thenReturn(Optional.of(tarifaBase));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.desactivarTarifaBase(1L, 2L));
        assertEquals(404, exception.getStatus());
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ========== FIND BY ESTADO TESTS ==========

    @Test
    void testFindTarifaBaseByEmpresaIdByEstado_Success() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(
                TarifaBase.EstadoTarifaBase.VIGENTE, empresa.getIdEmpresa()))
                .thenReturn(tarifaBase);

        // Act
        TarifaBase result = tarifaBaseService.findTarifaBaseByEmpresaIdByEstado(
                TarifaBase.EstadoTarifaBase.VIGENTE, usuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(tarifaBase.getIdTarifaBase(), result.getIdTarifaBase());
    }

    @Test
    void testFindTarifaBaseByEmpresaIdByEstado_FailsIfNoEmpresa() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.findTarifaBaseByEmpresaIdByEstado(
                        TarifaBase.EstadoTarifaBase.VIGENTE, usuario.getIdUsuario()));
        assertEquals(403, exception.getStatus());
        assertEquals("El usuario no tiene una empresa asociada", exception.getMessage());
    }

    // ========== TARIFA VIGENTE SUCURSAL TESTS ==========

    @Test
    void testTarifaVigenteSucursal_ConTarifaSucursal() {
        // Arrange
        when(tarifaSucursalRepository.findVigenteBySucursalIdAndFecha(
                eq(sucursal.getIdSucursal()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(tarifaSucursal));

        // Act
        BigDecimal result = tarifaBaseService.tarifaVigenteSucursal(sucursal);

        // Assert
        assertEquals(BigDecimal.valueOf(55), result);
    }

    @Test
    void testTarifaVigenteSucursal_SinTarifaSucursal_UsaTarifaBase() {
        // Arrange
        when(tarifaSucursalRepository.findVigenteBySucursalIdAndFecha(
                eq(sucursal.getIdSucursal()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(
                TarifaBase.EstadoTarifaBase.VIGENTE, empresa.getIdEmpresa()))
                .thenReturn(tarifaBase);

        // Act
        BigDecimal result = tarifaBaseService.tarifaVigenteSucursal(sucursal);

        // Assert
        assertEquals(BigDecimal.valueOf(50), result);
    }

    // ========== TARIFA CONGELADA TESTS ==========

    @Test
    void testTarifaCongelada_Success() {
        // Act
        BigDecimal result = tarifaBaseService.tarfifaCongelada(suscripcion);

        // Assert
        assertEquals(BigDecimal.valueOf(50), result);
    }

    // ========== TARIFA BASE VIGENTE EMPRESA TESTS ==========

    @Test
    void testTarifaBaseVigenteEmpresa_Success() {
        // Arrange
        when(tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(
                TarifaBase.EstadoTarifaBase.VIGENTE, empresa.getIdEmpresa()))
                .thenReturn(tarifaBase);

        // Act
        BigDecimal result = tarifaBaseService.tarifaBaseVigenteEmpresa(empresa);

        // Assert
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void testTarifaBaseVigenteEmpresa_FailsIfNoTarifa() {
        // Arrange
        when(tarifaBaseRepository.findByEstadoAndEmpresa_IdEmpresa(
                TarifaBase.EstadoTarifaBase.VIGENTE, empresa.getIdEmpresa()))
                .thenReturn(null);

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.tarifaBaseVigenteEmpresa(empresa));
        assertEquals(404, exception.getStatus());
        assertEquals("No se encontró una tarifa base vigente para la empresa.", exception.getMessage());
    }

    // ========== FIND ALL BY EMPRESA TESTS ==========

    @Test
    void testFindAllByEmpresa_Success() {
        // Arrange
        TarifaBase tarifa2 = new TarifaBase();
        tarifa2.setIdTarifaBase(2L);
        tarifa2.setEmpresa(empresa);
        tarifa2.setEstado(TarifaBase.EstadoTarifaBase.HISTORICO);

        List<TarifaBase> tarifas = Arrays.asList(tarifaBase, tarifa2);

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.getTarifaBaseByEmpresa_IdEmpresa(empresa.getIdEmpresa()))
                .thenReturn(tarifas);

        // Act
        List<TarifaBase> result = tarifaBaseService.findAllByEmpresa(usuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tarifaBase));
        assertTrue(result.contains(tarifa2));
    }

    @Test
    void testFindAllByEmpresa_FailsIfNoEmpresa() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.findAllByEmpresa(usuario.getIdUsuario()));
        assertEquals(403, exception.getStatus());
        assertEquals("El usuario no tiene una empresa asociada", exception.getMessage());
    }

    @Test
    void testFindAllByEmpresa_EmptyList() {
        // Arrange
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(usuario.getIdUsuario()))
                .thenReturn(List.of(empresa));
        when(tarifaBaseRepository.getTarifaBaseByEmpresa_IdEmpresa(empresa.getIdEmpresa()))
                .thenReturn(Collections.emptyList());

        // Act
        List<TarifaBase> result = tarifaBaseService.findAllByEmpresa(usuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
