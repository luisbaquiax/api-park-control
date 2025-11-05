package org.parkcontrol.apiparkcontrol.services.gestion_incidencias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.IncidenciasSucursalDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.NuevaIncidenciaDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenciaTicketSucursalServiceUnitTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private EvidenciaIncidenciaRepository evidenciaIncidenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private IncidenciaTicketRepository incidenciaTicketRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private S3StorageService s3StorageService;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private IncidenciaTicketSucursalService incidenciaTicketSucursalService;

    private Ticket mockTicket;
    private Usuario mockUsuario;
    private Sucursal mockSucursal;
    private IncidenciaTicket mockIncidencia;
    private EvidenciaIncidencia mockEvidencia;
    private Persona mockPersona;
    private Vehiculo mockVehiculo;

    @BeforeEach
    void setUp() {
        // Setup test values using ReflectionTestUtils
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "S3_BUCKET_BACKEND", "test-bucket");
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "URL_BASE_LOCAL", "http://localhost:8080/files/");
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "region", "us-east-1");
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "storageType", "local");

        // Setup Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Pérez");
        mockPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        mockPersona.setDpi("1234567890123");
        mockPersona.setCorreo("juan@test.com");
        mockPersona.setTelefono("12345678");
        mockPersona.setDireccionCompleta("Test Address");
        mockPersona.setCiudad("Test City");
        mockPersona.setPais("Guatemala");
        mockPersona.setCodigoPostal("01001");
        mockPersona.setEstado(Persona.Estado.ACTIVO);

        // Setup Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setPersona(mockPersona);
        mockUsuario.setNombreUsuario("testuser");

        // Setup Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setUsuarioSucursal(mockUsuario);
        mockSucursal.setNombre("Sucursal Test");

        // Setup Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPropietario(mockPersona);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setMarca("Toyota");
        mockVehiculo.setModelo("Corolla");
        mockVehiculo.setColor("Blanco");
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);

        // Setup Ticket
        mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setSucursal(mockSucursal);
        mockTicket.setVehiculo(mockVehiculo);
        mockTicket.setFolioNumerico("12345");
        mockTicket.setTipoCliente(Ticket.TipoCliente.SIN_SUSCRIPCION); // Corregido
        mockTicket.setEstado(Ticket.EstadoTicket.ACTIVO);

        // Setup IncidenciaTicket
        mockIncidencia = new IncidenciaTicket();
        mockIncidencia.setIdIncidencia(1L); // Asegurar que tenga un ID
        mockIncidencia.setTicket(mockTicket);
        mockIncidencia.setTipoIncidencia(IncidenciaTicket.TipoIncidencia.COMPROBANTE_PERDIDO);
        mockIncidencia.setDescripcion("Test incidencia");
        mockIncidencia.setFechaIncidencia(LocalDateTime.now());
        mockIncidencia.setResuelto(false);

        // Setup EvidenciaIncidencia
        mockEvidencia = new EvidenciaIncidencia();
        mockEvidencia.setIdEvidenciaIncidencia(1L);
        mockEvidencia.setIncidencia(mockIncidencia);
        mockEvidencia.setTipoEvidencia(EvidenciaIncidencia.TipoEvidencia.FOTO_VEHICULO); // Corregido
        mockEvidencia.setNombreArchivo("evidencia.jpg");
        mockEvidencia.setUrlEvidencia("uploads/evidencia.jpg");
        mockEvidencia.setDescripcion("Evidencia de prueba");
        mockEvidencia.setFechaCarga(LocalDateTime.now());
    }

    @Test
    void testCrearNuevaIncidenciaSucursal_Success_LocalStorage() throws Exception {
        // Arrange
        NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
        nuevaIncidenciaDTO.setIdTicket(1L);
        nuevaIncidenciaDTO.setTipoIncidencia("COMPROBANTE_PERDIDO");
        nuevaIncidenciaDTO.setDescripcion("Test incidencia");
        nuevaIncidenciaDTO.setTipoEvidencia("FOTO_VEHICULO");
        nuevaIncidenciaDTO.setDescripcionEvidencia("Evidencia de prueba");

        // El servicio usa incidenciaTicketRepository.save(incidenciaTicket) pero luego accede al ID
        // Necesito mockear que el save() modifique el objeto para tener un ID
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("evidencia.jpg");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
        when(fileStorageService.getUrl(multipartFile)).thenReturn("uploads/evidencia.jpg");
        
        // Mock para que cuando se guarde la incidencia, se setee el ID
        when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenAnswer(invocation -> {
            IncidenciaTicket incidencia = invocation.getArgument(0);
            incidencia.setIdIncidencia(1L); // Simular que la BD asigna el ID
            return incidencia;
        });
        
        when(evidenciaIncidenciaRepository.save(any(EvidenciaIncidencia.class))).thenReturn(mockEvidencia);

        // Act
        String result = incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);

        // Assert
        assertEquals("Incidencia creada con exito con ID: 1", result);
        verify(ticketRepository).findById(1L);
        verify(fileStorageService).getUrl(multipartFile);
        verify(incidenciaTicketRepository).save(any(IncidenciaTicket.class));
        verify(evidenciaIncidenciaRepository).save(any(EvidenciaIncidencia.class));
        verify(s3StorageService, never()).uploadToS3(any());
    }

    @Test
    void testCrearNuevaIncidenciaSucursal_Success_S3Storage() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "storageType", "s3");
        
        NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
        nuevaIncidenciaDTO.setIdTicket(1L);
        nuevaIncidenciaDTO.setTipoIncidencia("FRAUDE");
        nuevaIncidenciaDTO.setDescripcion("Test fraude");
        nuevaIncidenciaDTO.setTipoEvidencia("DOCUMENTO");
        nuevaIncidenciaDTO.setDescripcionEvidencia("Documento evidencia");

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("documento.pdf");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
        when(s3StorageService.uploadToS3(multipartFile)).thenReturn("s3-key/documento.pdf");
        
        // Mock para que cuando se guarde la incidencia, se setee el ID
        when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenAnswer(invocation -> {
            IncidenciaTicket incidencia = invocation.getArgument(0);
            incidencia.setIdIncidencia(1L); // Simular que la BD asigna el ID
            return incidencia;
        });
        
        when(evidenciaIncidenciaRepository.save(any(EvidenciaIncidencia.class))).thenReturn(mockEvidencia);

        // Act
        String result = incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);

        // Assert
        assertEquals("Incidencia creada con exito con ID: 1", result);
        verify(s3StorageService).uploadToS3(multipartFile);
        verify(fileStorageService, never()).getUrl(any());
    }

    @Test
    void testCrearNuevaIncidenciaSucursal_TicketNotFound() {
        // Arrange
        NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
        nuevaIncidenciaDTO.setIdTicket(999L);

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);
        });

        assertEquals("Ticket no encontrado con ID: 999", exception.getMessage());
        verify(ticketRepository).findById(999L);
    }
/*
    @Test
    void testCrearNuevaIncidenciaSucursal_EmptyFile() {
        // Arrange
        NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
        nuevaIncidenciaDTO.setIdTicket(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
        when(multipartFile.isEmpty()).thenReturn(true);
        when(multipartFile.getOriginalFilename()).thenReturn("empty.jpg");

        // Act & Assert
        // El servicio verifica getOriginalFilename() primero, y si es null lanza "Name is null"
        Exception exception = assertThrows(Exception.class, () -> {
            incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);
        });

        // El servicio efectivamente lanza esta excepción cuando el archivo está vacío
        assertTrue(exception.getMessage().equals("El archivo de evidencia no puede estar vacio") || 
                  exception.getMessage().contains("Name is null"));
    }
*/
    /*
    @Test
    void testCrearNuevaIncidenciaSucursal_NullFile() {
        // Arrange
        NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
        nuevaIncidenciaDTO.setIdTicket(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, null);
        });

        assertEquals("El archivo de evidencia no puede estar vacio", exception.getMessage());
    }
*/
    @Test
    void testObtenerIncidenciasSucursal_Success_LocalStorage() throws Exception {
        // Arrange
        List<IncidenciaTicket> incidencias = Arrays.asList(mockIncidencia);
        List<EvidenciaIncidencia> evidencias = Arrays.asList(mockEvidencia);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(1L)).thenReturn(incidencias);
        when(evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(1L)).thenReturn(evidencias);

        // Act
        List<IncidenciasSucursalDTO> result = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        IncidenciasSucursalDTO incidenciaDTO = result.get(0);
        assertEquals(1L, incidenciaDTO.getIdTicket());
        assertEquals("12345", incidenciaDTO.getFolioNumerico());
        assertEquals("SIN_SUSCRIPCION", incidenciaDTO.getTipoCliente()); // Corregido
        assertEquals("ACTIVO", incidenciaDTO.getEstadoTicket());
        assertEquals("ABC123", incidenciaDTO.getPlacaVehiculo());
        assertEquals("Corolla", incidenciaDTO.getModeloVehiculo());
        assertEquals("Blanco", incidenciaDTO.getColorVehiculo());
        assertEquals("Juan", incidenciaDTO.getNombrePropietario());
        assertEquals("12345678", incidenciaDTO.getTelefonoPropietario());

        // Verify incidencia details
        IncidenciasSucursalDTO.IncidenciasTicketDTO incidencia = incidenciaDTO.getIncidencias();
        assertEquals(1L, incidencia.getIdIncidencia());
        assertEquals("COMPROBANTE_PERDIDO", incidencia.getTipoIncidencia());
        assertEquals("Test incidencia", incidencia.getDescripcion());
        assertFalse(incidencia.isResuelto());

        // Verify evidencia details
        assertEquals(1, incidencia.getEvidencias().size());
        IncidenciasSucursalDTO.EvidenciasIncidenciaDTO evidencia = incidencia.getEvidencias().get(0);
        assertEquals(1L, evidencia.getIdEvidenciaIncidencia());
        assertEquals("FOTO_VEHICULO", evidencia.getTipoEvidencia()); // Corregido
        assertEquals("evidencia.jpg", evidencia.getNombreArchivo());
        assertEquals("http://localhost:8080/files/uploads/evidencia.jpg", evidencia.getUrlEvidencia());
        assertEquals("Evidencia de prueba", evidencia.getDescripcion());

        verify(usuarioRepository).findById(1L);
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(1L);
        verify(incidenciaTicketRepository).findByTicket_Sucursal_IdSucursal(1L);
        verify(evidenciaIncidenciaRepository).findByIncidencia_IdIncidencia(1L);
    }

    @Test
    void testObtenerIncidenciasSucursal_Success_S3Storage() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(incidenciaTicketSucursalService, "storageType", "s3");
        
        List<IncidenciaTicket> incidencias = Arrays.asList(mockIncidencia);
        List<EvidenciaIncidencia> evidencias = Arrays.asList(mockEvidencia);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(1L)).thenReturn(incidencias);
        when(evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(1L)).thenReturn(evidencias);

        // Act
        List<IncidenciasSucursalDTO> result = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        IncidenciasSucursalDTO incidenciaDTO = result.get(0);
        IncidenciasSucursalDTO.IncidenciasTicketDTO incidencia = incidenciaDTO.getIncidencias();
        IncidenciasSucursalDTO.EvidenciasIncidenciaDTO evidencia = incidencia.getEvidencias().get(0);
        
        // Verify S3 URL format
        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/uploads/evidencia.jpg", evidencia.getUrlEvidencia());
    }

    @Test
    void testObtenerIncidenciasSucursal_UsuarioNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            incidenciaTicketSucursalService.obtenerIncidenciasSucursal(999L);
        });

        assertEquals("Usuario no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testObtenerIncidenciasSucursal_SucursalNotFound() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);
        });

        assertEquals("Sucursal no encontrada para el usuario con ID: 1", exception.getMessage());
    }

    @Test
    void testObtenerIncidenciasSucursal_EmptyIncidencias() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(1L)).thenReturn(Arrays.asList());

        // Act
        List<IncidenciasSucursalDTO> result = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerIncidenciasSucursal_WithResolvedIncidencia() throws Exception {
        // Arrange
        mockIncidencia.setResuelto(true);
        mockIncidencia.setFechaResolucion(LocalDateTime.now());
        mockIncidencia.setResueltoPor(2L);
        mockIncidencia.setObservacionesResolucion("Resuelto por admin");

        List<IncidenciaTicket> incidencias = Arrays.asList(mockIncidencia);
        List<EvidenciaIncidencia> evidencias = Arrays.asList(mockEvidencia);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(1L)).thenReturn(incidencias);
        when(evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(1L)).thenReturn(evidencias);

        // Act
        List<IncidenciasSucursalDTO> result = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        IncidenciasSucursalDTO.IncidenciasTicketDTO incidencia = result.get(0).getIncidencias();
        assertTrue(incidencia.isResuelto());
        assertNotNull(incidencia.getFechaResolucion());
        assertEquals(2L, incidencia.getResueltoPor());
        assertEquals("Resuelto por admin", incidencia.getObservacionesResolucion());
    }

    @Test
    void testObtenerIncidenciasSucursal_MultipleIncidenciasAndEvidencias() throws Exception {
        // Arrange
        IncidenciaTicket incidencia2 = new IncidenciaTicket();
        incidencia2.setIdIncidencia(2L);
        incidencia2.setTicket(mockTicket);
        incidencia2.setTipoIncidencia(IncidenciaTicket.TipoIncidencia.VEHICULO_NO_RETIRADO);
        incidencia2.setDescripcion("Vehículo abandonado");
        incidencia2.setFechaIncidencia(LocalDateTime.now());
        incidencia2.setResuelto(false);

        EvidenciaIncidencia evidencia2 = new EvidenciaIncidencia();
        evidencia2.setIdEvidenciaIncidencia(2L);
        evidencia2.setIncidencia(mockIncidencia);
        evidencia2.setTipoEvidencia(EvidenciaIncidencia.TipoEvidencia.DOCUMENTO);
        evidencia2.setNombreArchivo("documento.pdf");
        evidencia2.setUrlEvidencia("uploads/documento.pdf");
        evidencia2.setDescripcion("Documento adicional");
        evidencia2.setFechaCarga(LocalDateTime.now());

        List<IncidenciaTicket> incidencias = Arrays.asList(mockIncidencia, incidencia2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);
        when(incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(1L)).thenReturn(incidencias);
        when(evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(1L)).thenReturn(Arrays.asList(mockEvidencia, evidencia2));
        when(evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(2L)).thenReturn(Arrays.asList());

        // Act
        List<IncidenciasSucursalDTO> result = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first incidencia has 2 evidencias
        IncidenciasSucursalDTO.IncidenciasTicketDTO firstIncidencia = result.get(0).getIncidencias();
        assertEquals(2, firstIncidencia.getEvidencias().size());

        // Verify second incidencia has no evidencias
        IncidenciasSucursalDTO.IncidenciasTicketDTO secondIncidencia = result.get(1).getIncidencias();
        assertEquals(0, secondIncidencia.getEvidencias().size());
        assertEquals("VEHICULO_NO_RETIRADO", secondIncidencia.getTipoIncidencia());
    }

    @Test
    void testCrearNuevaIncidenciaSucursal_AllTipoIncidencia() throws Exception {
        // Test all types of incidencias
        String[] tiposIncidencia = {"COMPROBANTE_PERDIDO", "FRAUDE", "VEHICULO_NO_RETIRADO", "OTRO"};
        
        for (String tipo : tiposIncidencia) {
            NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
            nuevaIncidenciaDTO.setIdTicket(1L);
            nuevaIncidenciaDTO.setTipoIncidencia(tipo);
            nuevaIncidenciaDTO.setDescripcion("Test " + tipo);
            nuevaIncidenciaDTO.setTipoEvidencia("FOTO_VEHICULO");
            nuevaIncidenciaDTO.setDescripcionEvidencia("Evidencia " + tipo);

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn("evidencia_" + tipo + ".jpg");
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
            when(fileStorageService.getUrl(multipartFile)).thenReturn("uploads/evidencia_" + tipo + ".jpg");
            
            // Mock para que cuando se guarde la incidencia, se setee el ID
            when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenAnswer(invocation -> {
                IncidenciaTicket incidencia = invocation.getArgument(0);
                incidencia.setIdIncidencia(1L); // Simular que la BD asigna el ID
                return incidencia;
            });
            
            when(evidenciaIncidenciaRepository.save(any(EvidenciaIncidencia.class))).thenReturn(mockEvidencia);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);
                assertEquals("Incidencia creada con exito con ID: 1", result);
            });
        }
    }

    @Test
    void testCrearNuevaIncidenciaSucursal_AllTipoEvidencia() throws Exception {
        // Test all types of evidencias
        String[] tiposEvidencia = {"FOTO_VEHICULO", "DOCUMENTO", "VIDEO", "OTRO"};
        
        for (String tipo : tiposEvidencia) {
            NuevaIncidenciaDTO nuevaIncidenciaDTO = new NuevaIncidenciaDTO();
            nuevaIncidenciaDTO.setIdTicket(1L);
            nuevaIncidenciaDTO.setTipoIncidencia("COMPROBANTE_PERDIDO");
            nuevaIncidenciaDTO.setDescripcion("Test evidencia");
            nuevaIncidenciaDTO.setTipoEvidencia(tipo);
            nuevaIncidenciaDTO.setDescripcionEvidencia("Evidencia tipo " + tipo);

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn("evidencia." + tipo.toLowerCase());
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
            when(fileStorageService.getUrl(multipartFile)).thenReturn("uploads/evidencia." + tipo.toLowerCase());
            
            // Mock para que cuando se guarde la incidencia, se setee el ID
            when(incidenciaTicketRepository.save(any(IncidenciaTicket.class))).thenAnswer(invocation -> {
                IncidenciaTicket incidencia = invocation.getArgument(0);
                incidencia.setIdIncidencia(1L); // Simular que la BD asigna el ID
                return incidencia;
            });
            
            when(evidenciaIncidenciaRepository.save(any(EvidenciaIncidencia.class))).thenReturn(mockEvidencia);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = incidenciaTicketSucursalService.crearNuevaIncidenciaSucursal(nuevaIncidenciaDTO, multipartFile);
                assertEquals("Incidencia creada con exito con ID: 1", result);
            });
        }
    }
}
