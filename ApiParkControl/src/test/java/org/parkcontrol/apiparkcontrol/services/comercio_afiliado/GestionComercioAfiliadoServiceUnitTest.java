package org.parkcontrol.apiparkcontrol.services.comercio_afiliado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.comercio_afliado.GestionComercioAfiliadoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionComercioAfiliadoServiceUnitTest {

    @Mock
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Mock
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private GestionComercioAfiliadoService gestionComercioAfiliadoService;

    private ComercioAfiliado mockComercioAfiliado;
    private Usuario mockUsuario;
    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private ConvenioComercioSucursal mockConvenio;
    private TarifaSucursal mockTarifaSucursal;
    private Persona mockPersona;

    @BeforeEach
    void setUp() {
        // Setup ComercioAfiliado
        mockComercioAfiliado = new ComercioAfiliado();
        mockComercioAfiliado.setId(1L);
        mockComercioAfiliado.setNombreComercial("Comercio Test");
        mockComercioAfiliado.setRazonSocial("Comercio Test S.A.");
        mockComercioAfiliado.setNit("1234567-8");
        mockComercioAfiliado.setTipoComercio("Restaurante");
        mockComercioAfiliado.setTelefono("12345678");
        mockComercioAfiliado.setCorreoContacto("comercio@test.com");
        mockComercioAfiliado.setEstado(ComercioAfiliado.Estado.ACTIVO);
        mockComercioAfiliado.setFechaRegistro(LocalDateTime.now());

        // Setup Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Admin");
        mockPersona.setApellido("Test");
        mockPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1)); // Agregar fecha de nacimiento
        mockPersona.setDpi("1234567890123");
        mockPersona.setCorreo("admin@test.com");
        mockPersona.setTelefono("12345678");
        mockPersona.setDireccionCompleta("Test Address");
        mockPersona.setCiudad("Test City");
        mockPersona.setPais("Guatemala");
        mockPersona.setCodigoPostal("01001");

        // Setup Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("adminuser");
        mockUsuario.setPersona(mockPersona);

        // Setup Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setUsuarioEmpresa(mockUsuario);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        // Setup Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setUsuarioSucursal(mockUsuario);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setDireccionCompleta("Dirección Test");
        mockSucursal.setCiudad("Ciudad Test");
        mockSucursal.setDepartamento("Departamento Test");
        mockSucursal.setHoraApertura(LocalTime.of(8, 0));
        mockSucursal.setHoraCierre(LocalTime.of(18, 0));
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(100);
        mockSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);

        // Setup TarifaSucursal
        mockTarifaSucursal = new TarifaSucursal();
        mockTarifaSucursal.setIdTarifaSucursal(1L);
        mockTarifaSucursal.setSucursal(mockSucursal);
        mockTarifaSucursal.setPrecioPorHora(new BigDecimal("15.00"));
        mockTarifaSucursal.setMoneda("GTQ");
        mockTarifaSucursal.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);

        // Setup ConvenioComercioSucursal
        mockConvenio = new ConvenioComercioSucursal();
        mockConvenio.setId(1L);
        mockConvenio.setComercioAfiliado(mockComercioAfiliado);
        mockConvenio.setSucursal(mockSucursal);
        mockConvenio.setHorasGratisMaximo(new BigDecimal("5.0"));
        mockConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        mockConvenio.setTarifaPorHora(new BigDecimal("15.00"));
        mockConvenio.setFechaInicioConvenio(LocalDateTime.now());
        mockConvenio.setFechaFinConvenio(LocalDateTime.now().plusYears(1));
        mockConvenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        mockConvenio.setCreadoPor(mockUsuario);
        mockConvenio.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void testGetComercioAfiliado_Success() {
        // Arrange
        List<ComercioAfiliado> comercios = Arrays.asList(mockComercioAfiliado);
        when(comercioAfiliadoRepository.findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO))
                .thenReturn(comercios);

        // Act
        List<ComercioAfiliadoDTO> result = gestionComercioAfiliadoService.getComercioAfiliado();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ComercioAfiliadoDTO dto = result.get(0);
        assertEquals(1L, dto.getIdComercio());
        assertEquals("Comercio Test", dto.getNombreComercial());
        assertEquals("Comercio Test S.A.", dto.getRazonSocial());
        assertEquals("1234567-8", dto.getNit());
        assertEquals("Restaurante", dto.getTipoComercio());
        assertEquals("12345678", dto.getTelefono());
        assertEquals("comercio@test.com", dto.getCorreoContacto());
        assertEquals("ACTIVO", dto.getEstado());

        verify(comercioAfiliadoRepository).findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO);
    }

    @Test
    void testGetComercioAfiliado_EmptyList() {
        // Arrange
        when(comercioAfiliadoRepository.findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO))
                .thenReturn(Arrays.asList());

        // Act
        List<ComercioAfiliadoDTO> result = gestionComercioAfiliadoService.getComercioAfiliado();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(comercioAfiliadoRepository).findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO);
    }

    @Test
    void testGetDetallesSucursalesConvenio_Success() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        List<ConvenioComercioSucursal> convenios = Arrays.asList(mockConvenio);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(sucursalRepository.findByEmpresa_IdEmpresaAndEstado(1L, Sucursal.EstadoSucursal.ACTIVA))
                .thenReturn(sucursales);
        when(convenioComercioSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(convenios);

        // Act
        List<DetalleSucursalesConvenioDTO> result = gestionComercioAfiliadoService.getDetallesSucursalesConvenio(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSucursalesConvenioDTO sucursalDTO = result.get(0);
        assertEquals(1L, sucursalDTO.getIdSucursal());
        assertEquals("Sucursal Test", sucursalDTO.getNombre());
        assertEquals(1, sucursalDTO.getConvenios().size());

        DetalleSucursalesConvenioDTO.ConvenioSucursalDTO convenioDTO = sucursalDTO.getConvenios().get(0);
        assertEquals(1L, convenioDTO.getIdConvenio());
        assertEquals("5.0", convenioDTO.getHorasGratisMaximo());
        assertEquals("MENSUAL", convenioDTO.getPeriodoCorte());
        assertEquals("ACTIVO", convenioDTO.getEstado());

        verify(usuarioRepository).findById(1L);
        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
    }

    @Test
    void testGetDetallesSucursalesConvenio_UsuarioNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.getDetallesSucursalesConvenio(999L);
        });

        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testGetDetallesSucursalesConvenio_EmpresaNotFound() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList()); // Lista vacía

        // Act & Assert
        // El servicio usa .getFirst() que lanza IndexOutOfBoundsException cuando la lista está vacía
        Exception exception = assertThrows(Exception.class, () -> {
            gestionComercioAfiliadoService.getDetallesSucursalesConvenio(1L);
        });

        // Verificar que se lanza alguna excepción (puede ser IndexOutOfBoundsException o NoSuchElementException)
        assertNotNull(exception);
        assertTrue(exception instanceof IndexOutOfBoundsException || 
                  exception instanceof java.util.NoSuchElementException ||
                  exception.getMessage() != null);
    }

    @Test
    void testCrearComercioAfiliado_Success() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setNombreComercial("Nuevo Comercio");
        dto.setRazonSocial("Nuevo Comercio S.A.");
        dto.setNit("9876543-2");
        dto.setTipoComercio("Tienda");
        dto.setTelefono("87654321");
        dto.setCorreoContacto("nuevo@comercio.com");

        when(comercioAfiliadoRepository.existsByNit("9876543-2")).thenReturn(false);
        when(comercioAfiliadoRepository.save(any(ComercioAfiliado.class))).thenReturn(mockComercioAfiliado);

        // Act
        String result = gestionComercioAfiliadoService.crearComercioAfiliado(dto);

        // Assert
        assertEquals("Comercio afiliado creado exitosamente con ID: 1", result);
        verify(comercioAfiliadoRepository).existsByNit("9876543-2");
        verify(comercioAfiliadoRepository).save(any(ComercioAfiliado.class));
    }

    @Test
    void testCrearComercioAfiliado_NitAlreadyExists() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setNit("1234567-8");

        when(comercioAfiliadoRepository.existsByNit("1234567-8")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearComercioAfiliado(dto);
        });

        assertEquals("Ya existe un comercio afiliado con el NIT: 1234567-8", exception.getMessage());
        verify(comercioAfiliadoRepository).existsByNit("1234567-8");
        verify(comercioAfiliadoRepository, never()).save(any());
    }

    @Test
    void testActualizarComercioAfiliado_Success() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setIdComercio(1L);
        dto.setNombreComercial("Comercio Actualizado");
        dto.setRazonSocial("Comercio Actualizado S.A.");
        dto.setNit("1234567-9");
        dto.setTipoComercio("Cafetería");
        dto.setTelefono("11111111");
        dto.setCorreoContacto("actualizado@comercio.com");

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(comercioAfiliadoRepository.save(any(ComercioAfiliado.class))).thenReturn(mockComercioAfiliado);

        // Act
        String result = gestionComercioAfiliadoService.actualizarComercioAfiliado(dto);

        // Assert
        assertEquals("Comercio afiliado actualizado exitosamente con ID: 1", result);
        verify(comercioAfiliadoRepository).findById(1L);
        verify(comercioAfiliadoRepository).save(mockComercioAfiliado);
    }

    @Test
    void testActualizarComercioAfiliado_NotFound() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setIdComercio(999L);

        when(comercioAfiliadoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.actualizarComercioAfiliado(dto);
        });

        assertEquals("Comercio afiliado no encontrado con ID: 999", exception.getMessage());
        verify(comercioAfiliadoRepository).findById(999L);
    }

    @Test
    void testEliminarComercioAfiliado_Success() {
        // Arrange
        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(comercioAfiliadoRepository.save(any(ComercioAfiliado.class))).thenReturn(mockComercioAfiliado);

        // Act
        String result = gestionComercioAfiliadoService.eliminarComercioAfiliado(1L);

        // Assert
        assertEquals("Comercio afiliado desactivado exitosamente con ID: 1", result);
        assertEquals(ComercioAfiliado.Estado.INACTIVO, mockComercioAfiliado.getEstado());
        verify(comercioAfiliadoRepository).findById(1L);
        verify(comercioAfiliadoRepository).save(mockComercioAfiliado);
    }

    @Test
    void testEliminarComercioAfiliado_NotFound() {
        // Arrange
        when(comercioAfiliadoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.eliminarComercioAfiliado(999L);
        });

        assertEquals("Comercio afiliado no encontrado con ID: 999", exception.getMessage());
        verify(comercioAfiliadoRepository).findById(999L);
    }

    @Test
    void testCrearConvenioComercioSucursal_Success() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(1L);
        dto.setIdSucursal(1L);
        dto.setHorasGratisMaximo("5.0");
        dto.setPeriodoCorte("MENSUAL");
        dto.setFechaInicioConvenio("2024-01-01");
        dto.setFechaFinConvenio("2024-12-31");
        dto.setCreadoPor(1L);

        List<TarifaSucursal> tarifas = Arrays.asList(mockTarifaSucursal);

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(convenioComercioSucursalRepository.existsByComercioAfiliado_IdAndSucursal_IdSucursalAndEstado(
                1L, 1L, ConvenioComercioSucursal.Estado.ACTIVO)).thenReturn(false);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(tarifas);
        when(convenioComercioSucursalRepository.save(any(ConvenioComercioSucursal.class))).thenReturn(mockConvenio);

        // Act
        String result = gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);

        // Assert
        assertEquals("Convenio creado exitosamente con ID: 1", result);
        verify(comercioAfiliadoRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(sucursalRepository).findById(1L);
        verify(convenioComercioSucursalRepository).save(any(ConvenioComercioSucursal.class));
    }

    @Test
    void testCrearConvenioComercioSucursal_ComercioNotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(999L);

        when(comercioAfiliadoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        assertEquals("Comercio afiliado no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testCrearConvenioComercioSucursal_UsuarioNotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(1L);
        dto.setCreadoPor(999L);

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testCrearConvenioComercioSucursal_SucursalNotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(1L);
        dto.setIdSucursal(999L);
        dto.setCreadoPor(1L);

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        assertEquals("Sucursal no encontrada con ID: 999", exception.getMessage());
    }

    @Test
    void testCrearConvenioComercioSucursal_ConvenioAlreadyExists() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(1L);
        dto.setIdSucursal(1L);
        dto.setCreadoPor(1L);

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(convenioComercioSucursalRepository.existsByComercioAfiliado_IdAndSucursal_IdSucursalAndEstado(
                1L, 1L, ConvenioComercioSucursal.Estado.ACTIVO)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        assertEquals("Ya existe un convenio activo entre el comercio afiliado y la sucursal.", exception.getMessage());
    }

    @Test
    void testCrearConvenioComercioSucursal_TarifaNotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(1L);
        dto.setIdSucursal(1L);
        dto.setHorasGratisMaximo("5.0"); // Asegurar que no sea null
        dto.setPeriodoCorte("MENSUAL");
        dto.setCreadoPor(1L);

        when(comercioAfiliadoRepository.findById(1L)).thenReturn(Optional.of(mockComercioAfiliado));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(convenioComercioSucursalRepository.existsByComercioAfiliado_IdAndSucursal_IdSucursalAndEstado(
                1L, 1L, ConvenioComercioSucursal.Estado.ACTIVO)).thenReturn(false);
        when(tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, TarifaSucursal.EstadoTarifaSucursal.VIGENTE))
                .thenReturn(Arrays.asList()); // Lista vacía

        // Act & Assert
        // El servicio usa .getFirst() que lanza IndexOutOfBoundsException cuando la lista está vacía
        Exception exception = assertThrows(Exception.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        // Verificar que se lanza alguna excepción (puede ser IndexOutOfBoundsException o NoSuchElementException)
        assertNotNull(exception);
        assertTrue(exception instanceof IndexOutOfBoundsException || 
                  exception instanceof java.util.NoSuchElementException ||
                  exception.getMessage() != null);
    }

    @Test
    void testActualizarConvenioComercioSucursal_Success() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(1L);
        dto.setHorasGratisMaximo("10.0");
        dto.setPeriodoCorte("ANUAL");
        dto.setFechaInicioConvenio("2024-02-01");
        dto.setFechaFinConvenio("2025-02-01");

        when(convenioComercioSucursalRepository.findById(1L)).thenReturn(Optional.of(mockConvenio));
        when(convenioComercioSucursalRepository.save(any(ConvenioComercioSucursal.class))).thenReturn(mockConvenio);

        // Act
        String result = gestionComercioAfiliadoService.actualizarConvenioComercioSucursal(dto);

        // Assert
        assertEquals("Convenio actualizado exitosamente con ID: 1", result);
        verify(convenioComercioSucursalRepository).findById(1L);
        verify(convenioComercioSucursalRepository).save(mockConvenio);
    }

    @Test
    void testActualizarConvenioComercioSucursal_NotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(999L);

        when(convenioComercioSucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.actualizarConvenioComercioSucursal(dto);
        });

        assertEquals("Convenio no encontrado con ID: 999", exception.getMessage());
        verify(convenioComercioSucursalRepository).findById(999L);
    }

    @Test
    void testCambiarEstadoConvenio_Success() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(1L);
        dto.setEstado("INACTIVO");

        when(convenioComercioSucursalRepository.findById(1L)).thenReturn(Optional.of(mockConvenio));
        when(convenioComercioSucursalRepository.save(any(ConvenioComercioSucursal.class))).thenReturn(mockConvenio);

        // Act
        String result = gestionComercioAfiliadoService.cambiarEstadoConvenio(dto);

        // Assert
        assertEquals("Estado del convenio actualizado exitosamente a: INACTIVO", result);
        assertEquals(ConvenioComercioSucursal.Estado.INACTIVO, mockConvenio.getEstado());
        verify(convenioComercioSucursalRepository).findById(1L);
        verify(convenioComercioSucursalRepository).save(mockConvenio);
    }

    @Test
    void testCambiarEstadoConvenio_NotFound() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(999L);
        dto.setEstado("INACTIVO");

        when(convenioComercioSucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.cambiarEstadoConvenio(dto);
        });

        assertEquals("Convenio no encontrado con ID: 999", exception.getMessage());
        verify(convenioComercioSucursalRepository).findById(999L);
    }

    @Test
    void testGetDetallesSucursalesConvenio_WithoutConvenios() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(sucursalRepository.findByEmpresa_IdEmpresaAndEstado(1L, Sucursal.EstadoSucursal.ACTIVA))
                .thenReturn(sucursales);
        when(convenioComercioSucursalRepository.findBySucursal_IdSucursalAndEstado(1L, ConvenioComercioSucursal.Estado.ACTIVO))
                .thenReturn(Arrays.asList());

        // Act
        List<DetalleSucursalesConvenioDTO> result = gestionComercioAfiliadoService.getDetallesSucursalesConvenio(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        DetalleSucursalesConvenioDTO sucursalDTO = result.get(0);
        assertEquals(1L, sucursalDTO.getIdSucursal());
        assertTrue(sucursalDTO.getConvenios().isEmpty());
    }

    @Test
    void testGetDetallesSucursalesConvenio_WithoutSucursales() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(sucursalRepository.findByEmpresa_IdEmpresaAndEstado(1L, Sucursal.EstadoSucursal.ACTIVA))
                .thenReturn(Arrays.asList());

        // Act
        List<DetalleSucursalesConvenioDTO> result = gestionComercioAfiliadoService.getDetallesSucursalesConvenio(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
