package org.parkcontrol.apiparkcontrol.services.empresa_flotilla;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa_flotilla.*;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.VehiculoClienteDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class GestionEmpresaFlotillaServiceIntegrationTest {

    @Autowired
    private GestionEmpresaFlotillaService gestionEmpresaFlotillaService;

    @Autowired
    private EmpresaFlotillaRepository empresaFlotillaRepository;
    @Autowired
    private PlanCorporativoRepository planCorporativoRepository;
    @Autowired
    private SuscripcionFlotillaRepository suscripcionFlotillaRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private TipoPlanRepository tipoPlanRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;

    private Usuario testUsuario;
    private Empresa testEmpresa;
    private TipoPlan testTipoPlan;
    private EmpresaFlotilla testEmpresaFlotilla;
    private Vehiculo testVehiculo;
    private Rol empresaRol;

    @BeforeEach
    void setUp() {
        // Clean database
        suscripcionFlotillaRepository.deleteAll();
        planCorporativoRepository.deleteAll();
        empresaFlotillaRepository.deleteAll();
        suscripcionRepository.deleteAll();
        vehiculoRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario de empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Create test persona
        Persona testPersona = new Persona();
        testPersona.setNombre("Test");
        testPersona.setApellido("User");
        testPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("test@example.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Test Address");
        testPersona.setCiudad("Test City");
        testPersona.setPais("Test Country");
        testPersona.setCodigoPostal("12345");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        // Create test usuario
        testUsuario = new Usuario();
        testUsuario.setPersona(testPersona);
        testUsuario.setRol(empresaRol);
        testUsuario.setNombreUsuario("testuser");
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testUsuario);
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Fiscal Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

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

        // Create test vehiculo
        testVehiculo = new Vehiculo();
        testVehiculo.setPropietario(testPersona);
        testVehiculo.setPlaca("ABC123");
        testVehiculo.setMarca("Toyota");
        testVehiculo.setModelo("Corolla");
        testVehiculo.setColor("Blanco");
        testVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        testVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        testVehiculo = vehiculoRepository.save(testVehiculo);

        // Create test empresa flotilla
        testEmpresaFlotilla = new EmpresaFlotilla();
        testEmpresaFlotilla.setNombreEmpresa("Test Fleet Company");
        testEmpresaFlotilla.setRazonSocial("Test Fleet Company S.A.");
        testEmpresaFlotilla.setNit("9876543-2");
        testEmpresaFlotilla.setTelefono("87654321");
        testEmpresaFlotilla.setCorreoContacto("fleet@test.com");
        testEmpresaFlotilla.setDireccion("Fleet Address");
        testEmpresaFlotilla.setEstado(EmpresaFlotilla.EstadoEmpresaFlotilla.ACTIVA);
        testEmpresaFlotilla = empresaFlotillaRepository.save(testEmpresaFlotilla);
    }

    @Test
    void testNuevaEmpresaFlotilla_Integration() {
        // Arrange
        NuevaEmpresaFlotillaDTO dto = new NuevaEmpresaFlotillaDTO();
        dto.setNombreEmpresa("New Fleet Company");
        dto.setRazonSocial("New Fleet Company S.A.");
        dto.setNit("1111222-3");
        dto.setTelefono("11112222");
        dto.setCorreoContacto("newfleet@test.com");
        dto.setDireccion("New Fleet Address");

        // Act
        String result = gestionEmpresaFlotillaService.nuevaEmpresaFlotilla(dto);

        // Assert
        assertEquals("Nueva empresa de flotilla creada con éxito.", result);

        List<EmpresaFlotilla> empresas = empresaFlotillaRepository.findAll();
        assertEquals(2, empresas.size()); // testEmpresaFlotilla + nueva empresa

        EmpresaFlotilla nuevaEmpresa = empresas.stream()
                .filter(e -> "New Fleet Company".equals(e.getNombreEmpresa()))
                .findFirst()
                .orElse(null);

        assertNotNull(nuevaEmpresa);
        assertEquals("New Fleet Company S.A.", nuevaEmpresa.getRazonSocial());
        assertEquals("1111222-3", nuevaEmpresa.getNit());
        assertEquals(EmpresaFlotilla.EstadoEmpresaFlotilla.ACTIVA, nuevaEmpresa.getEstado());
        assertNotNull(nuevaEmpresa.getFechaRegistro());
    }

    @Test
    void testNuevoPlanCorporativo_Integration() {
        // Arrange
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        dto.setIdTipoPlan(testTipoPlan.getId());
        dto.setNombrePlanCorporativo("Corporate Plan Integration Test");
        dto.setNumeroPlacasContratadas(100);
        dto.setDescuentoCorporativoAdicional(20.00);
        dto.setPrecioPlanCorporativo(25000.00);
        dto.setFechaInicio("2024-01-01");
        dto.setFechaFin("2024-12-31");
        dto.setIdCreadoPor(testUsuario.getIdUsuario());

        // Act
        String result = gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);

        // Assert
        assertEquals("Nuevo plan corporativo creado con éxito.", result);

        List<PlanCorporativo> planes = planCorporativoRepository.findAll();
        assertEquals(1, planes.size());

        PlanCorporativo plan = planes.get(0);
        assertEquals("Corporate Plan Integration Test", plan.getNombrePlanCorporativo());
        assertEquals(100, plan.getNumeroPlacasContratadas());
        assertEquals(0, new BigDecimal("20.00").compareTo(plan.getDescuentoCorporativoAdicional()));
        assertEquals(0, new BigDecimal("25000.00").compareTo(plan.getPrecioPlanCorporativo()));
        assertEquals(PlanCorporativo.EstadoPlanCorporativo.ACTIVO, plan.getEstado());
        assertEquals(testEmpresaFlotilla.getIdEmpresaFlotilla(), plan.getEmpresaFlotilla().getIdEmpresaFlotilla());
        assertEquals(testTipoPlan.getId(), plan.getTipoPlan().getId());
    }

    @Test
    void testActivarDesactivarPlanCorporativo_Integration() {
        // Arrange - Create plan corporativo
        NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
        dto.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        dto.setIdTipoPlan(testTipoPlan.getId());
        dto.setNombrePlanCorporativo("Test Plan");
        dto.setNumeroPlacasContratadas(50);
        dto.setDescuentoCorporativoAdicional(15.00);
        dto.setPrecioPlanCorporativo(15000.00);
        dto.setFechaInicio("2024-01-01");
        dto.setFechaFin("2024-12-31");
        dto.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        PlanCorporativo plan = planCorporativoRepository.findAll().get(0);

        // Test desactivar
        String resultDesactivar = gestionEmpresaFlotillaService.desactivarPlanCorporativo(plan.getIdPlanCorporativo());
        assertEquals("Plan corporativo desactivado con éxito.", resultDesactivar);

        PlanCorporativo planDesactivado = planCorporativoRepository.findById(plan.getIdPlanCorporativo()).orElse(null);
        assertNotNull(planDesactivado);
        assertEquals(PlanCorporativo.EstadoPlanCorporativo.CANCELADO, planDesactivado.getEstado());

        // Test activar
        String resultActivar = gestionEmpresaFlotillaService.activarPlanCorporativo(plan.getIdPlanCorporativo());
        assertEquals("Plan corporativo activado con éxito.", resultActivar);

        PlanCorporativo planActivado = planCorporativoRepository.findById(plan.getIdPlanCorporativo()).orElse(null);
        assertNotNull(planActivado);
        assertEquals(PlanCorporativo.EstadoPlanCorporativo.ACTIVO, planActivado.getEstado());
    }

    @Test
    void testSuscribirVehiculoPlanCorporativo_Integration() {
        // Arrange - Create plan corporativo first
        NuevoPlanCorporativoDTO planDTO = new NuevoPlanCorporativoDTO();
        planDTO.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        planDTO.setIdTipoPlan(testTipoPlan.getId());
        planDTO.setNombrePlanCorporativo("Test Plan for Subscription");
        planDTO.setNumeroPlacasContratadas(25);
        planDTO.setDescuentoCorporativoAdicional(10.00);
        planDTO.setPrecioPlanCorporativo(10000.00);
        planDTO.setFechaInicio("2024-01-01");
        planDTO.setFechaFin("2024-12-31");
        planDTO.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(planDTO);
        PlanCorporativo plan = planCorporativoRepository.findAll().get(0);

        SuscripcionFlotillaDTO suscripcionDTO = new SuscripcionFlotillaDTO();
        suscripcionDTO.setIdPlanCorporativo(plan.getIdPlanCorporativo());
        suscripcionDTO.setIdVehiculo(testVehiculo.getId());

        // Act
        String result = gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);

        // Assert
        assertEquals("Vehículo suscrito al plan corporativo con éxito.", result);

        List<SuscripcionFlotilla> suscripciones = suscripcionFlotillaRepository.findAll();
        assertEquals(1, suscripciones.size());

        SuscripcionFlotilla suscripcion = suscripciones.get(0);
        assertEquals(plan.getIdPlanCorporativo(), suscripcion.getPlanCorporativo().getIdPlanCorporativo());
        assertEquals(testVehiculo.getId(), suscripcion.getVehiculo().getId());
        assertEquals(SuscripcionFlotilla.EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        assertNotNull(suscripcion.getFechaAsignacion());
    }

    @Test
    void testCancelarSuscripcionVehiculoPlanCorporativo_Integration() {
        // Arrange - Create plan and subscription first
        NuevoPlanCorporativoDTO planDTO = new NuevoPlanCorporativoDTO();
        planDTO.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        planDTO.setIdTipoPlan(testTipoPlan.getId());
        planDTO.setNombrePlanCorporativo("Test Plan for Cancellation");
        planDTO.setNumeroPlacasContratadas(25);
        planDTO.setDescuentoCorporativoAdicional(10.00);
        planDTO.setPrecioPlanCorporativo(10000.00);
        planDTO.setFechaInicio("2024-01-01");
        planDTO.setFechaFin("2024-12-31");
        planDTO.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(planDTO);
        PlanCorporativo plan = planCorporativoRepository.findAll().get(0);

        SuscripcionFlotillaDTO suscripcionDTO = new SuscripcionFlotillaDTO();
        suscripcionDTO.setIdPlanCorporativo(plan.getIdPlanCorporativo());
        suscripcionDTO.setIdVehiculo(testVehiculo.getId());

        gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);
        SuscripcionFlotilla suscripcion = suscripcionFlotillaRepository.findAll().get(0);

        // Act
        String result = gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(suscripcion.getIdSuscripcionFlotilla());

        // Assert
        assertEquals("Suscripción de vehículo a plan corporativo cancelada con éxito.", result);

        SuscripcionFlotilla suscripcionCancelada = suscripcionFlotillaRepository.findById(suscripcion.getIdSuscripcionFlotilla()).orElse(null);
        assertNotNull(suscripcionCancelada);
        assertEquals(SuscripcionFlotilla.EstadoSuscripcion.INACTIVA, suscripcionCancelada.getEstado());
    }

    @Test
    void testObtenerDetalleEmpresasFlotilla_Integration() {
        // Arrange - Create complete scenario
        NuevoPlanCorporativoDTO planDTO = new NuevoPlanCorporativoDTO();
        planDTO.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        planDTO.setIdTipoPlan(testTipoPlan.getId());
        planDTO.setNombrePlanCorporativo("Full Integration Test Plan");
        planDTO.setNumeroPlacasContratadas(75);
        planDTO.setDescuentoCorporativoAdicional(25.00);
        planDTO.setPrecioPlanCorporativo(30000.00);
        planDTO.setFechaInicio("2024-01-01");
        planDTO.setFechaFin("2024-12-31");
        planDTO.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(planDTO);
        PlanCorporativo plan = planCorporativoRepository.findAll().get(0);

        SuscripcionFlotillaDTO suscripcionDTO = new SuscripcionFlotillaDTO();
        suscripcionDTO.setIdPlanCorporativo(plan.getIdPlanCorporativo());
        suscripcionDTO.setIdVehiculo(testVehiculo.getId());
        gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);

        // Act
        DetalleEmpresaFlotillaDTO result = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEmpresasFlotilla());
        assertEquals(1, result.getEmpresasFlotilla().size());

        DetalleEmpresaFlotillaDTO.EmpresaFlotillaDTO empresaDTO = result.getEmpresasFlotilla().get(0);
        assertEquals(testEmpresaFlotilla.getIdEmpresaFlotilla(), empresaDTO.getIdEmpresaFlotilla());
        assertEquals("Test Fleet Company", empresaDTO.getNombreEmpresa());
        assertEquals("Test Fleet Company S.A.", empresaDTO.getRazonSocial());
        assertEquals("9876543-2", empresaDTO.getNit());
        assertEquals("ACTIVA", empresaDTO.getEstado());

        assertEquals(1, empresaDTO.getPlanesCorporativos().size());
        DetalleEmpresaFlotillaDTO.PlanCorporativoDTO planResultDTO = empresaDTO.getPlanesCorporativos().get(0);
        assertEquals("Full Integration Test Plan", planResultDTO.getNombrePlanCorporativo());
        assertEquals(75, planResultDTO.getNumeroPlacasContratadas());
        assertEquals(25.00, planResultDTO.getDescuentoCorporativoAdicional());
        assertEquals(30000.00, planResultDTO.getPrecioPlanCorporativo());
        assertEquals("WORKWEEK", planResultDTO.getTipoPlan());
        assertEquals("ACTIVO", planResultDTO.getEstado());

        assertEquals(1, planResultDTO.getSuscripcionesVehiculos().size());
        DetalleEmpresaFlotillaDTO.SuscripcionVehiculoDTO suscripcionResultDTO = planResultDTO.getSuscripcionesVehiculos().get(0);
        assertEquals("ABC123", suscripcionResultDTO.getPlacaVehiculo());
        assertEquals("ACTIVA", suscripcionResultDTO.getEstado());
        assertNotNull(suscripcionResultDTO.getFechaAsignacion());
    }

    @Test
    void testObtenerVehiculos_Integration() {
        // Arrange - Create additional vehicles
        Persona otraPersona = new Persona();
        otraPersona.setNombre("Another");
        otraPersona.setApellido("Owner");
        otraPersona.setFechaNacimiento(LocalDate.of(1985, 6, 15));
        otraPersona.setDpi(generateUniqueDpi());
        otraPersona.setCorreo("another@test.com");
        otraPersona.setTelefono("87654321");
        otraPersona.setDireccionCompleta("Another Address");
        otraPersona.setCiudad("Another City");
        otraPersona.setPais("Another Country");
        otraPersona.setCodigoPostal("54321");
        otraPersona.setEstado(Persona.Estado.ACTIVO);
        otraPersona = personaRepository.save(otraPersona);

        Vehiculo otroVehiculo = new Vehiculo();
        otroVehiculo.setPropietario(otraPersona);
        otroVehiculo.setPlaca("XYZ789");
        otroVehiculo.setMarca("Honda");
        otroVehiculo.setModelo("Civic");
        otroVehiculo.setColor("Azul");
        otroVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        otroVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        vehiculoRepository.save(otroVehiculo);

        // Act
        List<VehiculoClienteDTO> result = gestionEmpresaFlotillaService.obtenerVehiculos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // testVehiculo + otroVehiculo

        // Verify both vehicles are returned
        List<String> placas = result.stream().map(VehiculoClienteDTO::getPlaca).toList();
        assertTrue(placas.contains("ABC123"));
        assertTrue(placas.contains("XYZ789"));

        // Verify first vehicle details
        VehiculoClienteDTO vehiculo1 = result.stream()
                .filter(v -> "ABC123".equals(v.getPlaca()))
                .findFirst()
                .orElse(null);
        assertNotNull(vehiculo1);
        assertEquals(testVehiculo.getId(), vehiculo1.getIdVehiculo());
        assertEquals("Toyota", vehiculo1.getMarca());
        assertEquals("Corolla", vehiculo1.getModelo());
        assertEquals("Blanco", vehiculo1.getColor());
        assertEquals("CUATRO_RUEDAS", vehiculo1.getTipoVehiculo());

        // Verify second vehicle details
        VehiculoClienteDTO vehiculo2 = result.stream()
                .filter(v -> "XYZ789".equals(v.getPlaca()))
                .findFirst()
                .orElse(null);
        assertNotNull(vehiculo2);
        assertEquals("Honda", vehiculo2.getMarca());
        assertEquals("Civic", vehiculo2.getModelo());
        assertEquals("Azul", vehiculo2.getColor());
        assertEquals("DOS_RUEDAS", vehiculo2.getTipoVehiculo());
    }

    @Test
    void testErrorHandling_EmpresaFlotillaNotFound_Integration() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
            dto.setIdEmpresaFlotilla(999L);
            dto.setIdTipoPlan(testTipoPlan.getId());
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Empresa de flotilla no encontrada.", exception.getMessage());
    }

    @Test
    void testErrorHandling_TipoPlanNotFound_Integration() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
            dto.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
            dto.setIdTipoPlan(999L);
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Tipo de plan no encontrado.", exception.getMessage());
    }

    @Test
    void testErrorHandling_UsuarioCreadorNotFound_Integration() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            NuevoPlanCorporativoDTO dto = new NuevoPlanCorporativoDTO();
            dto.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
            dto.setIdTipoPlan(testTipoPlan.getId());
            dto.setNombrePlanCorporativo("Test Plan");
            dto.setNumeroPlacasContratadas(10);
            dto.setDescuentoCorporativoAdicional(5.00);
            dto.setPrecioPlanCorporativo(5000.00);
            dto.setFechaInicio("2024-01-01");
            dto.setFechaFin("2024-12-31");
            dto.setIdCreadoPor(999L); // Usuario inexistente
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto);
        });

        assertEquals("Usuario creador no encontrado.", exception.getMessage());
    }

    @Test
    void testErrorHandling_PlanCorporativoAlreadyExists_Integration() {
        // Arrange - Create first plan
        NuevoPlanCorporativoDTO dto1 = new NuevoPlanCorporativoDTO();
        dto1.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        dto1.setIdTipoPlan(testTipoPlan.getId());
        dto1.setNombrePlanCorporativo("First Plan");
        dto1.setNumeroPlacasContratadas(25);
        dto1.setDescuentoCorporativoAdicional(10.00);
        dto1.setPrecioPlanCorporativo(10000.00);
        dto1.setFechaInicio("2024-01-01");
        dto1.setFechaFin("2024-12-31");
        dto1.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto1);

        // Try to create second active plan
        NuevoPlanCorporativoDTO dto2 = new NuevoPlanCorporativoDTO();
        dto2.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        dto2.setIdTipoPlan(testTipoPlan.getId());
        dto2.setNombrePlanCorporativo("Second Plan");
        dto2.setNumeroPlacasContratadas(30);
        dto2.setDescuentoCorporativoAdicional(15.00);
        dto2.setPrecioPlanCorporativo(12000.00);
        dto2.setFechaInicio("2024-01-01");
        dto2.setFechaFin("2024-12-31");
        dto2.setIdCreadoPor(testUsuario.getIdUsuario());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.nuevoPlanCorporativo(dto2);
        });

        assertEquals("Ya existe un plan corporativo activo para esta empresa de flotilla.", exception.getMessage());
    }

    @Test
    void testVehiculoAlreadySubscribed_Integration() {
        // Arrange - Create plan and subscription
        NuevoPlanCorporativoDTO planDTO = new NuevoPlanCorporativoDTO();
        planDTO.setIdEmpresaFlotilla(testEmpresaFlotilla.getIdEmpresaFlotilla());
        planDTO.setIdTipoPlan(testTipoPlan.getId());
        planDTO.setNombrePlanCorporativo("Test Plan");
        planDTO.setNumeroPlacasContratadas(25);
        planDTO.setDescuentoCorporativoAdicional(10.00);
        planDTO.setPrecioPlanCorporativo(10000.00);
        planDTO.setFechaInicio("2024-01-01");
        planDTO.setFechaFin("2024-12-31");
        planDTO.setIdCreadoPor(testUsuario.getIdUsuario());

        gestionEmpresaFlotillaService.nuevoPlanCorporativo(planDTO);
        PlanCorporativo plan = planCorporativoRepository.findAll().get(0);

        SuscripcionFlotillaDTO suscripcionDTO = new SuscripcionFlotillaDTO();
        suscripcionDTO.setIdPlanCorporativo(plan.getIdPlanCorporativo());
        suscripcionDTO.setIdVehiculo(testVehiculo.getId());

        gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);

        // Try to subscribe same vehicle again
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);
        });

        assertEquals("El vehículo ya está suscrito a este plan corporativo.", exception.getMessage());
    }

    @Test
    void testObtenerVehiculos_EmptyList_Integration() {
        // Arrange - Remove test vehicle
        vehiculoRepository.delete(testVehiculo);

        // Act
        List<VehiculoClienteDTO> result = gestionEmpresaFlotillaService.obtenerVehiculos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCompleteBusinessFlow_Integration() {
        // Complete business flow test covering all major operations
        
        // 1. Create empresa flotilla
        NuevaEmpresaFlotillaDTO empresaDTO = new NuevaEmpresaFlotillaDTO();
        empresaDTO.setNombreEmpresa("Complete Flow Fleet");
        empresaDTO.setRazonSocial("Complete Flow Fleet S.A.");
        empresaDTO.setNit("5555666-7");
        empresaDTO.setTelefono("55556666");
        empresaDTO.setCorreoContacto("complete@flow.com");
        empresaDTO.setDireccion("Complete Flow Address");

        String empresaResult = gestionEmpresaFlotillaService.nuevaEmpresaFlotilla(empresaDTO);
        assertEquals("Nueva empresa de flotilla creada con éxito.", empresaResult);

        EmpresaFlotilla newFlotilla = empresaFlotillaRepository.findAll().stream()
                .filter(e -> "Complete Flow Fleet".equals(e.getNombreEmpresa()))
                .findFirst()
                .orElse(null);
        assertNotNull(newFlotilla);

        // 2. Create plan corporativo
        NuevoPlanCorporativoDTO planDTO = new NuevoPlanCorporativoDTO();
        planDTO.setIdEmpresaFlotilla(newFlotilla.getIdEmpresaFlotilla());
        planDTO.setIdTipoPlan(testTipoPlan.getId());
        planDTO.setNombrePlanCorporativo("Complete Flow Plan");
        planDTO.setNumeroPlacasContratadas(100);
        planDTO.setDescuentoCorporativoAdicional(20.00);
        planDTO.setPrecioPlanCorporativo(50000.00);
        planDTO.setFechaInicio("2024-01-01");
        planDTO.setFechaFin("2024-12-31");
        planDTO.setIdCreadoPor(testUsuario.getIdUsuario());

        String planResult = gestionEmpresaFlotillaService.nuevoPlanCorporativo(planDTO);
        assertEquals("Nuevo plan corporativo creado con éxito.", planResult);

        PlanCorporativo newPlan = planCorporativoRepository.findAll().stream()
                .filter(p -> "Complete Flow Plan".equals(p.getNombrePlanCorporativo()))
                .findFirst()
                .orElse(null);
        assertNotNull(newPlan);

        // 3. Subscribe vehicle
        SuscripcionFlotillaDTO suscripcionDTO = new SuscripcionFlotillaDTO();
        suscripcionDTO.setIdPlanCorporativo(newPlan.getIdPlanCorporativo());
        suscripcionDTO.setIdVehiculo(testVehiculo.getId());

        String suscripcionResult = gestionEmpresaFlotillaService.suscribirVehiculoPlanCorporativo(suscripcionDTO);
        assertEquals("Vehículo suscrito al plan corporativo con éxito.", suscripcionResult);

        // 4. Get details
        DetalleEmpresaFlotillaDTO detalles = gestionEmpresaFlotillaService.obtenerDetalleEmpresasFlotilla(testUsuario.getIdUsuario());
        assertNotNull(detalles);
        assertTrue(detalles.getEmpresasFlotilla().size() >= 2); // original + new

        // 5. Cancel subscription
        SuscripcionFlotilla suscripcion = suscripcionFlotillaRepository.findAll().get(0);
        String cancelResult = gestionEmpresaFlotillaService.cancelarSuscripcionVehiculoPlanCorporativo(suscripcion.getIdSuscripcionFlotilla());
        assertEquals("Suscripción de vehículo a plan corporativo cancelada con éxito.", cancelResult);

        // 6. Deactivate plan
        String deactivateResult = gestionEmpresaFlotillaService.desactivarPlanCorporativo(newPlan.getIdPlanCorporativo());
        assertEquals("Plan corporativo desactivado con éxito.", deactivateResult);

        // 7. Reactivate plan
        String activateResult = gestionEmpresaFlotillaService.activarPlanCorporativo(newPlan.getIdPlanCorporativo());
        assertEquals("Plan corporativo activado con éxito.", activateResult);

        // Verify final state
        PlanCorporativo finalPlan = planCorporativoRepository.findById(newPlan.getIdPlanCorporativo()).orElse(null);
        assertNotNull(finalPlan);
        assertEquals(PlanCorporativo.EstadoPlanCorporativo.ACTIVO, finalPlan.getEstado());

        SuscripcionFlotilla finalSuscripcion = suscripcionFlotillaRepository.findById(suscripcion.getIdSuscripcionFlotilla()).orElse(null);
        assertNotNull(finalSuscripcion);
        assertEquals(SuscripcionFlotilla.EstadoSuscripcion.INACTIVA, finalSuscripcion.getEstado());
    }

    private String generateUniqueDpi() {
        // Usar nanoTime para mayor unicidad
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
