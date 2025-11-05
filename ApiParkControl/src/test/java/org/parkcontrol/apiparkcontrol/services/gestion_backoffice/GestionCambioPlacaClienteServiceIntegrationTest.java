package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.DetalleSolicitudesCambioPlacaDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.SolicitudCambioPlacaDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class GestionCambioPlacaClienteServiceIntegrationTest {

    @Autowired
    private GestionCambioPlacaClienteService gestionCambioPlacaClienteService;

    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SolicitudCambioPlacaRepository solicitudCambioPlacaRepository;
    @Autowired
    private EvidenciaCambioPlacaRepository evidenciaCambioPlacaRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;

    @MockBean
    private FileStorageService fileStorageService;
    @MockBean
    private S3StorageService s3StorageService;
    @MockBean
    private SuscripcionClienteService suscripcionClienteService;

    private Suscripcion testSuscripcion;
    private Vehiculo testVehiculoActual;
    private Vehiculo testVehiculoNuevo;
    private Usuario testCliente;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TipoPlan testTipoPlan;
    private TarifaBase testTarifaBase;

    @BeforeEach
    void setUp() {
        // Clean up database
        evidenciaCambioPlacaRepository.deleteAll();
        solicitudCambioPlacaRepository.deleteAll();
        suscripcionRepository.deleteAll();
        vehiculoRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        Rol clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Usuario cliente");
        clienteRol = rolRepository.save(clienteRol);

        // Create test persona
        Persona testPersona = new Persona();
        testPersona.setNombre("Juan");
        testPersona.setApellido("Test");
        testPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("juan@test.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Test Address");
        testPersona.setCiudad("Test City");
        testPersona.setPais("Guatemala");
        testPersona.setCodigoPostal("01001");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        // Create test client
        testCliente = new Usuario();
        testCliente.setPersona(testPersona);
        testCliente.setRol(clienteRol);
        testCliente.setNombreUsuario("testclient");
        testCliente.setContraseniaHash("hashedPassword");
        testCliente.setDobleFactorHabilitado(false);
        testCliente.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testCliente.setDebeCambiarContrasenia(false);
        testCliente.setEsPrimeraVez(false);
        testCliente.setIntentosFallidos(0);
        testCliente = usuarioRepository.save(testCliente);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testCliente);
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("test@company.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testCliente);
        testSucursal.setNombre("Sucursal Test");
        testSucursal.setDireccionCompleta("Test Address");
        testSucursal.setCiudad("Test City");
        testSucursal.setDepartamento("Test Department");
        testSucursal.setLatitud(new BigDecimal("14.6349"));
        testSucursal.setLongitud(new BigDecimal("-90.5069"));
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(100);
        testSucursal.setTelefonoContacto("12345678");
        testSucursal.setCorreoContacto("sucursal@test.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);

        // Create test tipo plan
        testTipoPlan = new TipoPlan();
        testTipoPlan.setEmpresa(testEmpresa);
        testTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        testTipoPlan.setCodigoPlan("WW-001");
        testTipoPlan.setDescripcion("Plan Workweek");
        testTipoPlan.setPrecioPlan(300.00);
        testTipoPlan.setHorasDia(8);
        testTipoPlan.setHorasMensuales(160);
        testTipoPlan.setDiasAplicables("L-M-X-J-V");
        testTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        testTipoPlan.setOrdenBeneficio(2);
        testTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        testTipoPlan = tipoPlanRepository.save(testTipoPlan);

        // Create test tarifa base
        testTarifaBase = new TarifaBase();
        testTarifaBase.setEmpresa(testEmpresa);
        testTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        testTarifaBase.setMoneda("GTQ");
        testTarifaBase.setFechaVigenciaInicio(LocalDate.from(LocalDateTime.now().minusDays(30)));
        testTarifaBase.setFechaVigenciaFin(LocalDate.from(LocalDateTime.now().plusDays(330)));
        testTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        testTarifaBase = tarifaBaseRepository.save(testTarifaBase);

        // Create test vehiculos
        testVehiculoActual = new Vehiculo();
        testVehiculoActual.setPropietario(testPersona);
        testVehiculoActual.setPlaca("ABC123");
        testVehiculoActual.setMarca("Toyota");
        testVehiculoActual.setModelo("Corolla");
        testVehiculoActual.setColor("Blanco");
        testVehiculoActual.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculoActual.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculoActual = vehiculoRepository.save(testVehiculoActual);

        testVehiculoNuevo = new Vehiculo();
        testVehiculoNuevo.setPropietario(testPersona);
        testVehiculoNuevo.setPlaca("XYZ789");
        testVehiculoNuevo.setMarca("Honda");
        testVehiculoNuevo.setModelo("Civic");
        testVehiculoNuevo.setColor("Negro");
        testVehiculoNuevo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculoNuevo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculoNuevo = vehiculoRepository.save(testVehiculoNuevo);

        // Create test suscripcion
        testSuscripcion = new Suscripcion();
        testSuscripcion.setEmpresa(testEmpresa);
        testSuscripcion.setUsuario(testCliente);
        testSuscripcion.setVehiculo(testVehiculoActual);
        testSuscripcion.setTipoPlan(testTipoPlan);
        testSuscripcion.setTarifaBaseReferencia(testTarifaBase);
        testSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        testSuscripcion.setDescuentoAplicado(new BigDecimal("10.00"));
        testSuscripcion.setPrecioPlan(new BigDecimal("270.00"));
        testSuscripcion.setHorasMensualesIncluidas(160);
        testSuscripcion.setHorasConsumidas(new BigDecimal("50.5"));
        testSuscripcion.setFechaInicio(LocalDateTime.now().minusDays(30));
        testSuscripcion.setFechaFin(LocalDateTime.now().plusDays(330));
        testSuscripcion.setFechaCompra(LocalDateTime.now().minusDays(30));
        testSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        testSuscripcion.setMetodoPago("TARJETA_CREDITO");
        testSuscripcion.setNumeroTransaccion("TXN123456");
        testSuscripcion = suscripcionRepository.save(testSuscripcion);

        // Configure mocks for file storage
        try {
            lenient().when(fileStorageService.getUrl(any())).thenReturn("uploads/test-file.pdf");
            lenient().when(s3StorageService.uploadToS3(any())).thenReturn("s3-key/test-file.pdf");
        } catch (Exception e) {
            // Handle any exceptions from mocking
        }

        // Mock suscripcion client service
        lenient().when(suscripcionClienteService.obtenerEmpresaSuscripciones(any()))
                .thenReturn(createMockEmpresaSuscripcionesDTO());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_Integration() throws Exception {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("XYZ789");
        solicitudDTO.setMotivo("VENTA");
        solicitudDTO.setDescripcionMotivo("Venta del vehículo actual");
        solicitudDTO.setTipoDocumento("TRASPASO");
        solicitudDTO.setDescripcionEvidencia("Documento de traspaso del nuevo vehículo");

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "traspaso.pdf", "application/pdf", "contenido del archivo".getBytes());

        // Act
        String result = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

        // Assert
        assertTrue(result.contains("Solicitud de cambio de placa creada con éxito con ID:"));

        // Verify in database
        List<SolicitudCambioPlaca> solicitudes = solicitudCambioPlacaRepository.findBySuscripcion_Usuario_IdUsuario(testCliente.getIdUsuario());
        assertEquals(1, solicitudes.size());

        SolicitudCambioPlaca solicitud = solicitudes.get(0);
        assertEquals(testSuscripcion.getId(), solicitud.getSuscripcion().getId());
        assertEquals(testVehiculoActual.getId(), solicitud.getVehiculoActual().getId());
        assertEquals("XYZ789", solicitud.getPlacaNueva());
        assertEquals(SolicitudCambioPlaca.Motivo.VENTA, solicitud.getMotivo());
        assertEquals("Venta del vehículo actual", solicitud.getDescripcionMotivo());
        assertEquals(SolicitudCambioPlaca.EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertNotNull(solicitud.getFechaSolicitud());

        // Verify evidencia
        EvidenciaCambioPlaca evidencia = evidenciaCambioPlacaRepository.findBySolicitudCambioPlac_Id(solicitud.getId());
        assertNotNull(evidencia);
        assertEquals(EvidenciaCambioPlaca.TipoDocumento.TRASPASO, evidencia.getTipoDocumento());
        assertEquals("traspaso.pdf", evidencia.getNombreArchivo());
        assertNotNull(evidencia.getUrlDocumento());
        assertEquals("Documento de traspaso del nuevo vehículo", evidencia.getDescripcion());
        assertNotNull(evidencia.getFechaCarga());
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_Integration() throws Exception {
        // Arrange - First create a solicitud
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("XYZ789");
        solicitudDTO.setMotivo("VENTA");
        solicitudDTO.setDescripcionMotivo("Venta del vehículo actual");
        solicitudDTO.setTipoDocumento("TRASPASO");
        solicitudDTO.setDescripcionEvidencia("Factura de compra");

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "factura.pdf", "application/pdf", "contenido".getBytes());

        gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSolicitudesCambioPlacaDTO solicitudResult = result.get(0);
        assertNotNull(solicitudResult.getIdSolicitudCambio());
        assertEquals("XYZ789", solicitudResult.getPlacaNueva());
        assertEquals("VENTA", solicitudResult.getMotivo());
        assertEquals("Venta del vehículo actual", solicitudResult.getDescripcionMotivo());
        assertEquals("PENDIENTE", solicitudResult.getEstado());
        assertNotNull(solicitudResult.getFechaSolicitud());

        // Verify vehiculo details
        assertNotNull(solicitudResult.getVehiculoActual());
        assertEquals("ABC123", solicitudResult.getVehiculoActual().getPlaca());
        assertEquals("Toyota", solicitudResult.getVehiculoActual().getMarca());

        assertNotNull(solicitudResult.getVehiculoNuevo());
        assertEquals("XYZ789", solicitudResult.getVehiculoNuevo().getPlaca());
        assertEquals("Honda", solicitudResult.getVehiculoNuevo().getMarca());

        // Verify suscripcion details
        assertNotNull(solicitudResult.getSuscripcionCliente());
        assertEquals(testSuscripcion.getId(), solicitudResult.getSuscripcionCliente().getIdSuscripcion());
        assertEquals("MENSUAL", solicitudResult.getSuscripcionCliente().getPeriodoContratado());

        // Verify evidencia
        assertNotNull(solicitudResult.getEvidenciaCambioPlaca());
        assertEquals("TRASPASO", solicitudResult.getEvidenciaCambioPlaca().getTipoDocumento());
        assertEquals("factura.pdf", solicitudResult.getEvidenciaCambioPlaca().getNombreArchivo());
        assertNotNull(solicitudResult.getEvidenciaCambioPlaca().getUrlDocumento());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_SuscripcionNotFound_Integration() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(999L); // Non-existent

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "test.pdf", "application/pdf", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("Suscripción no encontrada con ID: 999", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_VehiculoNotFound_Integration() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(999L); // Non-existent

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "test.pdf", "application/pdf", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("Vehículo no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_SuscripcionInactiva_Integration() {
        // Arrange
        testSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
        suscripcionRepository.save(testSuscripcion);

        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("XYZ789");

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "test.pdf", "application/pdf", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("La suscripción no está activa. No se puede procesar la solicitud de cambio de placa.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_PlacaNuevaNoExiste_Integration() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("NOEXISTE"); // Non-existent plate

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "test.pdf", "application/pdf", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("No existe un vehículo registrado con la nueva placa proporcionada.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_MismaPlaca_Integration() {
        // Arrange
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("ABC123"); // Same plate as current vehicle

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "test.pdf", "application/pdf", "test".getBytes());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        });

        assertEquals("La nueva placa no puede ser la misma que la placa actual.", exception.getMessage());
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_AllTipoDocumento_Integration() throws Exception {
        // Test all document types
        String[] tiposDocumento = {"DENUNCIA", "TRASPASO", "TARJETA_CIRCULACION", "IDENTIFICACION", "OTRO"};

        for (int i = 0; i < tiposDocumento.length; i++) {
            // Clean previous solicitudes for new test
            evidenciaCambioPlacaRepository.deleteAll();
            solicitudCambioPlacaRepository.deleteAll();

            // Create new vehicle for each test to avoid same plate issue
            Vehiculo nuevoVehiculo = new Vehiculo();
            nuevoVehiculo.setPropietario(testVehiculoNuevo.getPropietario());
            nuevoVehiculo.setPlaca("DOC" + i + "123");
            nuevoVehiculo.setMarca("Test");
            nuevoVehiculo.setModelo("Test Model");
            nuevoVehiculo.setColor("Test Color");
            nuevoVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
            nuevoVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
            nuevoVehiculo = vehiculoRepository.save(nuevoVehiculo);

            SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
            solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
            solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
            solicitudDTO.setIdCliente(testCliente.getIdUsuario());
            solicitudDTO.setPlacaNueva("DOC" + i + "123");
            solicitudDTO.setMotivo("VENTA");
            solicitudDTO.setDescripcionMotivo("Test " + tiposDocumento[i]);
            solicitudDTO.setTipoDocumento(tiposDocumento[i]);
            solicitudDTO.setDescripcionEvidencia("Evidencia " + tiposDocumento[i]);

            MockMultipartFile mockFile = new MockMultipartFile(
                    "evidencia", "test_" + i + ".pdf", "application/pdf", ("test " + tiposDocumento[i]).getBytes());

            // Act
            String result = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

            // Assert
            assertTrue(result.contains("Solicitud de cambio de placa creada con éxito"));

            // Verify document type in database
            List<EvidenciaCambioPlaca> evidencias = evidenciaCambioPlacaRepository.findAll();
            assertEquals(1, evidencias.size());
            assertEquals(EvidenciaCambioPlaca.TipoDocumento.valueOf(tiposDocumento[i]), evidencias.get(0).getTipoDocumento());
        }
    }

    @Test
    void testCrearNuevaSolicitudCambioPlaca_AllTipoMotivo_Integration() throws Exception {
        // Test all motivo types
        String[] tiposMotivo = {"VENTA", "ROBO", "SINIESTRO", "OTRO"};

        for (int i = 0; i < tiposMotivo.length; i++) {
            // Clean previous solicitudes for new test
            evidenciaCambioPlacaRepository.deleteAll();
            solicitudCambioPlacaRepository.deleteAll();

            // Create new vehicle for each test
            Vehiculo nuevoVehiculo = new Vehiculo();
            nuevoVehiculo.setPropietario(testVehiculoNuevo.getPropietario());
            nuevoVehiculo.setPlaca("MOT" + i + "123");
            nuevoVehiculo.setMarca("Test");
            nuevoVehiculo.setModelo("Test Model");
            nuevoVehiculo.setColor("Test Color");
            nuevoVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
            nuevoVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
            nuevoVehiculo = vehiculoRepository.save(nuevoVehiculo);

            SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
            solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
            solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
            solicitudDTO.setIdCliente(testCliente.getIdUsuario());
            solicitudDTO.setPlacaNueva("MOT" + i + "123");
            solicitudDTO.setMotivo(tiposMotivo[i]);
            solicitudDTO.setDescripcionMotivo("Motivo: " + tiposMotivo[i]);
            solicitudDTO.setTipoDocumento("TRASPASO");
            solicitudDTO.setDescripcionEvidencia("Evidencia para " + tiposMotivo[i]);

            MockMultipartFile mockFile = new MockMultipartFile(
                    "evidencia", "motivo_" + i + ".pdf", "application/pdf", ("test " + tiposMotivo[i]).getBytes());

            // Act
            String result = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);

            // Assert
            assertTrue(result.contains("Solicitud de cambio de placa creada con éxito"));

            // Verify motivo type in database
            List<SolicitudCambioPlaca> solicitudes = solicitudCambioPlacaRepository.findAll();
            assertEquals(1, solicitudes.size());
            assertEquals(SolicitudCambioPlaca.Motivo.valueOf(tiposMotivo[i]), solicitudes.get(0).getMotivo());
        }
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_ClienteNotFound_Integration() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(999L);
        });

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());
    }

    @Test
    void testObtenerSolicitudesCambioPlacaCliente_EmptyList_Integration() throws Exception {
        // Act
        List<DetalleSolicitudesCambioPlacaDTO> result = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCompleteWorkflow_Integration() throws Exception {
        // Test complete workflow from creation to retrieval
        
        // Step 1: Create solicitud
        SolicitudCambioPlacaDTO solicitudDTO = new SolicitudCambioPlacaDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setIdVehiculoActual(testVehiculoActual.getId());
        solicitudDTO.setIdCliente(testCliente.getIdUsuario());
        solicitudDTO.setPlacaNueva("XYZ789");
        solicitudDTO.setMotivo("VENTA");
        solicitudDTO.setDescripcionMotivo("Venta completa del vehículo actual");
        solicitudDTO.setTipoDocumento("TRASPASO");
        solicitudDTO.setDescripcionEvidencia("Documento oficial de traspaso");

        MockMultipartFile mockFile = new MockMultipartFile(
                "evidencia", "traspaso_oficial.pdf", "application/pdf", "contenido traspaso oficial".getBytes());

        // Step 2: Create solicitud
        String createResult = gestionCambioPlacaClienteService.crearNuevaSolicitudCambioPlaca(solicitudDTO, mockFile);
        assertTrue(createResult.contains("Solicitud de cambio de placa creada con éxito"));

        // Step 3: Retrieve solicitudes
        List<DetalleSolicitudesCambioPlacaDTO> solicitudes = gestionCambioPlacaClienteService.obtenerSolicitudesCambioPlacaCliente(testCliente.getIdUsuario());

        // Step 4: Verify complete workflow
        assertEquals(1, solicitudes.size());
        DetalleSolicitudesCambioPlacaDTO solicitudResult = solicitudes.get(0);

        // Verify all fields are populated correctly
        assertNotNull(solicitudResult.getIdSolicitudCambio());
        assertEquals("XYZ789", solicitudResult.getPlacaNueva());
        assertEquals("VENTA", solicitudResult.getMotivo());
        assertEquals("Venta completa del vehículo actual", solicitudResult.getDescripcionMotivo());
        assertEquals("PENDIENTE", solicitudResult.getEstado());

        // Verify relationships work correctly
        assertNotNull(solicitudResult.getVehiculoActual());
        assertNotNull(solicitudResult.getVehiculoNuevo());
        assertNotNull(solicitudResult.getSuscripcionCliente());
        assertNotNull(solicitudResult.getEvidenciaCambioPlaca());

        // Verify data integrity
        assertEquals(testVehiculoActual.getPlaca(), solicitudResult.getVehiculoActual().getPlaca());
        assertEquals(testVehiculoNuevo.getPlaca(), solicitudResult.getVehiculoNuevo().getPlaca());
        assertEquals("traspaso_oficial.pdf", solicitudResult.getEvidenciaCambioPlaca().getNombreArchivo());
        assertEquals("TRASPASO", solicitudResult.getEvidenciaCambioPlaca().getTipoDocumento());
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }

    private org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.PlanesSuscripcionDTO.EmpresaSuscripcionesDTO createMockEmpresaSuscripcionesDTO() {
        return org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.PlanesSuscripcionDTO.EmpresaSuscripcionesDTO.builder()
                .idEmpresa(testEmpresa.getIdEmpresa())
                .nombreComercial(testEmpresa.getNombreComercial())
                .nit(testEmpresa.getNit())
                .razonSocial(testEmpresa.getRazonSocial())
                .telefonoContacto(testEmpresa.getTelefonoPrincipal())
                .direccionFiscal(testEmpresa.getDireccionFiscal())
                .sucursales(Arrays.asList())
                .suscripciones(Arrays.asList())
                .build();
    }
}
