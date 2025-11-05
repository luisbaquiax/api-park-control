package org.parkcontrol.apiparkcontrol.services.comercio_afiliado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.comercio_afiliado.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.comercio_afliado.GestionComercioAfiliadoService;

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
class GestionComercioAfiliadoServiceIntegrationTest {

    @Autowired
    private GestionComercioAfiliadoService gestionComercioAfiliadoService;

    @Autowired
    private ComercioAfiliadoRepository comercioAfiliadoRepository;
    @Autowired
    private ConvenioComercioSucursalRepository convenioComercioSucursalRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private TarifaSucursalRepository tarifaSucursalRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;

    private ComercioAfiliado testComercioAfiliado;
    private Usuario testUsuario;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private TarifaSucursal testTarifaSucursal;

    @BeforeEach
    void setUp() {
        // Clean up database
        convenioComercioSucursalRepository.deleteAll();
        tarifaSucursalRepository.deleteAll();
        sucursalRepository.deleteAll();
        comercioAfiliadoRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        Rol empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario empresa");
        empresaRol = rolRepository.save(empresaRol);

        // Create test persona
        Persona testPersona = new Persona();
        testPersona.setNombre("Admin");
        testPersona.setApellido("Test");
        testPersona.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("admin@test.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Test Address");
        testPersona.setCiudad("Test City");
        testPersona.setPais("Guatemala");
        testPersona.setCodigoPostal("01001");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        // Create test usuario
        testUsuario = new Usuario();
        testUsuario.setPersona(testPersona);
        testUsuario.setRol(empresaRol);
        testUsuario.setNombreUsuario("testadmin");
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
        testEmpresa.setDireccionFiscal("Test Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("test@company.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuario);
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

        // Create test tarifa sucursal
        testTarifaSucursal = new TarifaSucursal();
        testTarifaSucursal.setSucursal(testSucursal);
        testTarifaSucursal.setPrecioPorHora(new BigDecimal("15.00"));
        testTarifaSucursal.setMoneda("GTQ");
        testTarifaSucursal.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        testTarifaSucursal.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        testTarifaSucursal.setEstado(TarifaSucursal.EstadoTarifaSucursal.VIGENTE);
        testTarifaSucursal = tarifaSucursalRepository.save(testTarifaSucursal);

        // Create test comercio afiliado
        testComercioAfiliado = new ComercioAfiliado();
        testComercioAfiliado.setNombreComercial("Comercio Test");
        testComercioAfiliado.setRazonSocial("Comercio Test S.A.");
        testComercioAfiliado.setNit("9876543-2");
        testComercioAfiliado.setTipoComercio("Restaurante");
        testComercioAfiliado.setTelefono("87654321");
        testComercioAfiliado.setCorreoContacto("comercio@test.com");
        testComercioAfiliado.setEstado(ComercioAfiliado.Estado.ACTIVO);
        testComercioAfiliado = comercioAfiliadoRepository.save(testComercioAfiliado);
    }

    @Test
    void testGetComercioAfiliado_Integration() {
        // Act
        List<ComercioAfiliadoDTO> result = gestionComercioAfiliadoService.getComercioAfiliado();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ComercioAfiliadoDTO dto = result.get(0);
        assertEquals(testComercioAfiliado.getId(), dto.getIdComercio());
        assertEquals("Comercio Test", dto.getNombreComercial());
        assertEquals("Comercio Test S.A.", dto.getRazonSocial());
        assertEquals("9876543-2", dto.getNit());
        assertEquals("Restaurante", dto.getTipoComercio());
        assertEquals("87654321", dto.getTelefono());
        assertEquals("comercio@test.com", dto.getCorreoContacto());
        assertEquals("ACTIVO", dto.getEstado());
        assertNotNull(dto.getFechaRegistro());
    }

    @Test
    void testGetDetallesSucursalesConvenio_EmptyConvenios_Integration() {
        // Act
        List<DetalleSucursalesConvenioDTO> result = gestionComercioAfiliadoService
                .getDetallesSucursalesConvenio(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSucursalesConvenioDTO sucursalDTO = result.get(0);
        assertEquals(testSucursal.getIdSucursal(), sucursalDTO.getIdSucursal());
        assertEquals("Sucursal Test", sucursalDTO.getNombre());
        assertEquals("Test City", sucursalDTO.getCiudad());
        assertEquals("Test Department", sucursalDTO.getDepartamento());
        assertEquals(50, sucursalDTO.getCapacidad2Ruedas());
        assertEquals(100, sucursalDTO.getCapacidad4Ruedas());
        assertTrue(sucursalDTO.getConvenios().isEmpty());
        assertNotNull(sucursalDTO.getUsuario());
        assertEquals("testadmin", sucursalDTO.getUsuario().getNombreUsuario());
    }

    @Test
    void testCrearComercioAfiliado_Integration() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setNombreComercial("Nuevo Comercio");
        dto.setRazonSocial("Nuevo Comercio S.A.");
        dto.setNit("5555555-5");
        dto.setTipoComercio("Cafetería");
        dto.setTelefono("55555555");
        dto.setCorreoContacto("nuevo@comercio.com");

        // Act
        String result = gestionComercioAfiliadoService.crearComercioAfiliado(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Comercio afiliado creado exitosamente con ID:"));

        // Verify in database
        List<ComercioAfiliado> comercios = comercioAfiliadoRepository
                .findComercioAfiliadoByEstado(ComercioAfiliado.Estado.ACTIVO);
        assertEquals(2, comercios.size()); // Original + nuevo

        ComercioAfiliado nuevoComercio = comercios.stream()
                .filter(c -> "5555555-5".equals(c.getNit()))
                .findFirst()
                .orElse(null);
        assertNotNull(nuevoComercio);
        assertEquals("Nuevo Comercio", nuevoComercio.getNombreComercial());
        assertEquals("Cafetería", nuevoComercio.getTipoComercio());
    }

    @Test
    void testCrearComercioAfiliado_NitAlreadyExists_Integration() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setNit("9876543-2"); // Same as existing

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearComercioAfiliado(dto);
        });

        assertEquals("Ya existe un comercio afiliado con el NIT: 9876543-2", exception.getMessage());
    }

    @Test
    void testActualizarComercioAfiliado_Integration() {
        // Arrange
        ComercioAfiliadoDTO dto = new ComercioAfiliadoDTO();
        dto.setIdComercio(testComercioAfiliado.getId());
        dto.setNombreComercial("Comercio Actualizado");
        dto.setRazonSocial("Comercio Actualizado S.A.");
        dto.setNit("9876543-3");
        dto.setTipoComercio("Supermercado");
        dto.setTelefono("11111111");
        dto.setCorreoContacto("actualizado@comercio.com");

        // Act
        String result = gestionComercioAfiliadoService.actualizarComercioAfiliado(dto);

        // Assert
        assertEquals("Comercio afiliado actualizado exitosamente con ID: " + testComercioAfiliado.getId(), result);

        // Verify in database
        ComercioAfiliado updated = comercioAfiliadoRepository.findById(testComercioAfiliado.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Comercio Actualizado", updated.getNombreComercial());
        assertEquals("Supermercado", updated.getTipoComercio());
        assertEquals("9876543-3", updated.getNit());
    }

    @Test
    void testEliminarComercioAfiliado_Integration() {
        // Act
        String result = gestionComercioAfiliadoService.eliminarComercioAfiliado(testComercioAfiliado.getId());

        // Assert
        assertEquals("Comercio afiliado desactivado exitosamente con ID: " + testComercioAfiliado.getId(), result);

        // Verify in database
        ComercioAfiliado deleted = comercioAfiliadoRepository.findById(testComercioAfiliado.getId()).orElse(null);
        assertNotNull(deleted);
        assertEquals(ComercioAfiliado.Estado.INACTIVO, deleted.getEstado());

        // Verify it doesn't appear in active list
        List<ComercioAfiliadoDTO> activeComercios = gestionComercioAfiliadoService.getComercioAfiliado();
        assertTrue(activeComercios.isEmpty());
    }

    @Test
    void testCrearConvenioComercioSucursal_Integration() {
        // Arrange
        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(testComercioAfiliado.getId());
        dto.setIdSucursal(testSucursal.getIdSucursal());
        dto.setHorasGratisMaximo("5.0");
        dto.setPeriodoCorte("MENSUAL");
        dto.setFechaInicioConvenio("2024-01-01");
        dto.setFechaFinConvenio("2024-12-31");
        dto.setCreadoPor(testUsuario.getIdUsuario());

        // Act
        String result = gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Convenio creado exitosamente con ID:"));

        // Verify in database
        List<ConvenioComercioSucursal> convenios = convenioComercioSucursalRepository
                .findBySucursal_IdSucursalAndEstado(testSucursal.getIdSucursal(), ConvenioComercioSucursal.Estado.ACTIVO);
        assertEquals(1, convenios.size());

        ConvenioComercioSucursal convenio = convenios.get(0);
        assertEquals(testComercioAfiliado.getId(), convenio.getComercioAfiliado().getId());
        assertEquals(testSucursal.getIdSucursal(), convenio.getSucursal().getIdSucursal());
        assertEquals(0, new BigDecimal("5.0").compareTo(convenio.getHorasGratisMaximo()));
        assertEquals(ConvenioComercioSucursal.PeriodoCorte.MENSUAL, convenio.getPeriodoCorte());
        assertEquals(0, testTarifaSucursal.getPrecioPorHora().compareTo(convenio.getTarifaPorHora()));
    }

    @Test
    void testCrearConvenioComercioSucursal_ConvenioAlreadyExists_Integration() {
        // Arrange - Create first convenio
        ConvenioComercioSucursal existingConvenio = new ConvenioComercioSucursal();
        existingConvenio.setComercioAfiliado(testComercioAfiliado);
        existingConvenio.setSucursal(testSucursal);
        existingConvenio.setHorasGratisMaximo(new BigDecimal("3.0"));
        existingConvenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        existingConvenio.setTarifaPorHora(testTarifaSucursal.getPrecioPorHora());
        existingConvenio.setFechaInicioConvenio(LocalDateTime.now());
        existingConvenio.setFechaFinConvenio(LocalDateTime.now().plusYears(1));
        existingConvenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        existingConvenio.setCreadoPor(testUsuario);
        existingConvenio.setFechaCreacion(LocalDateTime.now());
        convenioComercioSucursalRepository.save(existingConvenio);

        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdComercio(testComercioAfiliado.getId());
        dto.setIdSucursal(testSucursal.getIdSucursal());
        dto.setCreadoPor(testUsuario.getIdUsuario());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionComercioAfiliadoService.crearConvenioComercioSucursal(dto);
        });

        assertEquals("Ya existe un convenio activo entre el comercio afiliado y la sucursal.", exception.getMessage());
    }

    @Test
    void testActualizarConvenioComercioSucursal_Integration() {
        // Arrange - Create convenio first
        ConvenioComercioSucursal convenio = createTestConvenio();

        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(convenio.getId());
        dto.setHorasGratisMaximo("8.0");
        dto.setPeriodoCorte("ANUAL");
        dto.setFechaInicioConvenio("2024-02-01");
        dto.setFechaFinConvenio("2025-02-01");

        // Act
        String result = gestionComercioAfiliadoService.actualizarConvenioComercioSucursal(dto);

        // Assert
        assertEquals("Convenio actualizado exitosamente con ID: " + convenio.getId(), result);

        // Verify in database
        ConvenioComercioSucursal updated = convenioComercioSucursalRepository.findById(convenio.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(0, new BigDecimal("8.0").compareTo(updated.getHorasGratisMaximo()));
        assertEquals(ConvenioComercioSucursal.PeriodoCorte.ANUAL, updated.getPeriodoCorte());
    }

    @Test
    void testCambiarEstadoConvenio_Integration() {
        // Arrange - Create convenio first
        ConvenioComercioSucursal convenio = createTestConvenio();

        ConvenioComercioSucursalDTO dto = new ConvenioComercioSucursalDTO();
        dto.setIdConvenio(convenio.getId());
        dto.setEstado("INACTIVO");

        // Act
        String result = gestionComercioAfiliadoService.cambiarEstadoConvenio(dto);

        // Assert
        assertEquals("Estado del convenio actualizado exitosamente a: INACTIVO", result);

        // Verify in database
        ConvenioComercioSucursal updated = convenioComercioSucursalRepository.findById(convenio.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(ConvenioComercioSucursal.Estado.INACTIVO, updated.getEstado());
    }

    @Test
    void testGetDetallesSucursalesConvenio_WithConvenios_Integration() {
        // Arrange - Create convenio first
        createTestConvenio();

        // Act
        List<DetalleSucursalesConvenioDTO> result = gestionComercioAfiliadoService
                .getDetallesSucursalesConvenio(testUsuario.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSucursalesConvenioDTO sucursalDTO = result.get(0);
        assertEquals(1, sucursalDTO.getConvenios().size());

        DetalleSucursalesConvenioDTO.ConvenioSucursalDTO convenioDTO = sucursalDTO.getConvenios().get(0);
        assertNotNull(convenioDTO.getIdConvenio());
        assertEquals("5.0", convenioDTO.getHorasGratisMaximo());
        assertEquals("MENSUAL", convenioDTO.getPeriodoCorte());
        assertEquals("ACTIVO", convenioDTO.getEstado());
        assertNotNull(convenioDTO.getComercioAfiliado());
        assertEquals("Comercio Test", convenioDTO.getComercioAfiliado().getNombreComercial());
    }

    @Test
    void testCompleteWorkflow_Integration() {
        // Complete workflow: Create comercio -> Create convenio -> Update convenio -> Change state

        // Step 1: Create new comercio
        ComercioAfiliadoDTO comercioDTO = new ComercioAfiliadoDTO();
        comercioDTO.setNombreComercial("Workflow Comercio");
        comercioDTO.setRazonSocial("Workflow Comercio S.A.");
        comercioDTO.setNit("7777777-7");
        comercioDTO.setTipoComercio("Farmacia");
        comercioDTO.setTelefono("77777777");
        comercioDTO.setCorreoContacto("workflow@comercio.com");

        String createResult = gestionComercioAfiliadoService.crearComercioAfiliado(comercioDTO);
        assertTrue(createResult.contains("creado exitosamente"));

        // Get created comercio
        List<ComercioAfiliadoDTO> comercios = gestionComercioAfiliadoService.getComercioAfiliado();
        ComercioAfiliadoDTO newComercio = comercios.stream()
                .filter(c -> "7777777-7".equals(c.getNit()))
                .findFirst()
                .orElse(null);
        assertNotNull(newComercio);

        // Step 2: Create convenio
        ConvenioComercioSucursalDTO convenioDTO = new ConvenioComercioSucursalDTO();
        convenioDTO.setIdComercio(newComercio.getIdComercio());
        convenioDTO.setIdSucursal(testSucursal.getIdSucursal());
        convenioDTO.setHorasGratisMaximo("3.0");
        convenioDTO.setPeriodoCorte("MENSUAL");
        convenioDTO.setFechaInicioConvenio("2024-01-01");
        convenioDTO.setFechaFinConvenio("2024-12-31");
        convenioDTO.setCreadoPor(testUsuario.getIdUsuario());

        String convenioResult = gestionComercioAfiliadoService.crearConvenioComercioSucursal(convenioDTO);
        assertTrue(convenioResult.contains("Convenio creado exitosamente"));

        // Step 3: Verify convenio in detalles
        List<DetalleSucursalesConvenioDTO> detalles = gestionComercioAfiliadoService
                .getDetallesSucursalesConvenio(testUsuario.getIdUsuario());
        assertEquals(1, detalles.size());
        assertEquals(1, detalles.get(0).getConvenios().size());

        // Step 4: Update comercio
        comercioDTO.setIdComercio(newComercio.getIdComercio());
        comercioDTO.setNombreComercial("Workflow Comercio Updated");
        String updateResult = gestionComercioAfiliadoService.actualizarComercioAfiliado(comercioDTO);
        assertTrue(updateResult.contains("actualizado exitosamente"));

        // Step 5: Verify updated data
        List<ComercioAfiliadoDTO> updatedComercios = gestionComercioAfiliadoService.getComercioAfiliado();
        ComercioAfiliadoDTO updatedComercio = updatedComercios.stream()
                .filter(c -> newComercio.getIdComercio().equals(c.getIdComercio()))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedComercio);
        assertEquals("Workflow Comercio Updated", updatedComercio.getNombreComercial());
    }

    private ConvenioComercioSucursal createTestConvenio() {
        ConvenioComercioSucursal convenio = new ConvenioComercioSucursal();
        convenio.setComercioAfiliado(testComercioAfiliado);
        convenio.setSucursal(testSucursal);
        convenio.setHorasGratisMaximo(new BigDecimal("5.0"));
        convenio.setPeriodoCorte(ConvenioComercioSucursal.PeriodoCorte.MENSUAL);
        convenio.setTarifaPorHora(testTarifaSucursal.getPrecioPorHora());
        convenio.setFechaInicioConvenio(LocalDateTime.now());
        convenio.setFechaFinConvenio(LocalDateTime.now().plusYears(1));
        convenio.setEstado(ConvenioComercioSucursal.Estado.ACTIVO);
        convenio.setCreadoPor(testUsuario);
        convenio.setFechaCreacion(LocalDateTime.now());
        return convenioComercioSucursalRepository.save(convenio);
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
