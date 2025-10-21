package org.parkcontrol.apiparkcontrol.services.gestion_sucursal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionTarifaSucursalServiceUnitTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;

    @Mock
    private TarifaBaseRepository tarifaBaseRepository;

    @Mock
    private BitacoraTarifaSucursalRepository bitacoraTarifaSucursal;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private TarifaSucursalRepository tarifaRepository;

    @InjectMocks
    private GestionTarifaSucursalService gestionTarifaSucursalService;

    private Sucursal mockSucursal;
    private Empresa mockEmpresa;
    private TarifaSucursal mockTarifaSucursal;
    private TarifaBase mockTarifaBase;

    @BeforeEach
    void setUp() {
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");

        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setNombre("Test Sucursal");

        mockTarifaSucursal = new TarifaSucursal();
        mockTarifaSucursal.setIdTarifaSucursal(1L);
        mockTarifaSucursal.setSucursal(mockSucursal);
        mockTarifaSucursal.setPrecioPorHora(new BigDecimal("10.50"));
        mockTarifaSucursal.setMoneda("GTQ");
        mockTarifaSucursal.setFechaVigenciaInicio(LocalDateTime.now());
        mockTarifaSucursal.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);

        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setEmpresa(mockEmpresa);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        mockTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
    }

    @Test
    void testCrearNuevaTarifaSucursal_Success_WithCustomPrice() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(1L);
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora("12.75"); // Cambiar a String
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin("2024-12-31");

        List<TarifaSucursal> tarifasVigentes = Arrays.asList(mockTarifaSucursal);

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(tarifasVigentes);
        when(tarifaSucursalRepository.save(any(TarifaSucursal.class))).thenReturn(mockTarifaSucursal);
        when(bitacoraTarifaSucursal.save(any(BitacoraTarifaSucursal.class))).thenReturn(new BitacoraTarifaSucursal());

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(1L);
        verify(tarifaSucursalRepository).findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        verify(tarifaSucursalRepository, times(2)).save(any(TarifaSucursal.class)); // One for marking as historic, one for new tariff
        verify(bitacoraTarifaSucursal).save(any(BitacoraTarifaSucursal.class));
        
        // Verify that existing tariffs were marked as historic
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.HISTORICO, mockTarifaSucursal.getEstado());
    }

    @Test
    void testCrearNuevaTarifaSucursal_Success_WithTarifaBase() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(1L);
        nuevaTarifaDTO.setEsTarifaBase(true);
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        List<TarifaSucursal> tarifasVigentes = Arrays.asList();

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(tarifasVigentes);
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(mockTarifaBase);
        when(tarifaSucursalRepository.save(any(TarifaSucursal.class))).thenReturn(mockTarifaSucursal);
        when(bitacoraTarifaSucursal.save(any(BitacoraTarifaSucursal.class))).thenReturn(new BitacoraTarifaSucursal());

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);
        verify(tarifaBaseRepository).findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE);
        verify(tarifaSucursalRepository).save(any(TarifaSucursal.class));
        verify(bitacoraTarifaSucursal).save(any(BitacoraTarifaSucursal.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_SucursalNotFound() {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(999L);

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(999L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);
        });

        assertEquals("Sucursal no encontrada para el usuario proporcionado.", exception.getMessage());
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(999L);
    }

    @Test
    void testCrearNuevaTarifaSucursal_TarifaBaseNotFound() {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(1L);
        nuevaTarifaDTO.setEsTarifaBase(true);
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(Arrays.asList());
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);
        });

        assertEquals("No existe una tarifa base para la empresa de la sucursal.", exception.getMessage());
        verify(tarifaBaseRepository).findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE);
    }

    @Test
    void testEditarTarifaSucursal_Success() throws Exception {
        // Arrange
        TarifaSucursalDTO tarifaDTO = new TarifaSucursalDTO();
        tarifaDTO.setIdTarifaSucursal(1L);
        tarifaDTO.setPrecioPorHora(20.00);
        tarifaDTO.setMoneda("USD");
        tarifaDTO.setFechaVigenciaInicio("2024-01-01");
        tarifaDTO.setFechaVigenciaFin("2024-12-31");
        tarifaDTO.setEstado("VIGENTE");

        when(tarifaSucursalRepository.findById(1L)).thenReturn(Optional.of(mockTarifaSucursal));
        when(tarifaSucursalRepository.save(any(TarifaSucursal.class))).thenReturn(mockTarifaSucursal);
        when(bitacoraTarifaSucursal.save(any(BitacoraTarifaSucursal.class))).thenReturn(new BitacoraTarifaSucursal());

        // Act
        String result = gestionTarifaSucursalService.editarTarifaSucursal(tarifaDTO);

        // Assert
        assertEquals("Tarifa de sucursal editada exitosamente.", result);
        verify(tarifaSucursalRepository).findById(1L);
        verify(tarifaSucursalRepository).save(mockTarifaSucursal);
        verify(bitacoraTarifaSucursal).save(any(BitacoraTarifaSucursal.class));

        // Verify that the tariff was updated
        assertEquals(0, new BigDecimal("20.00").compareTo(mockTarifaSucursal.getPrecioPorHora())); // Cambiar comparación
        assertEquals("USD", mockTarifaSucursal.getMoneda());
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.VIGENTE, mockTarifaSucursal.getEstado());
    }

    @Test
    void testEditarTarifaSucursal_TarifaNotFound() {
        // Arrange
        TarifaSucursalDTO tarifaDTO = new TarifaSucursalDTO();
        tarifaDTO.setIdTarifaSucursal(999L);

        when(tarifaSucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.editarTarifaSucursal(tarifaDTO);
        });

        assertEquals("Tarifa de sucursal no encontrada.", exception.getMessage());
        verify(tarifaSucursalRepository).findById(999L);
    }

    @Test
    void testObtenerTarifasPorIdUsuario_Success() throws Exception {
        // Arrange
        Long idUsuarioSucursal = 1L;
        
        TarifaSucursal tarifa1 = new TarifaSucursal();
        tarifa1.setIdTarifaSucursal(1L);
        tarifa1.setPrecioPorHora(new BigDecimal("10.50"));
        tarifa1.setMoneda("GTQ");
        tarifa1.setFechaVigenciaInicio(LocalDateTime.of(2024, 1, 1, 0, 0));
        tarifa1.setFechaVigenciaFin(LocalDateTime.of(2024, 12, 31, 23, 59));
        tarifa1.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);

        TarifaSucursal tarifa2 = new TarifaSucursal();
        tarifa2.setIdTarifaSucursal(2L);
        tarifa2.setPrecioPorHora(new BigDecimal("8.00"));
        tarifa2.setMoneda("GTQ");
        tarifa2.setFechaVigenciaInicio(LocalDateTime.of(2023, 1, 1, 0, 0));
        tarifa2.setFechaVigenciaFin(null);
        tarifa2.setEstado(TarifaSucursal.EstadoTarifaSucursal.HISTORICO);

        List<TarifaSucursal> tarifas = Arrays.asList(tarifa1, tarifa2);

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursal(1L)).thenReturn(tarifas);

        // Act
        List<TarifaSucursalDTO> result = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(idUsuarioSucursal);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        TarifaSucursalDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getIdTarifaSucursal());
        assertEquals(10.50, dto1.getPrecioPorHora());
        assertEquals("GTQ", dto1.getMoneda());
        assertEquals("VIGENTE", dto1.getEstado());
        assertNotNull(dto1.getFechaVigenciaInicio());
        assertNotNull(dto1.getFechaVigenciaFin());

        TarifaSucursalDTO dto2 = result.get(1);
        assertEquals(2L, dto2.getIdTarifaSucursal());
        assertEquals(8.00, dto2.getPrecioPorHora());
        assertEquals("HISTORICO", dto2.getEstado());
        assertNull(dto2.getFechaVigenciaFin());

        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        verify(tarifaSucursalRepository).findBySucursal_IdSucursal(1L);
    }

    @Test
    void testObtenerTarifasPorIdUsuario_SucursalNotFound() {
        // Arrange
        Long idUsuarioSucursal = 999L;
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(idUsuarioSucursal);
        });

        assertEquals("Sucursal no encontrada para el usuario proporcionado.", exception.getMessage());
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
    }

    @Test
    void testObtenerTarifasPorIdUsuario_EmptyList() throws Exception {
        // Arrange
        Long idUsuarioSucursal = 1L;
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursal(1L)).thenReturn(Arrays.asList());

        // Act
        List<TarifaSucursalDTO> result = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(idUsuarioSucursal);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        verify(tarifaSucursalRepository).findBySucursal_IdSucursal(1L);
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithoutFechaFin() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(1L);
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora("12.75"); // Cambiar a String
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin(null); // No end date

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(Arrays.asList());
        when(tarifaSucursalRepository.save(any(TarifaSucursal.class))).thenReturn(mockTarifaSucursal);
        when(bitacoraTarifaSucursal.save(any(BitacoraTarifaSucursal.class))).thenReturn(new BitacoraTarifaSucursal());

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);
        verify(tarifaSucursalRepository).save(any(TarifaSucursal.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithEmptyFechaFin() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(1L);
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora("12.75"); // Cambiar a String
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin(""); // Empty end date

        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(Arrays.asList());
        when(tarifaSucursalRepository.save(any(TarifaSucursal.class))).thenReturn(mockTarifaSucursal);
        when(bitacoraTarifaSucursal.save(any(BitacoraTarifaSucursal.class))).thenReturn(new BitacoraTarifaSucursal());

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);
        verify(tarifaSucursalRepository).save(any(TarifaSucursal.class));
    }
}
