package org.parkcontrol.apiparkcontrol.services.empresa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

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
class TarifaBaseServiceIntegrationTest {

    @Autowired
    private TarifaBaseService tarifaBaseService;

    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;

    @Autowired
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private TipoPlanRepository tipoPlanRepository;

    private Usuario testUsuario;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private Rol empresaRol;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        bitacoraTarifaBaseRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        suscripcionRepository.deleteAll();
        tipoPlanRepository.deleteAll();
        vehiculoRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Crear rol
        empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario de empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Crear persona
        Persona persona = new Persona();
        persona.setNombre("Test");
        persona.setApellido("User");
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        persona.setDpi(generateUniqueDpi());
        persona.setCorreo("test@empresa.com");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        // Crear usuario
        testUsuario = new Usuario();
        testUsuario.setPersona(persona);
        testUsuario.setRol(empresaRol);
        testUsuario.setNombreUsuario("empresauser");
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);

        // Crear empresa
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

        // Crear sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuario);
        testSucursal.setNombre("Sucursal Test");
        testSucursal.setDireccionCompleta("Dirección Test");
        testSucursal.setCiudad("Ciudad Test");
        testSucursal.setDepartamento("Departamento Test");
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(100);
        testSucursal.setLatitud(BigDecimal.valueOf(14.6349));
        testSucursal.setLongitud(BigDecimal.valueOf(-90.5069));
        testSucursal.setTelefonoContacto("12345678");
        testSucursal.setCorreoContacto("sucursal@test.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);
    }

    @Test
    void testCreate_IntegrationSuccess_FechaHoy() {
        // Arrange
        TarifaBaseResponse tarifaRequest = new TarifaBaseResponse();
        tarifaRequest.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaRequest.setMoneda("GTQ");
        tarifaRequest.setFechaVigenciaInicio(LocalDate.now());
        tarifaRequest.setFechaVigenciaFin(LocalDate.now().plusMonths(1));

        // Act
        TarifaBase result = tarifaBaseService.create(tarifaRequest, testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getIdTarifaBase());
        assertEquals(BigDecimal.valueOf(50), result.getPrecioPorHora());
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, result.getEstado());
        assertEquals(testEmpresa.getIdEmpresa(), result.getEmpresa().getIdEmpresa());

        // Verificar bitácora
        List<BitacoraTarifaBase> bitacoras = bitacoraTarifaBaseRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaBase.Accion.CREACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testCreate_IntegrationSuccess_FechaProgramado() {
        // Arrange
        TarifaBaseResponse tarifaRequest = new TarifaBaseResponse();
        tarifaRequest.setPrecioPorHora(BigDecimal.valueOf(60));
        tarifaRequest.setMoneda("GTQ");
        tarifaRequest.setFechaVigenciaInicio(LocalDate.now().plusDays(7));
        tarifaRequest.setFechaVigenciaFin(LocalDate.now().plusMonths(1));

        // Act
        TarifaBase result = tarifaBaseService.create(tarifaRequest, testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(TarifaBase.EstadoTarifaBase.PROGRAMADO, result.getEstado());
    }

    @Test
    void testUpdate_IntegrationSuccess_ConCambioDePrecio() {
        // Arrange - Crear tarifa inicial
        TarifaBase tarifaInicial = new TarifaBase();
        tarifaInicial.setEmpresa(testEmpresa);
        tarifaInicial.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaInicial.setFechaVigenciaInicio(LocalDate.now());
        tarifaInicial.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaInicial.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaInicial = tarifaBaseRepository.save(tarifaInicial);

        // Preparar update
        TarifaBaseResponse updateRequest = new TarifaBaseResponse();
        updateRequest.setIdTarifaBase(tarifaInicial.getIdTarifaBase());
        updateRequest.setPrecioPorHora(BigDecimal.valueOf(70));
        updateRequest.setMoneda("GTQ");
        updateRequest.setFechaVigenciaInicio(LocalDate.now());
        updateRequest.setFechaVigenciaFin(LocalDate.now().plusMonths(1));

        // Act
        TarifaBase result = tarifaBaseService.update(updateRequest, testUsuario.getIdUsuario());

        // Assert
        assertEquals(TarifaBase.EstadoTarifaBase.HISTORICO, result.getEstado());
        
        // Verificar que se creó nueva tarifa
        List<TarifaBase> tarifas = tarifaBaseRepository.findAll();
        assertEquals(2, tarifas.size());
        
        // Verificar bitácora
        List<BitacoraTarifaBase> bitacoras = bitacoraTarifaBaseRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaBase.Accion.ACTUALIZACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testActivarTarifaBase_IntegrationSuccess() {
        // Arrange
        TarifaBase tarifaProgramada = new TarifaBase();
        tarifaProgramada.setEmpresa(testEmpresa);
        tarifaProgramada.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaProgramada.setFechaVigenciaInicio(LocalDate.now());
        tarifaProgramada.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaProgramada.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);
        tarifaProgramada = tarifaBaseRepository.save(tarifaProgramada);

        // Act
        MessageSuccess result = tarifaBaseService.activarTarifaBase(
                tarifaProgramada.getIdTarifaBase(), 
                testUsuario.getIdUsuario());

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("Tarifa activada correctamente", result.getMessage());
        
        // Verificar cambio de estado
        TarifaBase tarifaActualizada = tarifaBaseRepository.findById(tarifaProgramada.getIdTarifaBase())
                .orElseThrow();
        assertEquals(TarifaBase.EstadoTarifaBase.VIGENTE, tarifaActualizada.getEstado());
        
        // Verificar bitácora
        List<BitacoraTarifaBase> bitacoras = bitacoraTarifaBaseRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaBase.Accion.ACTIVACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testDesactivarTarifaBase_IntegrationSuccess() {
        // Arrange
        TarifaBase tarifaVigente = new TarifaBase();
        tarifaVigente.setEmpresa(testEmpresa);
        tarifaVigente.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaVigente.setFechaVigenciaInicio(LocalDate.now());
        tarifaVigente.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaVigente.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaVigente = tarifaBaseRepository.save(tarifaVigente);

        // Act
        MessageSuccess result = tarifaBaseService.desactivarTarifaBase(
                tarifaVigente.getIdTarifaBase(), 
                testUsuario.getIdUsuario());

        // Assert
        assertEquals(201, result.getCode());
        assertEquals("Tarifa desactivada correctamente", result.getMessage());
        
        // Verificar cambio de estado
        TarifaBase tarifaActualizada = tarifaBaseRepository.findById(tarifaVigente.getIdTarifaBase())
                .orElseThrow();
        assertEquals(TarifaBase.EstadoTarifaBase.HISTORICO, tarifaActualizada.getEstado());
        
        // Verificar bitácora
        List<BitacoraTarifaBase> bitacoras = bitacoraTarifaBaseRepository.findAll();
        assertEquals(1, bitacoras.size());
        assertEquals(BitacoraTarifaBase.Accion.DESACTIVACION, bitacoras.get(0).getAccion());
    }

    @Test
    void testFindTarifaBaseByEmpresaIdByEstado_IntegrationSuccess() {
        // Arrange
        TarifaBase tarifaVigente = new TarifaBase();
        tarifaVigente.setEmpresa(testEmpresa);
        tarifaVigente.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaVigente.setFechaVigenciaInicio(LocalDate.now());
        tarifaVigente.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaVigente.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaVigente = tarifaBaseRepository.save(tarifaVigente);

        // Act
        TarifaBase result = tarifaBaseService.findTarifaBaseByEmpresaIdByEstado(
                TarifaBase.EstadoTarifaBase.VIGENTE, 
                testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(tarifaVigente.getIdTarifaBase(), result.getIdTarifaBase());
    }

    @Test
    void testTarifaVigenteSucursal_IntegrationConTarifaSucursal() {
        // Arrange - Crear tarifa base
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(testEmpresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBaseRepository.save(tarifaBase);

        // Crear tarifa sucursal
        TarifaSucursal tarifaSucursal = new TarifaSucursal();
        tarifaSucursal.setSucursal(testSucursal);
        tarifaSucursal.setPrecioPorHora(BigDecimal.valueOf(55));
        tarifaSucursal.setMoneda("GTQ");
        tarifaSucursal.setFechaVigenciaInicio(LocalDate.now().atStartOfDay());
        tarifaSucursal.setFechaVigenciaFin(LocalDate.now().plusMonths(1).atStartOfDay());
        tarifaSucursal.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        tarifaSucursalRepository.save(tarifaSucursal);

        // Act
        BigDecimal result = tarifaBaseService.tarifaVigenteSucursal(testSucursal);

        // Assert
        assertEquals(BigDecimal.valueOf(55), result);
    }

    @Test
    void testTarifaVigenteSucursal_IntegrationSinTarifaSucursal() {
        // Arrange - Solo crear tarifa base
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(testEmpresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBaseRepository.save(tarifaBase);

        // Act
        BigDecimal result = tarifaBaseService.tarifaVigenteSucursal(testSucursal);

        // Assert
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void testTarifaCongelada_Integration() {
        // Arrange
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(testEmpresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(45));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now().minusMonths(1));
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBase = tarifaBaseRepository.save(tarifaBase);

        // Crear vehículo y plan para suscripción
        Persona propietario = new Persona();
        propietario.setNombre("Cliente");
        propietario.setApellido("Test");
        propietario.setFechaNacimiento(LocalDate.of(1995, 5, 15));
        propietario.setDpi(generateUniqueDpi());
        propietario.setCorreo("cliente@test.com");
        propietario.setTelefono("87654321");
        propietario.setDireccionCompleta("Dirección Cliente");
        propietario.setCiudad("Ciudad");
        propietario.setPais("Guatemala");
        propietario.setCodigoPostal("01001");
        propietario = personaRepository.save(propietario);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("ABC123");
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setColor("Blanco");
        vehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        vehiculo.setPropietario(propietario);
        vehiculo = vehiculoRepository.save(vehiculo);

        TipoPlan tipoPlan = new TipoPlan();
        tipoPlan.setEmpresa(testEmpresa);
        tipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        tipoPlan.setCodigoPlan("WW001");
        tipoPlan.setDescripcion("Plan Workweek");
        tipoPlan.setPrecioPlan(200.0);
        tipoPlan.setHorasMensuales(160);
        tipoPlan.setHorasDia(8);
        tipoPlan.setDiasAplicables("L-M-X-J-V");
        tipoPlan.setCoberturaHoraria("08:00-18:00");
        tipoPlan.setOrdenBeneficio(2);
        tipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        tipoPlan = tipoPlanRepository.save(tipoPlan);

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEmpresa(testEmpresa);
        suscripcion.setUsuario(testUsuario);
        suscripcion.setVehiculo(vehiculo);
        suscripcion.setTipoPlan(tipoPlan);
        suscripcion.setTarifaBaseReferencia(tarifaBase);
        suscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        suscripcion.setDescuentoAplicado(BigDecimal.valueOf(15));
        suscripcion.setPrecioPlan(BigDecimal.valueOf(200));
        suscripcion.setHorasMensualesIncluidas(160);
        suscripcion.setHorasConsumidas(BigDecimal.ZERO);
        suscripcion.setFechaInicio(LocalDateTime.now());
        suscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion = suscripcionRepository.save(suscripcion);

        // Act
        BigDecimal result = tarifaBaseService.tarfifaCongelada(suscripcion);

        // Assert
        assertEquals(BigDecimal.valueOf(45), result);
    }

    @Test
    void testTarifaBaseVigenteEmpresa_Integration() {
        // Arrange
        TarifaBase tarifaBase = new TarifaBase();
        tarifaBase.setEmpresa(testEmpresa);
        tarifaBase.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaBase.setFechaVigenciaInicio(LocalDate.now());
        tarifaBase.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBaseRepository.save(tarifaBase);

        // Act
        BigDecimal result = tarifaBaseService.tarifaBaseVigenteEmpresa(testEmpresa);

        // Assert
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void testTarifaBaseVigenteEmpresa_Integration_NoTarifaVigente() {
        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.tarifaBaseVigenteEmpresa(testEmpresa));
        assertEquals(404, exception.getStatus());
        assertEquals("No se encontró una tarifa base vigente para la empresa.", exception.getMessage());
    }

    @Test
    void testFindAllByEmpresa_Integration() {
        // Arrange
        TarifaBase tarifa1 = new TarifaBase();
        tarifa1.setEmpresa(testEmpresa);
        tarifa1.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifa1.setFechaVigenciaInicio(LocalDate.now());
        tarifa1.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifa1.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBaseRepository.save(tarifa1);

        TarifaBase tarifa2 = new TarifaBase();
        tarifa2.setEmpresa(testEmpresa);
        tarifa2.setPrecioPorHora(BigDecimal.valueOf(40));
        tarifa2.setFechaVigenciaInicio(LocalDate.now().minusMonths(2));
        tarifa2.setFechaVigenciaFin(LocalDate.now().minusMonths(1));
        tarifa2.setEstado(TarifaBase.EstadoTarifaBase.HISTORICO);
        tarifaBaseRepository.save(tarifa2);

        // Act
        List<TarifaBase> result = tarifaBaseService.findAllByEmpresa(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testCreate_IntegrationFails_Solapamiento() {
        // Arrange - Crear tarifa existente
        TarifaBase tarifaExistente = new TarifaBase();
        tarifaExistente.setEmpresa(testEmpresa);
        tarifaExistente.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaExistente.setFechaVigenciaInicio(LocalDate.now());
        tarifaExistente.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaExistente.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        tarifaBaseRepository.save(tarifaExistente);

        // Intentar crear otra con fechas solapadas
        TarifaBaseResponse tarifaNueva = new TarifaBaseResponse();
        tarifaNueva.setPrecioPorHora(BigDecimal.valueOf(60));
        tarifaNueva.setMoneda("GTQ");
        tarifaNueva.setFechaVigenciaInicio(LocalDate.now().plusDays(15));
        tarifaNueva.setFechaVigenciaFin(LocalDate.now().plusMonths(2));

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.create(tarifaNueva, testUsuario.getIdUsuario()));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ya existe la tarifa"));
    }

    @Test
    void testActivarTarifaBase_IntegrationFails_FechaNoCoincide() {
        // Arrange
        TarifaBase tarifaProgramada = new TarifaBase();
        tarifaProgramada.setEmpresa(testEmpresa);
        tarifaProgramada.setPrecioPorHora(BigDecimal.valueOf(50));
        tarifaProgramada.setFechaVigenciaInicio(LocalDate.now().plusDays(7));
        tarifaProgramada.setFechaVigenciaFin(LocalDate.now().plusMonths(1));
        tarifaProgramada.setEstado(TarifaBase.EstadoTarifaBase.PROGRAMADO);
        tarifaProgramada = tarifaBaseRepository.save(tarifaProgramada);

        final Long tarifaId = tarifaProgramada.getIdTarifaBase();

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class,
                () -> tarifaBaseService.activarTarifaBase(tarifaId, testUsuario.getIdUsuario()));
        assertEquals(409, exception.getStatus());
        assertTrue(exception.getMessage().contains("No se puede activar la tarifa antes"));
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
