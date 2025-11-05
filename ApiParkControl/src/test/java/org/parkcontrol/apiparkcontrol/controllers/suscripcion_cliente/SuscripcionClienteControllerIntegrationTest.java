package org.parkcontrol.apiparkcontrol.controllers.suscripcion_cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.NuevaSuscripcionDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.RenovacionSuscripcionDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SuscripcionClienteControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
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
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;

    // Test entities
    private Usuario testCliente;
    private Usuario testUsuarioEmpresa;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TipoPlan testTipoPlan;
    private TarifaBase testTarifaBase;
    private Vehiculo testVehiculo;
    private Suscripcion testSuscripcion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean database
        historialPagoSuscripcionRepository.deleteAll();
        suscripcionRepository.deleteAll();
        vehiculoRepository.deleteAll();
        configuracionDescuentoPlanRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create roles
        Rol clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Usuario cliente");
        clienteRol = rolRepository.save(clienteRol);

        Rol empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Create test cliente
        Persona personaCliente = createTestPersona("Juan", "Pérez", "client@test.com", generateUniqueDpi());
        testCliente = createTestUsuario(personaCliente, clienteRol, "cliente123");

        // Create test empresa usuario
        Persona personaEmpresa = createTestPersona("Admin", "Empresa", "admin@empresa.com", generateUniqueDpi());
        testUsuarioEmpresa = createTestUsuario(personaEmpresa, empresaRol, "adminempresa");

        // Create test empresa
        testEmpresa = createTestEmpresa();

        // Create test sucursal
        testSucursal = createTestSucursal();

        // Create test tarifa base
        testTarifaBase = createTestTarifaBase();

        // Create test tipo plan
        testTipoPlan = createTestTipoPlan();
        createTestConfiguracionDescuento();

        // Create test vehiculo
        testVehiculo = createTestVehiculo();

        // Create test suscripcion
        testSuscripcion = createTestSuscripcion();
        
        // Crear historial de pago inicial para la suscripción de prueba
        // Esto simula lo que hace el servicio cuando crea una nueva suscripción
        HistorialPagoSuscripcion historialInicial = new HistorialPagoSuscripcion();
        historialInicial.setSuscripcion(testSuscripcion);
        historialInicial.setFechaPago(LocalDateTime.now());
        historialInicial.setMontoPagado(testSuscripcion.getPrecioPlan());
        historialInicial.setMetodoPago(HistorialPagoSuscripcion.MetodoPago.TARJETA_CREDITO);
        historialInicial.setNumeroTransaccion("TXN_INICIAL_123");
        historialInicial.setEstadoPago(HistorialPagoSuscripcion.EstadoPago.COMPLETADO);
        historialInicial.setMotivoPago(HistorialPagoSuscripcion.MotivoPago.COMPRA_INICIAL);
        historialPagoSuscripcionRepository.save(historialInicial);
    }

    private Persona createTestPersona(String nombre, String apellido, String correo, String dpi) {
        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        persona.setDpi(dpi);
        persona.setCorreo(correo);
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Guatemala");
        persona.setPais("Guatemala");
        persona.setCodigoPostal("01001");
        persona.setEstado(Persona.Estado.ACTIVO);
        return personaRepository.save(persona);
    }

    private Usuario createTestUsuario(Persona persona, Rol rol, String nombreUsuario) {
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setRol(rol);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setContraseniaHash("hashedPassword");
        usuario.setDobleFactorHabilitado(false);
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario.setDebeCambiarContrasenia(false);
        usuario.setEsPrimeraVez(false);
        usuario.setIntentosFallidos(0);
        return usuarioRepository.save(usuario);
    }

    private Empresa createTestEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setUsuarioEmpresa(testUsuarioEmpresa);
        empresa.setNombreComercial("Test Company");
        empresa.setRazonSocial("Test Company S.A.");
        empresa.setNit("1234567-8");
        empresa.setDireccionFiscal("Test Fiscal Address");
        empresa.setTelefonoPrincipal("12345678");
        empresa.setCorreoPrincipal("empresa@test.com");
        empresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        return empresaRepository.save(empresa);
    }

    private Sucursal createTestSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setEmpresa(testEmpresa);
        sucursal.setUsuarioSucursal(testUsuarioEmpresa); // Usar el mismo usuario por simplicidad
        sucursal.setNombre("Sucursal Test");
        sucursal.setDireccionCompleta("Avenida Test 123");
        sucursal.setCiudad("Guatemala");
        sucursal.setDepartamento("Guatemala");
        sucursal.setLatitud(new BigDecimal("14.6349"));
        sucursal.setLongitud(new BigDecimal("-90.5069"));
        sucursal.setHoraApertura(LocalTime.of(8, 0));
        sucursal.setHoraCierre(LocalTime.of(18, 0));
        sucursal.setCapacidad2Ruedas(50);
        sucursal.setCapacidad4Ruedas(100);
        sucursal.setTelefonoContacto("22334455");
        sucursal.setCorreoContacto("test@sucursal.com");
        sucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        return sucursalRepository.save(sucursal);
    }

    private TarifaBase createTestTarifaBase() {
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(testEmpresa);
        tarifaBase.setPrecioPorHora(new BigDecimal("10.00"));
        tarifaBase.setMoneda("GTQ");
        tarifaBase.setFechaVigenciaInicio(LocalDate.now().minusDays(30));
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusDays(330));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        return tarifaBaseRepository.save(tarifaBase);
    }

    private TipoPlan createTestTipoPlan() {
        TipoPlan tipoPlan = new TipoPlan();
        tipoPlan.setEmpresa(testEmpresa);
        tipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        tipoPlan.setCodigoPlan("WW-001");
        tipoPlan.setDescripcion("Plan Workweek");
        tipoPlan.setPrecioPlan(350.0);
        tipoPlan.setHorasDia(8);
        tipoPlan.setHorasMensuales(160);
        tipoPlan.setDiasAplicables("L-M-X-J-V");
        tipoPlan.setCoberturaHoraria("08:00 - 18:00");
        tipoPlan.setOrdenBeneficio(2);
        tipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        return tipoPlanRepository.save(tipoPlan);
    }

    private void createTestConfiguracionDescuento() {
        ConfiguracionDescuentoPlan config = new ConfiguracionDescuentoPlan();
        config.setTipoPlan(testTipoPlan);
        config.setDescuentoMensual(new BigDecimal("15.00"));
        config.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        config.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        config.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        config.setCreadoPor(testUsuarioEmpresa);
        config.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(config);
    }

    private Vehiculo createTestVehiculo() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPropietario(testCliente.getPersona());
        vehiculo.setPlaca("ABC123");
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setAnio(2020);
        vehiculo.setColor("Blanco");
        vehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS); // Corregir el enum
        vehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        return vehiculoRepository.save(vehiculo);
    }

    private Suscripcion createTestSuscripcion() {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEmpresa(testEmpresa);
        suscripcion.setUsuario(testCliente);
        suscripcion.setVehiculo(testVehiculo);
        suscripcion.setTipoPlan(testTipoPlan);
        suscripcion.setTarifaBaseReferencia(testTarifaBase);
        suscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        suscripcion.setDescuentoAplicado(new BigDecimal("0.00"));
        suscripcion.setPrecioPlan(new BigDecimal("350.00"));
        suscripcion.setHorasMensualesIncluidas(160);
        suscripcion.setHorasConsumidas(new BigDecimal("45.5"));
        suscripcion.setFechaInicio(LocalDateTime.now());
        suscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        suscripcion.setFechaCompra(LocalDateTime.now());
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setMetodoPago("TARJETA_CREDITO");
        suscripcion.setNumeroTransaccion("TXN123456789");
        return suscripcionRepository.save(suscripcion);
    }

    @Test
    void testGetPlanesDisponiblesParaCliente_Integration() throws Exception {
        mockMvc.perform(get("/api/cliente/suscripciones/planes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.empresasSuscripciones", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].idEmpresa", is(testEmpresa.getIdEmpresa().intValue())))
                .andExpect(jsonPath("$.empresasSuscripciones[0].nombreComercial", is("Test Company")))
                .andExpect(jsonPath("$.empresasSuscripciones[0].sucursales", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].suscripciones", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].suscripciones[0].nombrePlan", is("WORKWEEK")));
    }

    @Test
    void testGetVehiculosPorCliente_Integration() throws Exception {
        mockMvc.perform(get("/api/cliente/suscripciones/vehiculos/{idCliente}", testCliente.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idVehiculo", is(testVehiculo.getId().intValue())))
                .andExpect(jsonPath("$[0].placa", is("ABC123")))
                .andExpect(jsonPath("$[0].marca", is("Toyota")))
                .andExpect(jsonPath("$[0].modelo", is("Corolla")))
                .andExpect(jsonPath("$[0].tipoVehiculo", is("CUATRO_RUEDAS")));
    }

    @Test
    void testGetVehiculosPorCliente_ClienteInexistente_Integration() throws Exception {
        mockMvc.perform(get("/api/cliente/suscripciones/vehiculos/{idCliente}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Cliente no encontrado o inactivo")));
    }

    @Test
    void testGetPlanesContratadosPorCliente_Integration() throws Exception {
        mockMvc.perform(get("/api/cliente/suscripciones/planes/{idCliente}", testCliente.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCliente", is(testCliente.getIdUsuario().intValue())))
                .andExpect(jsonPath("$.nombreCliente", is("cliente123")))
                .andExpect(jsonPath("$.suscripcionCliente", hasSize(1)))
                .andExpect(jsonPath("$.suscripcionCliente[0].idSuscripcion", is(testSuscripcion.getId().intValue())))
                .andExpect(jsonPath("$.suscripcionCliente[0].periodoContratado", is("MENSUAL")))
                .andExpect(jsonPath("$.suscripcionCliente[0].estadoSuscripcion", is("ACTIVA")));
    }

    @Test
    void testCrearNuevaSuscripcion_Integration() throws Exception {
        // Arrange - Create another vehicle for new subscription
        Vehiculo nuevoVehiculo = new Vehiculo();
        nuevoVehiculo.setPropietario(testCliente.getPersona());
        nuevoVehiculo.setPlaca("XYZ789");
        nuevoVehiculo.setMarca("Honda");
        nuevoVehiculo.setModelo("Civic");
        nuevoVehiculo.setAnio(2021);
        nuevoVehiculo.setColor("Negro");
        nuevoVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        nuevoVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        nuevoVehiculo = vehiculoRepository.save(nuevoVehiculo);

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(nuevoVehiculo.getId());
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_DEBITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN987654321");

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaSuscripcionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nueva suscripción creada con éxito")));

        // Verify subscription was created
        assertEquals(2, suscripcionRepository.count());
        // Verify payment history was created (1 inicial del setUp + 1 nuevo = 2 total)
        assertEquals(2, historialPagoSuscripcionRepository.count());
    }

    @Test
    void testCrearNuevaSuscripcion_VehiculoYaSuscrito_Integration() throws Exception {
        // Try to create subscription with already subscribed vehicle
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(testVehiculo.getId()); // Already subscribed
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN111111111");

        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaSuscripcionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("ya tiene una suscripción activa")));
    }

    @Test
    void testRenovarSuscripcion_Integration() throws Exception {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(testSuscripcion.getId());
        renovacionDTO.setNuevoPeriodoContratado("ANUAL");
        renovacionDTO.setMetodoPago("TRANSFERENCIA_BANCARIA");
        renovacionDTO.setNumeroTransaccion("TXN555555555");

        LocalDateTime fechaFinOriginal = testSuscripcion.getFechaFin();

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/renovar-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renovacionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Suscripción renovada con éxito")));

        // Verify subscription was renewed
        Suscripcion suscripcionRenovada = suscripcionRepository.findById(testSuscripcion.getId()).orElse(null);
        assertNotNull(suscripcionRenovada);
        assertTrue(suscripcionRenovada.getFechaFin().isAfter(fechaFinOriginal));

        // Verify payment history was created (1 inicial del setUp + 1 renovación = 2 total)
        assertEquals(2, historialPagoSuscripcionRepository.count());
    }

    @Test
    void testRenovarSuscripcion_SuscripcionInexistente_Integration() throws Exception {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(999L);
        renovacionDTO.setNuevoPeriodoContratado("MENSUAL");

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/renovar-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renovacionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Suscripción no encontrada")));
    }

    @Test
    void testCrearNuevaSuscripcion_PeriodoAnual_Integration() throws Exception {
        // Arrange - Create another vehicle
        Vehiculo vehiculoAnual = new Vehiculo();
        vehiculoAnual.setPropietario(testCliente.getPersona());
        vehiculoAnual.setPlaca("ANU789");
        vehiculoAnual.setMarca("Nissan");
        vehiculoAnual.setModelo("Sentra");
        vehiculoAnual.setAnio(2022);
        vehiculoAnual.setColor("Gris");
        vehiculoAnual.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        vehiculoAnual.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        vehiculoAnual = vehiculoRepository.save(vehiculoAnual);

        NuevaSuscripcionDTO suscripcionAnualDTO = new NuevaSuscripcionDTO();
        suscripcionAnualDTO.setIdCliente(testCliente.getIdUsuario());
        suscripcionAnualDTO.setIdVehiculo(vehiculoAnual.getId());
        suscripcionAnualDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        suscripcionAnualDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        suscripcionAnualDTO.setPeriodoContratado("ANUAL");
        suscripcionAnualDTO.setMetodoPago("TARJETA_CREDITO");
        suscripcionAnualDTO.setNumeroTransaccion("TXNANUAL123");

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(suscripcionAnualDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nueva suscripción creada con éxito")));

        // Verify annual subscription has discount applied
        List<Suscripcion> suscripciones = suscripcionRepository.findByUsuario_IdUsuario(testCliente.getIdUsuario());
        Suscripcion suscripcionAnual = suscripciones.stream()
                .filter(s -> s.getPeriodoContratado() == Suscripcion.PeriodoContratado.ANUAL)
                .findFirst()
                .orElse(null);
        
        assertNotNull(suscripcionAnual);
        assertTrue(suscripcionAnual.getDescuentoAplicado().compareTo(BigDecimal.ZERO) > 0);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
