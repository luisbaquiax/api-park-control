package org.parkcontrol.apiparkcontrol.services.gestion_backoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.DetalleSolicitudesTemporalDTO;
import org.parkcontrol.apiparkcontrol.dto.gestion_backoffice.SolicitarPermisoTemporalDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SolicitudTemporalClienteServiceIntegrationTest {

    @Autowired
    private SolicitudTemporalClienteService solicitudTemporalClienteService;

    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private PermisoTemporalRepository permisoTemporalRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
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
    private SuscripcionClienteService suscripcionClienteService;

    private Suscripcion testSuscripcion;
    private Vehiculo testVehiculoTemporal;
    private Usuario testCliente;
    private Persona testPersona; // Add this field
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TipoPlan testTipoPlan;
    private TarifaBase testTarifaBase;

    @BeforeEach
    void setUp() {
        // Clean up database
        permisoTemporalRepository.deleteAll();
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
        testPersona = new Persona(); // Now using the class field
        testPersona.setNombre("Juan");
        testPersona.setApellido("Test Cliente");
        testPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("juan.cliente@test.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Cliente Address");
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
        testSucursal.setDireccionCompleta("Sucursal Address");
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

        // Create test vehiculo temporal
        testVehiculoTemporal = new Vehiculo();
        testVehiculoTemporal.setPropietario(testPersona);
        testVehiculoTemporal.setPlaca("TMP789");
        testVehiculoTemporal.setMarca("Honda");
        testVehiculoTemporal.setModelo("Civic");
        testVehiculoTemporal.setColor("Azul");
        testVehiculoTemporal.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculoTemporal.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculoTemporal = vehiculoRepository.save(testVehiculoTemporal);

        // Create test suscripcion
        testSuscripcion = new Suscripcion();
        testSuscripcion.setEmpresa(testEmpresa);
        testSuscripcion.setUsuario(testCliente);
        testSuscripcion.setVehiculo(testVehiculoTemporal); // Use the temporal vehicle as base
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

        // Mock suscripcion client service
        lenient().when(suscripcionClienteService.obtenerEmpresaSuscripciones(any()))
                .thenReturn(createMockEmpresaSuscripcionesDTO());
    }

    @Test
    void testSolicitarPermisoTemporal_Integration() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setPlacaTemporal("TMP789");
        solicitudDTO.setTipoVehiculoPermitido("CUATRO_RUEDAS");
        solicitudDTO.setMotivo("Vehículo principal en mantenimiento");

        // Act
        String result = solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

        // Assert
        assertEquals("Solicitud de permiso temporal creada exitosamente", result);

        // Verify in database
        List<PermisoTemporal> permisos = permisoTemporalRepository.findBySuscripcion_Id(testSuscripcion.getId());
        assertEquals(1, permisos.size());

        PermisoTemporal permiso = permisos.get(0);
        assertEquals(testSuscripcion.getId(), permiso.getSuscripcion().getId());
        assertEquals("TMP789", permiso.getPlacaTemporal());
        assertEquals(PermisoTemporal.TipoVehiculo.CUATRO_RUEDAS, permiso.getTipoVehiculoPermitido());
        assertEquals("Vehículo principal en mantenimiento", permiso.getMotivo());
        assertEquals(0, permiso.getUsosRealizados());
        assertEquals(PermisoTemporal.EstadoPermiso.PENDIENTE, permiso.getEstado());
    }

    @Test
    void testSolicitarPermisoTemporal_SuscripcionNotFound_Integration() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(999L); // Non-existent

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("Suscripcion no encontrada", exception.getMessage());
    }

    @Test
    void testSolicitarPermisoTemporal_SuscripcionInactiva_Integration() {
        // Arrange
        testSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
        suscripcionRepository.save(testSuscripcion);

        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setPlacaTemporal("TMP789");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("La suscripcion no esta activa", exception.getMessage());
    }

    @Test
    void testSolicitarPermisoTemporal_PlacaNoRegistrada_Integration() {
        // Arrange
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setPlacaTemporal("NOEXISTE");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
        });

        assertEquals("La placa temporal no esta registrada en el sistema", exception.getMessage());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_Integration() {
        // Arrange - Create a permiso temporal first
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setPlacaTemporal("TMP789");
        solicitudDTO.setTipoVehiculoPermitido("CUATRO_RUEDAS");
        solicitudDTO.setMotivo("Vehículo de remplazo temporal");

        solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        assertNotNull(detalleDTO.getIdPermisoTemporal());
        assertEquals("TMP789", detalleDTO.getPlacaTemporal());
        assertEquals("CUATRO_RUEDAS", detalleDTO.getTipoVehiculoPermitido());
        assertEquals("Vehículo de remplazo temporal", detalleDTO.getMotivo());
        assertEquals(0, detalleDTO.getUsosRealizados());
        assertEquals("PENDIENTE", detalleDTO.getEstado());
        assertNull(detalleDTO.getFechaInicio());
        assertNull(detalleDTO.getFechaFin());
        assertNull(detalleDTO.getFechaAprobacion());

        // Verify suscripcion details
        assertNotNull(detalleDTO.getSuscripcionCliente());
        assertEquals(testSuscripcion.getId(), detalleDTO.getSuscripcionCliente().getIdSuscripcion());
        assertEquals("MENSUAL", detalleDTO.getSuscripcionCliente().getPeriodoContratado());
        assertEquals(270.00, detalleDTO.getSuscripcionCliente().getPrecioPlan());

        // Verify sucursales disponibles (empty by default)
        assertNotNull(detalleDTO.getSucursalesDisponiblesPermiso());
        assertTrue(detalleDTO.getSucursalesDisponiblesPermiso().isEmpty());
    }

    @Test
    void testObtenerDetallesPermisosTemporales_EmptyList_Integration() {
        // Act - No permisos created
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSolicitarPermisoTemporal_AllTipoVehiculo_Integration() {
        // Test both vehicle types
        String[] tiposVehiculo = {"DOS_RUEDAS", "CUATRO_RUEDAS"};

        for (int i = 0; i < tiposVehiculo.length; i++) {
            // Clean previous permisos for new test
            permisoTemporalRepository.deleteAll();

            // Create specific vehicle for each type
            Vehiculo vehiculoEspecifico = new Vehiculo();
            vehiculoEspecifico.setPropietario(testPersona);
            vehiculoEspecifico.setPlaca("VEH" + i + "123");
            vehiculoEspecifico.setMarca("Test");
            vehiculoEspecifico.setModelo("Model " + i);
            vehiculoEspecifico.setColor("Color " + i);
            vehiculoEspecifico.setTipoVehiculo(
                    tiposVehiculo[i].equals("DOS_RUEDAS") ? 
                    Vehiculo.TipoVehiculo.DOS_RUEDAS : 
                    Vehiculo.TipoVehiculo.CUATRO_RUEDAS
            );
            vehiculoEspecifico.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
            vehiculoEspecifico = vehiculoRepository.save(vehiculoEspecifico);

            SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
            solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
            solicitudDTO.setPlacaTemporal("VEH" + i + "123");
            solicitudDTO.setTipoVehiculoPermitido(tiposVehiculo[i]);
            solicitudDTO.setMotivo("Test " + tiposVehiculo[i]);

            // Act
            String result = solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

            // Assert
            assertEquals("Solicitud de permiso temporal creada exitosamente", result);

            // Verify in database
            List<PermisoTemporal> permisos = permisoTemporalRepository.findBySuscripcion_Id(testSuscripcion.getId());
            assertEquals(1, permisos.size());
            assertEquals(PermisoTemporal.TipoVehiculo.valueOf(tiposVehiculo[i]), permisos.get(0).getTipoVehiculoPermitido());
        }
    }

    @Test
    void testCompleteWorkflow_Integration() {
        // Test complete workflow from creation to retrieval

        // Step 1: Create multiple permisos temporales
        String[] placas = {"TMP001", "TMP002"};
        String[] tipos = {"DOS_RUEDAS", "CUATRO_RUEDAS"};
        String[] motivos = {"Moto de remplazo", "Carro de remplazo"};

        for (int i = 0; i < placas.length; i++) {
            // Create specific vehicle
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setPropietario(testPersona);
            vehiculo.setPlaca(placas[i]);
            vehiculo.setMarca("Test" + i);
            vehiculo.setModelo("Model" + i);
            vehiculo.setColor("Color" + i);
            vehiculo.setTipoVehiculo(
                    tipos[i].equals("DOS_RUEDAS") ? 
                    Vehiculo.TipoVehiculo.DOS_RUEDAS : 
                    Vehiculo.TipoVehiculo.CUATRO_RUEDAS
            );
            vehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
            vehiculoRepository.save(vehiculo);

            SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
            solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
            solicitudDTO.setPlacaTemporal(placas[i]);
            solicitudDTO.setTipoVehiculoPermitido(tipos[i]);
            solicitudDTO.setMotivo(motivos[i]);

            String createResult = solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);
            assertTrue(createResult.contains("exitosamente"));
        }

        // Step 2: Retrieve all permisos
        List<DetalleSolicitudesTemporalDTO> permisos = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(testCliente.getIdUsuario());

        // Step 3: Verify complete workflow
        assertEquals(2, permisos.size());

        for (int i = 0; i < permisos.size(); i++) {
            DetalleSolicitudesTemporalDTO permiso = permisos.get(i);
            assertNotNull(permiso.getIdPermisoTemporal());
            assertTrue(List.of(placas).contains(permiso.getPlacaTemporal()));
            assertTrue(List.of(tipos).contains(permiso.getTipoVehiculoPermitido()));
            assertTrue(List.of(motivos).contains(permiso.getMotivo()));
            assertEquals("PENDIENTE", permiso.getEstado());
            assertEquals(0, permiso.getUsosRealizados());
            
            // Verify suscripcion relationship
            assertNotNull(permiso.getSuscripcionCliente());
            assertEquals(testSuscripcion.getId(), permiso.getSuscripcionCliente().getIdSuscripcion());
        }

        // Step 4: Verify database consistency
        List<PermisoTemporal> permisosDB = permisoTemporalRepository.findBySuscripcion_Id(testSuscripcion.getId());
        assertEquals(2, permisosDB.size());
        
        for (PermisoTemporal permisoDB : permisosDB) {
            assertTrue(List.of(placas).contains(permisoDB.getPlacaTemporal()));
            assertEquals(PermisoTemporal.EstadoPermiso.PENDIENTE, permisoDB.getEstado());
            assertEquals(testSuscripcion.getId(), permisoDB.getSuscripcion().getId());
        }
    }

    @Test
    void testObtenerDetallesPermisosTemporales_WithSucursalesValidas_Integration() {
        // Arrange - Create additional sucursal
        Sucursal sucursal2 = new Sucursal();
        sucursal2.setEmpresa(testEmpresa);
        sucursal2.setUsuarioSucursal(testCliente);
        sucursal2.setNombre("Sucursal 2");
        sucursal2.setDireccionCompleta("Address 2");
        sucursal2.setCiudad("City 2");
        sucursal2.setDepartamento("Department 2");
        sucursal2.setLatitud(new BigDecimal("15.0000"));
        sucursal2.setLongitud(new BigDecimal("-91.0000"));
        sucursal2.setHoraApertura(LocalTime.of(7, 0));
        sucursal2.setHoraCierre(LocalTime.of(19, 0));
        sucursal2.setCapacidad2Ruedas(30);
        sucursal2.setCapacidad4Ruedas(80);
        sucursal2.setTelefonoContacto("87654321");
        sucursal2.setCorreoContacto("sucursal2@test.com");
        sucursal2.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        sucursal2 = sucursalRepository.save(sucursal2);

        // Create permiso temporal first
        SolicitarPermisoTemporalDTO solicitudDTO = new SolicitarPermisoTemporalDTO();
        solicitudDTO.setIdSuscripcion(testSuscripcion.getId());
        solicitudDTO.setPlacaTemporal("TMP789");
        solicitudDTO.setTipoVehiculoPermitido("CUATRO_RUEDAS");
        solicitudDTO.setMotivo("Permiso con sucursales específicas");

        solicitudTemporalClienteService.solicitarPermisoTemporal(solicitudDTO);

        // Update permiso to include specific sucursales
        List<PermisoTemporal> permisos = permisoTemporalRepository.findBySuscripcion_Id(testSuscripcion.getId());
        PermisoTemporal permiso = permisos.get(0);
        permiso.setSucursalesValidas(testSucursal.getIdSucursal() + "," + sucursal2.getIdSucursal());
        permisoTemporalRepository.save(permiso);

        // Act
        List<DetalleSolicitudesTemporalDTO> result = solicitudTemporalClienteService.obtenerDetallesPermisosTemporales(testCliente.getIdUsuario());

        // Assert
        assertEquals(1, result.size());
        DetalleSolicitudesTemporalDTO detalleDTO = result.get(0);
        
        assertNotNull(detalleDTO.getSucursalesDisponiblesPermiso());
        assertEquals(2, detalleDTO.getSucursalesDisponiblesPermiso().size());
        
        // Verify both sucursales are included
        List<Long> sucursalIds = detalleDTO.getSucursalesDisponiblesPermiso().stream()
                .map(s -> s.getIdSucursal())
                .toList();
        assertTrue(sucursalIds.contains(testSucursal.getIdSucursal()));
        assertTrue(sucursalIds.contains(sucursal2.getIdSucursal()));
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
                .sucursales(java.util.Arrays.asList())
                .suscripciones(java.util.Arrays.asList())
                .build();
    }
}
