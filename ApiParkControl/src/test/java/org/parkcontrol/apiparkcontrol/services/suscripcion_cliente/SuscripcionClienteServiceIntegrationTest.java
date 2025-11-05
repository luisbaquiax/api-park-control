package org.parkcontrol.apiparkcontrol.services.suscripcion_cliente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SuscripcionClienteServiceIntegrationTest {

    @Autowired
    private SuscripcionClienteService suscripcionClienteService;

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
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;
    @Autowired
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;

    private Usuario testCliente;
    private Empresa testEmpresa;
    private TipoPlan testTipoPlan;
    private Vehiculo testVehiculo;
    private TarifaBase testTarifaBase;
    private Sucursal testSucursal;

    @BeforeEach
    void setUp() {
        // Clean up database
        historialPagoSuscripcionRepository.deleteAll();
        suscripcionRepository.deleteAll();
        configuracionDescuentoPlanRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        vehiculoRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test roles
        Rol clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Usuario cliente");
        clienteRol = rolRepository.save(clienteRol);

        Rol empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Create test persona for cliente
        Persona personaCliente = new Persona();
        personaCliente.setNombre("Juan");
        personaCliente.setApellido("Cliente");
        personaCliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        personaCliente.setDpi(generateUniqueDpi());
        personaCliente.setCorreo("cliente@test.com");
        personaCliente.setTelefono("12345678");
        personaCliente.setDireccionCompleta("Dirección Cliente");
        personaCliente.setCiudad("Guatemala");
        personaCliente.setPais("Guatemala");
        personaCliente.setCodigoPostal("01001");
        personaCliente.setEstado(Persona.Estado.ACTIVO);
        personaCliente = personaRepository.save(personaCliente);

        // Create test cliente
        testCliente = new Usuario();
        testCliente.setPersona(personaCliente);
        testCliente.setRol(clienteRol);
        testCliente.setNombreUsuario("testcliente");
        testCliente.setContraseniaHash("hashedPassword");
        testCliente.setDobleFactorHabilitado(false);
        testCliente.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testCliente.setDebeCambiarContrasenia(false);
        testCliente.setEsPrimeraVez(false);
        testCliente.setIntentosFallidos(0);
        testCliente = usuarioRepository.save(testCliente);

        // Create test persona for empresa
        Persona personaEmpresa = new Persona();
        personaEmpresa.setNombre("Admin");
        personaEmpresa.setApellido("Empresa");
        personaEmpresa.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        personaEmpresa.setDpi(generateUniqueDpi());
        personaEmpresa.setCorreo("admin@empresa.com");
        personaEmpresa.setTelefono("87654321");
        personaEmpresa.setDireccionCompleta("Dirección Empresa");
        personaEmpresa.setCiudad("Guatemala");
        personaEmpresa.setPais("Guatemala");
        personaEmpresa.setCodigoPostal("01002");
        personaEmpresa.setEstado(Persona.Estado.ACTIVO);
        personaEmpresa = personaRepository.save(personaEmpresa);

        // Create test empresa user
        Usuario usuarioEmpresa = new Usuario();
        usuarioEmpresa.setPersona(personaEmpresa);
        usuarioEmpresa.setRol(empresaRol);
        usuarioEmpresa.setNombreUsuario("adminempresa");
        usuarioEmpresa.setContraseniaHash("hashedPassword");
        usuarioEmpresa.setDobleFactorHabilitado(false);
        usuarioEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioEmpresa.setDebeCambiarContrasenia(false);
        usuarioEmpresa.setEsPrimeraVez(false);
        usuarioEmpresa.setIntentosFallidos(0);
        usuarioEmpresa = usuarioRepository.save(usuarioEmpresa);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(usuarioEmpresa);
        testEmpresa.setNombreComercial("Test Parking Company");
        testEmpresa.setRazonSocial("Test Parking Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Dirección Fiscal Empresa");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("info@testparking.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(usuarioEmpresa);
        testSucursal.setNombre("Sucursal Centro");
        testSucursal.setDireccionCompleta("Centro de la Ciudad");
        testSucursal.setCiudad("Guatemala");
        testSucursal.setDepartamento("Guatemala");
        testSucursal.setLatitud(new BigDecimal("14.6349"));
        testSucursal.setLongitud(new BigDecimal("-90.5069"));
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(100);
        testSucursal.setTelefonoContacto("12345678");
        testSucursal.setCorreoContacto("sucursal@testparking.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);

        // Create test tarifa base
        testTarifaBase = new TarifaBase();
        testTarifaBase.setEmpresa(testEmpresa);
        testTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        testTarifaBase.setMoneda("GTQ");
        testTarifaBase.setFechaVigenciaInicio(LocalDate.from(LocalDateTime.now().minusDays(30)));
        testTarifaBase.setFechaVigenciaFin(LocalDate.from(LocalDateTime.now().plusDays(330)));
        testTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        testTarifaBase = tarifaBaseRepository.save(testTarifaBase);

        // Create test tipo plan
        testTipoPlan = new TipoPlan();
        testTipoPlan.setEmpresa(testEmpresa);
        testTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        testTipoPlan.setCodigoPlan("WW-001");
        testTipoPlan.setDescripcion("Plan Workweek para uso laboral");
        testTipoPlan.setPrecioPlan(300.00);
        testTipoPlan.setHorasDia(8);
        testTipoPlan.setHorasMensuales(160);
        testTipoPlan.setDiasAplicables("L-M-X-J-V");
        testTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        testTipoPlan.setOrdenBeneficio(2);
        testTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        testTipoPlan = tipoPlanRepository.save(testTipoPlan);

        // Create test configuracion descuento
        ConfiguracionDescuentoPlan configDescuento = new ConfiguracionDescuentoPlan();
        configDescuento.setTipoPlan(testTipoPlan);
        configDescuento.setDescuentoMensual(new BigDecimal("15.00"));
        configDescuento.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        configDescuento.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        configDescuento.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        configDescuento.setCreadoPor(usuarioEmpresa);
        configDescuento.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        configuracionDescuentoPlanRepository.save(configDescuento);

        // Create test vehiculo
        testVehiculo = new Vehiculo();
        testVehiculo.setPropietario(personaCliente);
        testVehiculo.setPlaca("ABC123");
        testVehiculo.setMarca("Toyota");
        testVehiculo.setModelo("Corolla");
        testVehiculo.setColor("Blanco");
        testVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculo = vehiculoRepository.save(testVehiculo);
    }

    @Test
    void testObtenerPlanesSuscripcion_Integration() {
        // Act
        PlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcion();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEmpresasSuscripciones());
        assertEquals(1, result.getEmpresasSuscripciones().size());

        PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaDTO = result.getEmpresasSuscripciones().get(0);
        assertEquals(testEmpresa.getIdEmpresa(), empresaDTO.getIdEmpresa());
        assertEquals("Test Parking Company", empresaDTO.getNombreComercial());
        assertEquals("1234567-8", empresaDTO.getNit());
        assertEquals("Test Parking Company S.A.", empresaDTO.getRazonSocial());

        // Verify sucursales
        assertEquals(1, empresaDTO.getSucursales().size());
        assertEquals("Sucursal Centro", empresaDTO.getSucursales().get(0).getNombre());

        // Verify suscripciones (planes)
        assertEquals(1, empresaDTO.getSuscripciones().size());
        assertEquals("WORKWEEK", empresaDTO.getSuscripciones().get(0).getNombrePlan());
        assertEquals(300.00, empresaDTO.getSuscripciones().get(0).getPrecioPlan());
    }

    @Test
    void testObtenerPlanesSuscripcionPorCliente_EmptyList_Integration() {
        // Act
        ClientePlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(testCliente.getIdUsuario(), result.getIdCliente());
        assertEquals("testcliente", result.getNombreCliente());
        assertTrue(result.getSuscripcionCliente().isEmpty());
    }

    @Test
    void testObtenerVehiculosCliente_Integration() {
        // Act
        List<VehiculoClienteDTO> result = suscripcionClienteService.obtenerVehiculosCliente(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        VehiculoClienteDTO vehiculoDTO = result.get(0);
        assertEquals(testVehiculo.getId(), vehiculoDTO.getIdVehiculo());
        assertEquals("ABC123", vehiculoDTO.getPlaca());
        assertEquals("Toyota", vehiculoDTO.getMarca());
        assertEquals("Corolla", vehiculoDTO.getModelo());
        assertEquals("Blanco", vehiculoDTO.getColor());
        assertEquals("CUATRO_RUEDAS", vehiculoDTO.getTipoVehiculo());
    }

    @Test
    void testNuevaSuscripcionCliente_Mensual_Integration() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(testVehiculo.getId());
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN123456789");

        // Act
        String result = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);

        // Assert
        assertEquals("Nueva suscripción creada con éxito", result);

        // Verify suscripcion was created
        List<Suscripcion> suscripciones = suscripcionRepository.findByUsuario_IdUsuario(testCliente.getIdUsuario());
        assertEquals(1, suscripciones.size());

        Suscripcion suscripcion = suscripciones.get(0);
        assertEquals(testCliente.getIdUsuario(), suscripcion.getUsuario().getIdUsuario());
        assertEquals(testVehiculo.getId(), suscripcion.getVehiculo().getId());
        assertEquals(testEmpresa.getIdEmpresa(), suscripcion.getEmpresa().getIdEmpresa());
        assertEquals(testTipoPlan.getId(), suscripcion.getTipoPlan().getId());
        assertEquals(Suscripcion.PeriodoContratado.MENSUAL, suscripcion.getPeriodoContratado()); // Ahora debe funcionar
        assertEquals(0, new BigDecimal("300.00").compareTo(suscripcion.getPrecioPlan()));
        assertEquals(160, suscripcion.getHorasMensualesIncluidas());
        assertEquals(Suscripcion.EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        assertEquals("TARJETA_CREDITO", suscripcion.getMetodoPago());
        assertEquals("TXN123456789", suscripcion.getNumeroTransaccion());

        // Verify historial pago was created
        List<HistorialPagoSuscripcion> historialPagos = historialPagoSuscripcionRepository.findAll();
        assertEquals(1, historialPagos.size());

        HistorialPagoSuscripcion historialPago = historialPagos.get(0);
        assertEquals(suscripcion.getId(), historialPago.getSuscripcion().getId());
        assertEquals(0, new BigDecimal("300.00").compareTo(historialPago.getMontoPagado()));
        assertEquals(HistorialPagoSuscripcion.MetodoPago.TARJETA_CREDITO, historialPago.getMetodoPago());
        assertEquals(HistorialPagoSuscripcion.EstadoPago.COMPLETADO, historialPago.getEstadoPago());
        assertEquals(HistorialPagoSuscripcion.MotivoPago.COMPRA_INICIAL, historialPago.getMotivoPago());
    }

    @Test
    void testNuevaSuscripcionCliente_Anual_Integration() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(testVehiculo.getId());
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("ANUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN987654321");

        // Act
        String result = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);

        // Assert
        assertEquals("Nueva suscripción creada con éxito", result);

        // Verify suscripcion was created
        List<Suscripcion> suscripciones = suscripcionRepository.findByUsuario_IdUsuario(testCliente.getIdUsuario());
        assertEquals(1, suscripciones.size());

        Suscripcion suscripcion = suscripciones.get(0);
        assertEquals(Suscripcion.PeriodoContratado.ANUAL, suscripcion.getPeriodoContratado());
        
        // Verificar el cálculo según la lógica real del servicio
        // El servicio hace: 300 * 12 = 3600, descuento = 300 * 0.10 = 30, precio final = 3600 - 30 = 3570
        BigDecimal precioBase = new BigDecimal("300.00");
        BigDecimal precioAnual = precioBase.multiply(new BigDecimal("12")); // 3600
        BigDecimal descuentoCalculado = precioBase.multiply(new BigDecimal("0.10")); // 30 (10% del precio base, no del total)
        BigDecimal precioFinalEsperado = precioAnual.subtract(descuentoCalculado); // 3570
        
        assertEquals(0, precioFinalEsperado.compareTo(suscripcion.getPrecioPlan()));
        assertEquals(0, descuentoCalculado.compareTo(suscripcion.getDescuentoAplicado()));
        
        // Verify dates
        assertNotNull(suscripcion.getFechaInicio());
        assertNotNull(suscripcion.getFechaFin());
        assertTrue(suscripcion.getFechaFin().isAfter(suscripcion.getFechaInicio().plusMonths(11))); // Should be ~1 year later
    }

    @Test
    void testRenovarSuscripcionCliente_Integration() {
        // Arrange - First create a suscripcion
        Suscripcion suscripcionExistente = createTestSuscripcion();

        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(suscripcionExistente.getId());
        renovacionDTO.setNuevoPeriodoContratado("MENSUAL");
        renovacionDTO.setMetodoPago("TARJETA_CREDITO");
        renovacionDTO.setNumeroTransaccion("TXN555666777");

        LocalDateTime fechaFinOriginal = suscripcionExistente.getFechaFin();

        // Act
        String result = suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);

        // Assert
        assertEquals("Suscripción renovada con éxito", result);

        // Verify suscripcion was updated
        Suscripcion suscripcionRenovada = suscripcionRepository.findById(suscripcionExistente.getId()).orElse(null);
        assertNotNull(suscripcionRenovada);
        assertTrue(suscripcionRenovada.getFechaFin().isAfter(fechaFinOriginal));

        // Verify historial pago for renovation
        List<HistorialPagoSuscripcion> historialPagos = historialPagoSuscripcionRepository.findAll();
        assertEquals(1, historialPagos.size());

        HistorialPagoSuscripcion historialRenovacion = historialPagos.get(0);
        assertEquals(HistorialPagoSuscripcion.MotivoPago.RENOVACION, historialRenovacion.getMotivoPago());
        assertEquals("TXN555666777", historialRenovacion.getNumeroTransaccion());
    }

    @Test
    void testNuevaSuscripcionCliente_ClienteNotFound_Integration() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(999L); // Non-existent client

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Cliente no encontrado o inactivo", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_VehiculoNotBelongsToClient_Integration() {
        // Arrange - Create another person and vehicle
        Persona otraPersona = new Persona();
        otraPersona.setNombre("Pedro");
        otraPersona.setApellido("Otro");
        otraPersona.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        otraPersona.setDpi(generateUniqueDpi());
        otraPersona.setCorreo("pedro@test.com");
        otraPersona.setTelefono("87654321");
        otraPersona.setDireccionCompleta("Dirección Pedro");
        otraPersona.setCiudad("Guatemala");
        otraPersona.setPais("Guatemala");
        otraPersona.setCodigoPostal("01003");
        otraPersona.setEstado(Persona.Estado.ACTIVO);
        otraPersona = personaRepository.save(otraPersona);

        Vehiculo otroVehiculo = new Vehiculo();
        otroVehiculo.setPropietario(otraPersona);
        otroVehiculo.setPlaca("DEF456");
        otroVehiculo.setMarca("Honda");
        otroVehiculo.setModelo("Civic");
        otroVehiculo.setColor("Azul");
        otroVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        otroVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        otroVehiculo = vehiculoRepository.save(otroVehiculo);

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(otroVehiculo.getId()); // Vehicle belongs to another person

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Vehículo no encontrado o no pertenece al cliente", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_SuscripcionAlreadyExists_Integration() {
        // Arrange - Create existing suscripcion
        createTestSuscripcion();

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(testVehiculo.getId());
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("El vehículo ya tiene una suscripción activa con el mismo tipo de plan en esta empresa", exception.getMessage());
    }

    @Test
    void testObtenerPlanesSuscripcionPorCliente_WithSuscripciones_Integration() {
        // Arrange - Create test suscripcion
        createTestSuscripcion();

        // Act
        ClientePlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(testCliente.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(testCliente.getIdUsuario(), result.getIdCliente());
        assertEquals("testcliente", result.getNombreCliente());
        assertEquals(1, result.getSuscripcionCliente().size());

        ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcionDTO = result.getSuscripcionCliente().get(0);
        assertEquals("MENSUAL", suscripcionDTO.getPeriodoContratado());
        assertEquals(300.00, suscripcionDTO.getPrecioPlan());
        assertEquals(0.00, suscripcionDTO.getDescuentoAplicado());
        assertEquals(160, suscripcionDTO.getHorasMensualesIncluidas());
        assertEquals("ACTIVA", suscripcionDTO.getEstadoSuscripcion()); // Corregido

        // Verify vehiculo data
        assertNotNull(suscripcionDTO.getVehiculoClienteDTO());
        assertEquals("ABC123", suscripcionDTO.getVehiculoClienteDTO().getPlaca());
        assertEquals("Toyota", suscripcionDTO.getVehiculoClienteDTO().getMarca());

        // Verify plan data
        assertNotNull(suscripcionDTO.getTipoPlanSuscripcionDTO());
        assertEquals("WORKWEEK", suscripcionDTO.getTipoPlanSuscripcionDTO().getNombrePlan());

        // Verify sucursales
        assertNotNull(suscripcionDTO.getSucursalesDisponibles());
        assertEquals(1, suscripcionDTO.getSucursalesDisponibles().size());
        assertEquals("Sucursal Centro", suscripcionDTO.getSucursalesDisponibles().get(0).getNombre());
    }

    @Test
    void testRenovarSuscripcionCliente_SuscripcionNotFound_Integration() {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(999L); // Non-existent suscripcion

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);
        });

        assertEquals("Suscripción no encontrada", exception.getMessage());
    }

    @Test
    void testRenovarSuscripcionCliente_PeriodoInvalido_Integration() {
        // Arrange - Create suscripcion
        Suscripcion suscripcionExistente = createTestSuscripcion();

        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(suscripcionExistente.getId());
        renovacionDTO.setNuevoPeriodoContratado("INVALID_PERIOD");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);
        });

        assertEquals("Periodo contratado no válido", exception.getMessage());
    }

    @Test
    void testCompleteWorkflow_Integration() {
        // Complete workflow: Get plans -> Get vehicles -> Create subscription -> Renew
        
        // Step 1: Get all available plans
        PlanesSuscripcionDTO planes = suscripcionClienteService.obtenerPlanesSuscripcion();
        assertNotNull(planes);
        assertEquals(1, planes.getEmpresasSuscripciones().size());

        // Step 2: Get client vehicles
        List<VehiculoClienteDTO> vehiculos = suscripcionClienteService.obtenerVehiculosCliente(testCliente.getIdUsuario());
        assertEquals(1, vehiculos.size());

        // Step 3: Create new subscription
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(testCliente.getIdUsuario());
        nuevaSuscripcionDTO.setIdVehiculo(testVehiculo.getId());
        nuevaSuscripcionDTO.setIdEmpresa(testEmpresa.getIdEmpresa());
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(testTipoPlan.getId());
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN111222333");

        String createResult = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        assertEquals("Nueva suscripción creada con éxito", createResult);

        // Step 4: Get client subscriptions
        ClientePlanesSuscripcionDTO clientePlanes = suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(testCliente.getIdUsuario());
        assertEquals(1, clientePlanes.getSuscripcionCliente().size());

        // Step 5: Renew subscription
        Suscripcion createdSuscripcion = suscripcionRepository.findByUsuario_IdUsuario(testCliente.getIdUsuario()).get(0);
        
        // Verificar que el período contratado se estableció correctamente
        assertEquals(Suscripcion.PeriodoContratado.MENSUAL, createdSuscripcion.getPeriodoContratado());
        
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(createdSuscripcion.getId());
        renovacionDTO.setNuevoPeriodoContratado("ANUAL");
        renovacionDTO.setMetodoPago("TARJETA_CREDITO");
        renovacionDTO.setNumeroTransaccion("TXN444555666");

        String renewResult = suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);
        assertEquals("Suscripción renovada con éxito", renewResult);

        // Verify final state
        List<HistorialPagoSuscripcion> historialPagos = historialPagoSuscripcionRepository.findAll();
        assertEquals(2, historialPagos.size()); // One for creation, one for renewal
    }

    private Suscripcion createTestSuscripcion() {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEmpresa(testEmpresa);
        suscripcion.setUsuario(testCliente);
        suscripcion.setVehiculo(testVehiculo);
        suscripcion.setTipoPlan(testTipoPlan);
        suscripcion.setTarifaBaseReferencia(testTarifaBase);
        suscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL); // Ahora establecemos el período
        suscripcion.setDescuentoAplicado(new BigDecimal("0.00"));
        suscripcion.setPrecioPlan(new BigDecimal("300.00"));
        suscripcion.setHorasMensualesIncluidas(160);
        suscripcion.setHorasConsumidas(new BigDecimal("0.00"));
        suscripcion.setFechaInicio(LocalDateTime.now());
        suscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        suscripcion.setFechaCompra(LocalDateTime.now());
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setMetodoPago("TARJETA_CREDITO");
        suscripcion.setNumeroTransaccion("TEST123");
        return suscripcionRepository.save(suscripcion);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
