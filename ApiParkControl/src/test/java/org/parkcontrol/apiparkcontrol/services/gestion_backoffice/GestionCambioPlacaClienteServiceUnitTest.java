package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.DetalleSolicitudesCambioPlacaDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.SolicitudCambioPlacaDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.VehiculoClienteDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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
class GestionCambioPlacaClienteServiceUnitTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SolicitudCambioPlacaRepository solicitudCambioPlacaRepository;
    @Mock
    private EvidenciaCambioPlacaRepository evidenciaCambioPlacaRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private S3StorageService s3StorageService;
    @Mock
    private SuscripcionClienteService suscripcionClienteService;

    @InjectMocks
    private GestionCambioPlacaClienteService gestionCambioPlacaClienteService;

    private Suscripcion mockSuscripcion;
    private Vehiculo mockVehiculoActual;
    private Vehiculo mockVehiculoNuevo;
    private Usuario mockCliente;
    private SolicitudCambioPlaca mockSolicitud;
    private EvidenciaCambioPlaca mockEvidencia;
    private MockMultipartFile mockFile;
    private Empresa mockEmpresa;
    private TipoPlan mockTipoPlan;
    private TarifaBase mockTarifaBase;

    @BeforeEach
    void setUp() {
        // Configure service properties
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "S3_BUCKET_BACKEND", "test-bucket");
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "URL_BASE_LOCAL", "http://localhost:8080/files/");
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "region", "us-east-1");
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "storageType", "local");

        // Mock Cliente/Usuario
        mockCliente = new Usuario();
        mockCliente.setIdUsuario(1L);
        mockCliente.setNombreUsuario("testclient");

        // Mock Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setNit("1234567-8");

        // Mock TipoPlan
        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setPrecioPlan(300.00);

        // Mock TarifaBase
        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));

        // Mock Vehiculo Actual
        mockVehiculoActual = new Vehiculo();
        mockVehiculoActual.setId(1L);
        mockVehiculoActual.setPlaca("ABC123");
        mockVehiculoActual.setMarca("Toyota");
        mockVehiculoActual.setModelo("Corolla");
        mockVehiculoActual.setColor("Blanco");
        mockVehiculoActual.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);

        // Mock Vehiculo Nuevo
        mockVehiculoNuevo = new Vehiculo();
        mockVehiculoNuevo.setId(2L);
        mockVehiculoNuevo.setPlaca("XYZ789");
        mockVehiculoNuevo.setMarca("Honda");
        mockVehiculoNuevo.setModelo("Civic");
        mockVehiculoNuevo.setColor("Negro");
        mockVehiculoNuevo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);

        // Mock Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockCliente);
        mockSuscripcion.setVehiculo(mockVehiculoActual);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setTarifaBaseReferencia(mockTarifaBase);
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        mockSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        mockSuscripcion.setDescuentoAplicado(new BigDecimal("10.00"));
        mockSuscripcion.setPrecioPlan(new BigDecimal("270.00"));
        mockSuscripcion.setHorasMensualesIncluidas(160);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("50.5"));
        mockSuscripcion.setFechaInicio(LocalDateTime.now().minusDays(30));
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusDays(330));
        mockSuscripcion.setFechaCompra(LocalDateTime.now().minusDays(30));

        // Mock SolicitudCambioPlaca
        mockSolicitud = new SolicitudCambioPlaca();
        mockSolicitud.setId(1L);
        mockSolicitud.setSuscripcion(mockSuscripcion);
        mockSolicitud.setVehiculoActual(mockVehiculoActual);
        mockSolicitud.setPlacaNueva("XYZ789");
        mockSolicitud.setMotivo(SolicitudCambioPlaca.Motivo.VENTA);
        mockSolicitud.setDescripcionMotivo("Venta del vehículo actual");
        mockSolicitud.setFechaSolicitud(LocalDateTime.now());
        mockSolicitud.setEstado(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE);

        // Mock EvidenciaCambioPlaca
        mockEvidencia = new EvidenciaCambioPlaca();
        mockEvidencia.setId(1L);
        mockEvidencia.setSolicitudCambioPlac(mockSolicitud);
        mockEvidencia.setTipoDocumento(EvidenciaCambioPlaca.TipoDocumento.TRASPASO);
        mockEvidencia.setNombreArchivo("factura.pdf");
        mockEvidencia.setUrlDocumento("uploads/factura.pdf");
        mockEvidencia.setDescripcion("Factura de compra del nuevo vehículo");
        mockEvidencia.setFechaCarga(LocalDateTime.now());

        // Mock MultipartFile
        mockFile = new MockMultipartFile(
                "evidencia", "factura.pdf", "application/pdf", "contenido del archivo".getBytes());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_Success() throws Exception {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(1L);
        solicitudDTO.setIdVehiculoActual(1L);
        solicitudDTO.setIdCliente(1L);
        solicitudDTO.setPlacaNueva("XYZ789");
        solicitudDTO.setMotivo("VENTA");
        solicitudDTO.setDescripcionMotivo("Venta del vehículo actual");
        solicitudDTO.setTipoDocumento("TRASPASO");
        solicitudDTO.setDescripcionEvidencia("Documento de traspaso del nuevo vehículo");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.APROBADA))
                .thenReturn(Arrays.asList());
        
        // CRITICAL: Mock save() to return the object WITH the ID set
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenAnswer(invocation -> {
            SolicitudCambioPlaca solicitud = invocation.getArgument(0);
            solicitud.setId(1L); // Simulate database setting the ID
            return solicitud;
        });
        
        when(fileStorageService.getUrl(mockFile)).thenReturn("uploads/factura.pdf");
        when(evidenciaCambioPlacaRepository.save(any(EvidenciaCambioPlaca.class))).thenReturn(mockEvidencia);

        // Act
        String result = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

        // Assert
        assertEquals("Solicitud de cambio de placa creada con éxito con ID: 1", result);
        verify(suscripcionRepository).findById(1L);
        verify(vehiculoRepository).findById(1L);
        verify(vehiculoRepository).findByPlaca("XYZ789");
        verify(solicitudCambioPlacaRepository).save(any(SolicitudCambioPlaca.class));
        verify(evidenciaCambioPlacaRepository).save(any(EvidenciaCambioPlaca.class));
        verify(fileStorageService).getUrl(mockFile);
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_S3Storage_Success() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "storageType", "s3");
        
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.APROBADA))
                .thenReturn(Arrays.asList());
        
        // CRITICAL: Mock save() to return the object WITH the ID set  
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenAnswer(invocation -> {
            SolicitudCambioPlaca solicitud = invocation.getArgument(0);
            solicitud.setId(1L); // Simulate database setting the ID
            return solicitud;
        });
        
        when(s3StorageService.uploadToS3(mockFile)).thenReturn("s3-key/factura.pdf");
        when(evidenciaCambioPlacaRepository.save(any(EvidenciaCambioPlaca.class))).thenReturn(mockEvidencia);

        // Act
        String result = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

        // Assert
        assertEquals("Solicitud de cambio de placa creada con éxito con ID: 1", result);
        verify(s3StorageService).uploadToS3(mockFile);
        verify(fileStorageService, never()).getUrl(any());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_SuscripcionNotFound() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("Suscripción no encontrada con ID: 1", exception.getMessage());
        verify(suscripcionRepository).findById(1L);
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_SuscripcionInactiva() {
        // Arrange
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("La suscripción no está activa. No se puede procesar la solicitud de cambio de placa.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_VehiculoActualNotFound() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("Vehículo no encontrado con ID: 1", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_VehiculoNoPerteneceSuscripcion() {
        // Arrange
        Vehiculo otroVehiculo = new Vehiculo();
        otroVehiculo.setId(999L);
        mockSuscripcion.setVehiculo(otroVehiculo);

        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("El vehículo no pertenece a la suscripción indicada.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_NuevaPlacaNoExiste() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("No existe un vehículo registrado con la nueva placa proporcionada.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_MismaPlaca() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        solicitudDTO.setPlacaNueva("ABC123"); // Same as current

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("ABC123")).thenReturn(mockVehiculoActual);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("La nueva placa no puede ser la misma que la placa actual.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_NuevaPlacaYaTieneSuscripcion() {
        // Arrange
        Suscripcion otraSuscripcion = new Suscripcion();
        otraSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList(otraSuscripcion));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("La nueva placa ya pertenece a otra suscripción activa.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_SolicitudPendienteExiste() {
        // Arrange
        SolicitudCambioPlaca solicitudPendiente = new SolicitudCambioPlaca();
        
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList(solicitudPendiente));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("Ya existe una solicitud de cambio de placa pendiente para esta suscripción.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_CambioRecienteEnSeisMeses() {
        // Arrange
        SolicitudCambioPlaca solicitudReciente = new SolicitudCambioPlaca();
        solicitudReciente.setFechaEfectiva(LocalDateTime.now().minusMonths(3)); // 3 months ago

        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.APROBADA))
                .thenReturn(Arrays.asList(solicitudReciente));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("El cliente ya ha realizado un cambio de placa en los últimos 6 meses.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_ArchivoVacio() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();
        MockMultipartFile emptyFile = new MockMultipartFile("evidencia", "", "application/pdf", new byte[0]);

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.APROBADA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenReturn(mockSolicitud);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, emptyFile);
        });

        assertEquals("El archivo de evidencia no puede estar vacio", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_ArchivoNull() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = createBasicSolicitudDTO();

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculoActual));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(suscripcionRepository.findByVehiculo_IdAndEstado(2L, Suscripcion.EstadoSuscripcion.ACTIVA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_IdAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuarioAndEstado(1L, SolicitudCambioPlaca.EstadoSolicitud.APROBADA))
                .thenReturn(Arrays.asList());
        when(solicitudCambioPlacaRepository.save(any(SolicitudCambioPlaca.class))).thenReturn(mockSolicitud);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, null);
        });

        assertEquals("El archivo de evidencia no puede estar vacio", exception.getMessage());
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_Success() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(1L))
                .thenReturn(Arrays.asList(mockSolicitud));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(evidenciaCambioPlacaRepository.findBySolicitudCambioPlac_Id(1L)).thenReturn(mockEvidencia);
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSolicitudesCambioPlacaDTO solicitudDTO = result.get(0);
        assertEquals(1L, solicitudDTO.getIdSolicitudCambio());
        assertEquals("XYZ789", solicitudDTO.getPlacaNueva());
        assertEquals("VENTA", solicitudDTO.getMotivo());
        assertEquals("Venta del vehículo actual", solicitudDTO.getDescripcionMotivo());
        assertEquals("PENDIENTE", solicitudDTO.getEstado());

        // Verify vehiculo actual
        VehiculoClienteDTO vehiculoActual = solicitudDTO.getVehiculoActual();
        assertEquals(1L, vehiculoActual.getIdVehiculo());
        assertEquals("ABC123", vehiculoActual.getPlaca());
        assertEquals("Toyota", vehiculoActual.getMarca());

        // Verify vehiculo nuevo
        VehiculoClienteDTO vehiculoNuevo = solicitudDTO.getVehiculoNuevo();
        assertEquals(2L, vehiculoNuevo.getIdVehiculo());
        assertEquals("XYZ789", vehiculoNuevo.getPlaca());
        assertEquals("Honda", vehiculoNuevo.getMarca());

        // Verify evidencia
        DetalleSolicitudesCambioPlacaDTO.DetalleEvidenciaCambioPlacaDTO evidencia = solicitudDTO.getEvidenciaCambioPlaca();
        assertEquals(1L, evidencia.getIdEvidencia());
        assertEquals("TRASPASO", evidencia.getTipoDocumento());
        assertEquals("factura.pdf", evidencia.getNombreArchivo());
        assertTrue(evidencia.getUrlDocumento().contains("uploads/factura.pdf"));

        verify(usuarioRepository).findById(1L);
        verify(solicitudCambioPlacaRepository).findBySuscripcion_Usuario_IdUsuario(1L);
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_S3Storage() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(gestionCambioPlacaClienteService, "storageType", "s3");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(1L))
                .thenReturn(Arrays.asList(mockSolicitud));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(evidenciaCambioPlacaRepository.findBySolicitudCambioPlac_Id(1L)).thenReturn(mockEvidencia);
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(1L);

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesCambioPlacaDTO.DetalleEvidenciaCambioPlacaDTO evidencia = result.get(0).getEvidenciaCambioPlaca();
        assertTrue(evidencia.getUrlDocumento().contains("test-bucket.s3.us-east-1.amazonaws.com"));
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_ClienteNotFound() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(999L);
        });

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());
        verify(usuarioRepository).findById(999L);
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_EmptyList() throws Exception {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_WithFechaRevisionAndEfectiva() throws Exception {
        // Arrange
        mockSolicitud.setFechaRevision(LocalDateTime.now().minusDays(1));
        mockSolicitud.setFechaEfectiva(LocalDateTime.now());
        mockSolicitud.setObservacionesRevision("Solicitud aprobada");
        mockSolicitud.setEstado(SolicitudCambioPlaca.EstadoSolicitud.APROBADA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        when(solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(1L))
                .thenReturn(Arrays.asList(mockSolicitud));
        when(vehiculoRepository.findByPlaca("XYZ789")).thenReturn(mockVehiculoNuevo);
        when(evidenciaCambioPlacaRepository.findBySolicitudCambioPlac_Id(1L)).thenReturn(mockEvidencia);
        when(suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa))
                .thenReturn(createMockEmpresaSuscripcionesDTO());

        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(1L);

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesCambioPlacaDTO solicitudDTO = result.get(0);
        assertEquals("APROBADA", solicitudDTO.getEstado());
        assertNotNull(solicitudDTO.getFechaRevision());
        assertNotNull(solicitudDTO.getFechaEfectiva());
        assertEquals("Solicitud aprobada", solicitudDTO.getObservacionesRevision());
    }

    // Helper methods
    private SolicitudCambioPlacaDTO createBasicSolicitudDTO() {
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(1L);
        solicitudDTO.setIdVehiculoActual(1L);
        solicitudDTO.setIdCliente(1L);
        solicitudDTO.setPlacaNueva("XYZ789");
        solicitudDTO.setMotivo("VENTA");
        solicitudDTO.setDescripcionMotivo("Venta del vehículo actual");
        solicitudDTO.setTipoDocumento("TRASPASO");
        solicitudDTO.setDescripcionEvidencia("Documento de traspaso del nuevo vehículo");
        return solicitudDTO;
    }

    private org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.PlanesSuscripcionDTO.EmpresaSuscripcionesDTO createMockEmpresaSuscripcionesDTO() {
        // Create a simple mock for the empresa suscripciones DTO
        return org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.PlanesSuscripcionDTO.EmpresaSuscripcionesDTO.builder()
                .idEmpresa(1L)
                .nombreComercial("Test Company")
                .nit("1234567-8")
                .razonSocial("Test Company S.A.")
                .telefonoContacto("12345678")
                .direccionFiscal("Test Address")
                .sucursales(Arrays.asList())
                .suscripciones(Arrays.asList())
                .build();
    }
}
