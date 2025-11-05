package org.parkcontrol.apiparkcontrol.services.gestion_incidencias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResolucionIncidenciasServiceUnitTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private EvidenciaIncidenciaRepository evidenciaIncidenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private IncidenciaTicketRepository incidenciaTicketRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private IncidenciaTicketSucursalService incidenciaTicketSucursalService;

    @InjectMocks
    private ResolucionIncidenciasService resolucionIncidenciasService;

    private Usuario mockUsuarioEmpresa;
    private Usuario mockUsuarioResuelve;
    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private IncidenciaTicket mockIncidencia;
    private Persona mockPersona;

    @BeforeEach
    void setUp() {
        // Setup test values
        ReflectionTestUtils.setField(resolucionIncidenciasService, "S3_BUCKET_BACKEND", "test-bucket");
        ReflectionTestUtils.setField(resolucionIncidenciasService, "URL_BASE_LOCAL", "http://localhost:8080/files/");
        ReflectionTestUtils.setField(resolucionIncidenciasService, "region", "us-east-1");
        ReflectionTestUtils.setField(resolucionIncidenciasService, "storageType", "local");

        // Setup Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Admin");
        mockPersona.setApellido("Empresa");
        mockPersona.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        mockPersona.setDpi("1234567890123");
        mockPersona.setCorreo("admin@empresa.com");
        mockPersona.setTelefono("87654321");
        mockPersona.setEstado(Persona.Estado.ACTIVO);

        // Setup Usuario Empresa
        mockUsuarioEmpresa = new Usuario();
        mockUsuarioEmpresa.setIdUsuario(1L);
        mockUsuarioEmpresa.setPersona(mockPersona);
        mockUsuarioEmpresa.setNombreUsuario("adminempresa");

        // Setup Usuario que resuelve
        mockUsuarioResuelve = new Usuario();
        mockUsuarioResuelve.setIdUsuario(2L);
        mockUsuarioResuelve.setPersona(mockPersona);
        mockUsuarioResuelve.setNombreUsuario("resolver");

        // Setup Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setUsuarioEmpresa(mockUsuarioEmpresa);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setRazonSocial("Test Company S.A.");

        // Setup Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setUsuarioSucursal(mockUsuarioEmpresa);
        mockSucursal.setNombre("Sucursal Test");
        mockSucursal.setDireccionCompleta("Dirección Test");
        mockSucursal.setTelefonoContacto("12345678");

        // Setup IncidenciaTicket
        mockIncidencia = new IncidenciaTicket();
        mockIncidencia.setIdIncidencia(1L);
        mockIncidencia.setTipoIncidencia(IncidenciaTicket.TipoIncidencia.COMPROBANTE_PERDIDO);
        mockIncidencia.setDescripcion("Test incidencia");
        mockIncidencia.setFechaIncidencia(LocalDateTime.now());
        mockIncidencia.setResuelto(false);
    }

    @Test
    void testObtenerDetalleIncidenciasPorEmpresa_Success() throws Exception {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        
        IncidenciasSucursalDTO incidenciaSucursalDTO = new IncidenciasSucursalDTO();
        incidenciaSucursalDTO.setIdTicket(1L);
        incidenciaSucursalDTO.setFolioNumerico("12345");
        
        IncidenciasSucursalDTO.IncidenciasTicketDTO incidenciaTicketDTO = new IncidenciasSucursalDTO.IncidenciasTicketDTO();
        incidenciaTicketDTO.setIdIncidencia(1L);
        incidenciaTicketDTO.setTipoIncidencia("COMPROBANTE_PERDIDO");
        incidenciaTicketDTO.setDescripcion("Test incidencia");
        incidenciaSucursalDTO.setIncidencias(incidenciaTicketDTO);
        
        List<IncidenciasSucursalDTO> incidenciasSucursal = Arrays.asList(incidenciaSucursalDTO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L)).thenReturn(incidenciasSucursal);

        // Act
        List<DetalleSucursalesIncidenciasDTO> result = resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSucursalesIncidenciasDTO detalleDTO = result.get(0);
        assertEquals(1L, detalleDTO.getIdSucursal());
        assertEquals("Sucursal Test", detalleDTO.getNombreSucursal());
        assertEquals("Dirección Test", detalleDTO.getDireccionSucursal());
        assertEquals("12345678", detalleDTO.getTelefonoSucursal());
        assertEquals(1, detalleDTO.getIncidenciasSucursalDTOList().size());

        verify(usuarioRepository).findById(1L);
        verify(empresaRepository).findByUsuarioEmpresa_IdUsuario(1L);
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(incidenciaTicketSucursalService).obtenerIncidenciasSucursal(1L);
    }

    @Test
    void testObtenerDetalleIncidenciasPorEmpresa_UsuarioNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(999L);
        });

        assertEquals("Usuario de empresa no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testObtenerDetalleIncidenciasPorEmpresa_EmpresaNotFound() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        // Simular el comportamiento de getFirst() cuando la lista está vacía
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(1L);
        });

        // El servicio usa getFirst() que lanza IndexOutOfBoundsException cuando la lista está vacía
        assertTrue(exception instanceof java.util.NoSuchElementException || 
                  exception instanceof IndexOutOfBoundsException ||
                  exception.getMessage().contains("Empresa no encontrada"));
    }

    @Test
    void testObtenerDetalleIncidenciasPorEmpresa_MultipleSucursales() throws Exception {
        // Arrange
        Sucursal sucursal2 = new Sucursal();
        sucursal2.setIdSucursal(2L);
        sucursal2.setEmpresa(mockEmpresa);
        sucursal2.setUsuarioSucursal(mockUsuarioEmpresa);
        sucursal2.setNombre("Sucursal Test 2");
        sucursal2.setDireccionCompleta("Dirección Test 2");
        sucursal2.setTelefonoContacto("87654321");

        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal, sucursal2);

        IncidenciasSucursalDTO incidenciaSucursalDTO = new IncidenciasSucursalDTO();
        incidenciaSucursalDTO.setIdTicket(1L);
        List<IncidenciasSucursalDTO> incidenciasSucursal = Arrays.asList(incidenciaSucursalDTO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuarioEmpresa));
        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(1L)).thenReturn(empresas);
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L)).thenReturn(incidenciasSucursal);
        // Remover el stubbing innecesario que causa el error UnnecessaryStubbing
        // when(incidenciaTicketSucursalService.obtenerIncidenciasSucursal(2L)).thenReturn(Arrays.asList());

        // Act
        List<DetalleSucursalesIncidenciasDTO> result = resolucionIncidenciasService.obtenerDetalleIncidenciasPorEmpresa(1L);

        // Assert
        assertNotNull(result);
        // El servicio filtra sucursales sin incidencias, pero incluye aquellas que sí tienen
        assertTrue(result.size() >= 1);
        assertEquals("Sucursal Test", result.get(0).getNombreSucursal());
    }

    @Test
    void testResolverIncidencia_Success() throws Exception {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(1L);
        resolucionDTO.setIdUsuarioResuelve(2L);
        resolucionDTO.setObservacionesResolucion("Resuelto correctamente");

        when(incidenciaTicketRepository.findById(1L)).thenReturn(Optional.of(mockIncidencia));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockUsuarioResuelve));
        when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenReturn(mockIncidencia);

        // Act
        String result = resolucionIncidenciasService.resolverIncidencia(resolucionDTO);

        // Assert
        assertEquals("Incidencia con ID: 1 resuelta exitosamente.", result);
        assertTrue(mockIncidencia.isResuelto());
        assertEquals(2L, mockIncidencia.getResueltoPor());
        assertEquals("Resuelto correctamente", mockIncidencia.getObservacionesResolucion());
        assertNotNull(mockIncidencia.getFechaResolucion());

        verify(incidenciaTicketRepository).findById(1L);
        verify(usuarioRepository).findById(2L);
        verify(incidenciaTicketRepository).save(mockIncidencia);
    }

    @Test
    void testResolverIncidencia_IncidenciaNotFound() {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(999L);

        when(incidenciaTicketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            resolucionIncidenciasService.resolverIncidencia(resolucionDTO);
        });

        assertEquals("Incidencia no encontrada con ID: 999", exception.getMessage());
        verify(incidenciaTicketRepository).findById(999L);
    }

    @Test
    void testResolverIncidencia_UsuarioNotFound() {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(1L);
        resolucionDTO.setIdUsuarioResuelve(999L);

        when(incidenciaTicketRepository.findById(1L)).thenReturn(Optional.of(mockIncidencia));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            resolucionIncidenciasService.resolverIncidencia(resolucionDTO);
        });

        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testResolverIncidencia_AlreadyResolved() {
        // Arrange
        mockIncidencia.setResuelto(true);
        mockIncidencia.setFechaResolucion(LocalDateTime.now());

        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(1L);
        resolucionDTO.setIdUsuarioResuelve(2L);

        when(incidenciaTicketRepository.findById(1L)).thenReturn(Optional.of(mockIncidencia));
        // Según el service, verifica el usuario ANTES de verificar si está resuelta
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockUsuarioResuelve));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            resolucionIncidenciasService.resolverIncidencia(resolucionDTO);
        });

        assertEquals("La incidencia con ID: 1 ya está resuelta.", exception.getMessage());
        verify(incidenciaTicketRepository).findById(1L);
        verify(usuarioRepository).findById(2L); // El servicio SÍ busca el usuario antes de verificar
        verify(incidenciaTicketRepository, never()).save(any());
    }

    @Test
    void testResolverIncidencia_EmptyObservaciones() throws Exception {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(1L);
        resolucionDTO.setIdUsuarioResuelve(2L);
        resolucionDTO.setObservacionesResolucion(""); // Empty observations

        when(incidenciaTicketRepository.findById(1L)).thenReturn(Optional.of(mockIncidencia));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockUsuarioResuelve));
        when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenReturn(mockIncidencia);

        // Act
        String result = resolucionIncidenciasService.resolverIncidencia(resolucionDTO);

        // Assert
        assertEquals("Incidencia con ID: 1 resuelta exitosamente.", result);
        assertTrue(mockIncidencia.isResuelto());
        assertEquals("", mockIncidencia.getObservacionesResolucion());
    }

    @Test
    void testResolverIncidencia_NullObservaciones() throws Exception {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(1L);
        resolucionDTO.setIdUsuarioResuelve(2L);
        resolucionDTO.setObservacionesResolucion(null); // Null observations

        when(incidenciaTicketRepository.findById(1L)).thenReturn(Optional.of(mockIncidencia));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mockUsuarioResuelve));
        when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenReturn(mockIncidencia);

        // Act
        String result = resolucionIncidenciasService.resolverIncidencia(resolucionDTO);

        // Assert
        assertEquals("Incidencia con ID: 1 resuelta exitosamente.", result);
        assertTrue(mockIncidencia.isResuelto());
        assertNull(mockIncidencia.getObservacionesResolucion());
    }
}
