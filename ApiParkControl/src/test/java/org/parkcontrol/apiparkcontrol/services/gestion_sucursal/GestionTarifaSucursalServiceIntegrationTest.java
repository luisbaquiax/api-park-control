package org.parkcontrol.apiparkcontrol.services.gestion_sucursal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
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
class GestionTarifaSucursalServiceIntegrationTest {

    @Autowired
    private GestionTarifaSucursalService gestionTarifaSucursalService;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;

    @Autowired
    private TarifaBaseRepository tarifaBaseRepository;

    @Autowired
    private BitacoraTarifaSucursalRepository bitacoraTarifaSucursalRepository;

    private Empresa testEmpresa;
    private Usuario testUsuario;
    private Sucursal testSucursal;
    private TarifaBase testTarifaBase;
    private Rol sucursalRol;

    @BeforeEach
    void setUp() {
        // Clean up database
        bitacoraTarifaSucursalRepository.deleteAll();
        tarifaSucursalRepository.deleteAll();
        tarifaBaseRepository.deleteAll();
        sucursalRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        empresaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        sucursalRol = new Rol();
        sucursalRol.setNombreRol("SUCURSAL");
        sucursalRol.setDescripcion("Usuario de sucursal");
        sucursalRol = rolRepository.save(sucursalRol);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Fiscal Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test usuario and persona first
        Persona persona = new Persona();
        persona.setNombre("Test");
        persona.setApellido("User");
        persona.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
        persona.setDpi(generateUniqueDpi());
        persona.setCorreo("test@test.com");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        testUsuario = new Usuario();
        testUsuario.setPersona(persona);
        testUsuario.setRol(sucursalRol);
        testUsuario.setNombreUsuario("testuser");
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuario);
        testSucursal.setNombre("Test Sucursal");
        testSucursal.setDireccionCompleta("Test Address");
        testSucursal.setCiudad("Test City");
        testSucursal.setDepartamento("Test Department");
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(30);
        testSucursal.setLatitud(new BigDecimal("14.634915"));
        testSucursal.setLongitud(new BigDecimal("-90.506882"));
        testSucursal.setTelefonoContacto("87654321");
        testSucursal.setCorreoContacto("sucursal@test.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);

        // Create test tarifa base AFTER creating empresa, usuario and sucursal
        testTarifaBase = new TarifaBase();
        testTarifaBase.setEmpresa(testEmpresa);
        testTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        testTarifaBase.setMoneda("GTQ");
        testTarifaBase.setFechaVigenciaInicio(LocalDate.now().minusDays(30)); // Cambiar a LocalDate
        testTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        testTarifaBase = tarifaBaseRepository.save(testTarifaBase);
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithCustomPrice_Integration() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora(String.valueOf(12.75));
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin("2024-12-31");

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);

        // Verify tarifa was created
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());

        TarifaSucursal tarifaCreada = tarifas.get(0);
        assertEquals(new BigDecimal("12.75"), tarifaCreada.getPrecioPorHora());
        assertEquals("GTQ", tarifaCreada.getMoneda());
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.VIGENTE, tarifaCreada.getEstado());
        assertNotNull(tarifaCreada.getFechaVigenciaInicio());
        assertNotNull(tarifaCreada.getFechaVigenciaFin());

        // Verify bitacora was created
        List<BitacoraTarifaSucursal> bitacoras = bitacoraTarifaSucursalRepository.findAll();
        assertEquals(1, bitacoras.size());

        BitacoraTarifaSucursal bitacora = bitacoras.get(0);
        assertEquals(BitacoraTarifaSucursal.Accion.CREACION, bitacora.getAccion());
        assertEquals(new BigDecimal("12.75"), bitacora.getPrecioNuevo());
        assertEquals("Creación de nueva tarifa de sucursal.", bitacora.getObservaciones());
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithTarifaBase_Integration() throws Exception {
        // Verify tarifa base exists before test
        TarifaBase verificaTarifaBase = tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(
                testEmpresa.getIdEmpresa(), TarifaBase.EstadoTarifaBase.VIGENTE);
        assertNotNull(verificaTarifaBase, "TarifaBase should exist before test");

        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(true);
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);

        // Verify tarifa was created with base price
        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());

        TarifaSucursal tarifaCreada = tarifas.get(0);
        assertEquals(0, testTarifaBase.getPrecioPorHora().compareTo(tarifaCreada.getPrecioPorHora()));
        assertEquals("GTQ", tarifaCreada.getMoneda());
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.VIGENTE, tarifaCreada.getEstado());
    }

    @Test
    void testCrearNuevaTarifaSucursal_ReplacesExistingVigente_Integration() throws Exception {
        // Create existing vigente tarifa
        TarifaSucursal existingTarifa = new TarifaSucursal();
        existingTarifa.setSucursal(testSucursal);
        existingTarifa.setPrecioPorHora(new BigDecimal("10.00"));
        existingTarifa.setMoneda("GTQ");
        existingTarifa.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        existingTarifa.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        tarifaSucursalRepository.save(existingTarifa);

        // Arrange new tarifa
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora(String.valueOf(20.00));
        nuevaTarifaDTO.setMoneda("USD");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);

        // Verify existing tarifa is now historic
        TarifaSucursal updatedExisting = tarifaSucursalRepository.findById(existingTarifa.getIdTarifaSucursal()).orElse(null);
        assertNotNull(updatedExisting);
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.HISTORICO, updatedExisting.getEstado());

        // Verify new tarifa is vigente
        List<TarifaSucursal> vigenteTarifas = tarifaSucursalRepository.findBySucursal_IdSucursalAndEstado(
                testSucursal.getIdSucursal(), TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        assertEquals(1, vigenteTarifas.size());
        assertEquals(0, new BigDecimal("20.00").compareTo(vigenteTarifas.get(0).getPrecioPorHora())); // Cambiar comparación
        assertEquals("USD", vigenteTarifas.get(0).getMoneda());
    }

    @Test
    void testEditarTarifaSucursal_Integration() throws Exception {
        // Create tarifa to edit
        TarifaSucursal tarifa = new TarifaSucursal();
        tarifa.setSucursal(testSucursal);
        tarifa.setPrecioPorHora(new BigDecimal("10.50"));
        tarifa.setMoneda("GTQ");
        tarifa.setFechaVigenciaInicio(LocalDateTime.of(2024, 1, 1, 0, 0));
        tarifa.setFechaVigenciaFin(LocalDateTime.of(2024, 12, 31, 23, 59));
        tarifa.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        tarifa = tarifaSucursalRepository.save(tarifa);

        // Arrange edit DTO
        TarifaSucursalDTO tarifaDTO = new TarifaSucursalDTO();
        tarifaDTO.setIdTarifaSucursal(tarifa.getIdTarifaSucursal());
        tarifaDTO.setPrecioPorHora(25.00);
        tarifaDTO.setMoneda("USD");
        tarifaDTO.setFechaVigenciaInicio("2024-06-01");
        tarifaDTO.setFechaVigenciaFin("2024-12-31");
        tarifaDTO.setEstado("HISTORICO");

        // Act
        String result = gestionTarifaSucursalService.editarTarifaSucursal(tarifaDTO);

        // Assert
        assertEquals("Tarifa de sucursal editada exitosamente.", result);

        // Verify tarifa was updated
        TarifaSucursal updatedTarifa = tarifaSucursalRepository.findById(tarifa.getIdTarifaSucursal()).orElse(null);
        assertNotNull(updatedTarifa);
        assertEquals(0, new BigDecimal("25.00").compareTo(updatedTarifa.getPrecioPorHora())); // Cambiar comparación
        assertEquals("USD", updatedTarifa.getMoneda());
        assertEquals(TarifaSucursal.EstadoTarifaSucursal.HISTORICO, updatedTarifa.getEstado());

        // Verify bitacora was created
        List<BitacoraTarifaSucursal> bitacoras = bitacoraTarifaSucursalRepository.findAll();
        assertEquals(1, bitacoras.size());

        BitacoraTarifaSucursal bitacora = bitacoras.get(0);
        assertEquals(BitacoraTarifaSucursal.Accion.ACTUALIZACION, bitacora.getAccion());
        assertEquals(0, new BigDecimal("10.50").compareTo(bitacora.getPrecioAnterior())); // Cambiar comparación
        assertEquals(0, new BigDecimal("25.00").compareTo(bitacora.getPrecioNuevo())); // Cambiar comparación
        assertEquals("Edición de tarifa de sucursal.", bitacora.getObservaciones());
    }

    @Test
    void testObtenerTarifasPorIdUsuario_Integration() throws Exception {
        // Create multiple tarifas
        TarifaSucursal tarifa1 = new TarifaSucursal();
        tarifa1.setSucursal(testSucursal);
        tarifa1.setPrecioPorHora(new BigDecimal("10.50"));
        tarifa1.setMoneda("GTQ");
        tarifa1.setFechaVigenciaInicio(LocalDateTime.of(2024, 1, 1, 0, 0));
        tarifa1.setFechaVigenciaFin(LocalDateTime.of(2024, 12, 31, 23, 59));
        tarifa1.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        tarifaSucursalRepository.save(tarifa1);

        TarifaSucursal tarifa2 = new TarifaSucursal();
        tarifa2.setSucursal(testSucursal);
        tarifa2.setPrecioPorHora(new BigDecimal("8.00"));
        tarifa2.setMoneda("GTQ");
        tarifa2.setFechaVigenciaInicio(LocalDateTime.of(2023, 1, 1, 0, 0));
        tarifa2.setFechaVigenciaFin(null);
        tarifa2.setEstado(TarifaSucursal.EstadoTarifaSucursal.HISTORICO);
        tarifaSucursalRepository.save(tarifa2);

        // Act
        List<TarifaSucursalDTO> result = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Find vigente tarifa
        TarifaSucursalDTO vigenteTarifa = result.stream()
                .filter(t -> "VIGENTE".equals(t.getEstado()))
                .findFirst()
                .orElse(null);
        assertNotNull(vigenteTarifa);
        assertEquals(10.50, vigenteTarifa.getPrecioPorHora());
        assertEquals("GTQ", vigenteTarifa.getMoneda());
        assertNotNull(vigenteTarifa.getFechaVigenciaInicio());
        assertNotNull(vigenteTarifa.getFechaVigenciaFin());

        // Find historic tarifa
        TarifaSucursalDTO historicTarifa = result.stream()
                .filter(t -> "HISTORICO".equals(t.getEstado()))
                .findFirst()
                .orElse(null);
        assertNotNull(historicTarifa);
        assertEquals(8.00, historicTarifa.getPrecioPorHora());
        assertNull(historicTarifa.getFechaVigenciaFin());
    }

    @Test
    void testCrearNuevaTarifaSucursal_UserNotFound_Integration() {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(999L);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);
        });

        assertEquals("Sucursal no encontrada para el usuario proporcionado.", exception.getMessage());
    }

    @Test
    void testCrearNuevaTarifaSucursal_TarifaBaseNotFound_Integration() throws Exception {
        // Remove tarifa base
        tarifaBaseRepository.delete(testTarifaBase);

        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(true);
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);
        });

        assertEquals("No existe una tarifa base para la empresa de la sucursal.", exception.getMessage());
    }

    @Test
    void testEditarTarifaSucursal_TarifaNotFound_Integration() {
        // Arrange
        TarifaSucursalDTO tarifaDTO = new TarifaSucursalDTO();
        tarifaDTO.setIdTarifaSucursal(999L);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            gestionTarifaSucursalService.editarTarifaSucursal(tarifaDTO);
        });

        assertEquals("Tarifa de sucursal no encontrada.", exception.getMessage());
    }

    @Test
    void testObtenerTarifasPorIdUsuario_EmptyResult_Integration() throws Exception {
        // Act
        List<TarifaSucursalDTO> result = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithoutFechaFin_Integration() throws Exception {
        // Arrange
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora(String.valueOf(18.50));
        nuevaTarifaDTO.setMoneda("EUR");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-03-01");
        // No fechaVigenciaFin

        // Act
        String result = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);

        // Assert
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", result);

        List<TarifaSucursal> tarifas = tarifaSucursalRepository.findBySucursal_IdSucursal(testSucursal.getIdSucursal());
        assertEquals(1, tarifas.size());

        TarifaSucursal tarifaCreada = tarifas.get(0);
        assertEquals(0, new BigDecimal("18.50").compareTo(tarifaCreada.getPrecioPorHora())); // Cambiar comparación
        assertEquals("EUR", tarifaCreada.getMoneda());
        assertNull(tarifaCreada.getFechaVigenciaFin());
    }

    @Test
    void testCompleteWorkflow_Integration() throws Exception {
        // Test complete workflow: create, edit, get tarifas

        // Step 1: Create new tarifa
        NuevaTarifaSucursalDTO nuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        nuevaTarifaDTO.setIdUsuarioSucursal(testUsuario.getIdUsuario());
        nuevaTarifaDTO.setEsTarifaBase(false);
        nuevaTarifaDTO.setPrecioPorHora(String.valueOf(15.75));
        nuevaTarifaDTO.setMoneda("GTQ");
        nuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        nuevaTarifaDTO.setFechaVigenciaFin("2024-12-31");

        String createResult = gestionTarifaSucursalService.crearNuevaTarifaSucursal(nuevaTarifaDTO);
        assertEquals("Nueva tarifa de sucursal creada exitosamente.", createResult);

        // Step 2: Get tarifas to find the created one
        List<TarifaSucursalDTO> tarifas = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(testUsuario.getIdUsuario());
        assertEquals(1, tarifas.size());

        TarifaSucursalDTO createdTarifa = tarifas.get(0);
        assertEquals(15.75, createdTarifa.getPrecioPorHora());
        assertEquals("GTQ", createdTarifa.getMoneda());
        assertEquals("VIGENTE", createdTarifa.getEstado());

        // Step 3: Edit the tarifa
        TarifaSucursalDTO editDTO = new TarifaSucursalDTO();
        editDTO.setIdTarifaSucursal(createdTarifa.getIdTarifaSucursal());
        editDTO.setPrecioPorHora(22.50);
        editDTO.setMoneda("USD");
        editDTO.setFechaVigenciaInicio("2024-02-01");
        editDTO.setFechaVigenciaFin("2024-11-30");
        editDTO.setEstado("HISTORICO");

        String editResult = gestionTarifaSucursalService.editarTarifaSucursal(editDTO);
        assertEquals("Tarifa de sucursal editada exitosamente.", editResult);

        // Step 4: Verify changes
        List<TarifaSucursalDTO> updatedTarifas = gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(testUsuario.getIdUsuario());
        assertEquals(1, updatedTarifas.size());

        TarifaSucursalDTO updatedTarifa = updatedTarifas.get(0);
        assertEquals(22.50, updatedTarifa.getPrecioPorHora());
        assertEquals("USD", updatedTarifa.getMoneda());
        assertEquals("HISTORICO", updatedTarifa.getEstado());

        // Verify bitacoras were created
        List<BitacoraTarifaSucursal> bitacoras = bitacoraTarifaSucursalRepository.findAll();
        assertEquals(2, bitacoras.size()); // One for creation, one for edit
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
