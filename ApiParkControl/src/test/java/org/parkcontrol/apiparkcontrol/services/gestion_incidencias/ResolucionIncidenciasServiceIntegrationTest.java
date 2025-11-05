package org.parkcontrol.apiparkcontrol.services.gestion_incidencias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

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
class ResolucionIncidenciasServiceIntegrationTest {

    @Autowired
    private ResolucionIncidenciasService resolucionIncidenciasService;

    @Autowired
    private IncidenciaTicketRepository incidenciaTicketRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;

    @MockBean
    private IncidenciaTicketSucursalService incidenciaTicketSucursalService;

    private Usuario testUsuarioEmpresa;
    private Usuario testUsuarioResuelve;
    private Empresa testEmpresa;
    private Sucursal testSucursal;
    private IncidenciaTicket testIncidencia;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clean up database
        incidenciaTicketRepository.deleteAll();
        ticketRepository.deleteAll();
        vehiculoRepository.deleteAll();
        sucursalRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test roles
        Rol empresaRol = new Rol();
        empresaRol.setNombreRol("EMPRESA");
        empresaRol.setDescripcion("Usuario empresa");
        empresaRol = rolRepository.save(empresaRol);

        Rol adminRol = new Rol();
        adminRol.setNombreRol("ADMIN");
        adminRol.setDescripcion("Usuario administrador");
        adminRol = rolRepository.save(adminRol);

        // Create test persona for empresa
        Persona personaEmpresa = new Persona();
        personaEmpresa.setNombre("Admin");
        personaEmpresa.setApellido("Empresa");
        personaEmpresa.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        personaEmpresa.setDpi(generateUniqueDpi());
        personaEmpresa.setCorreo("admin@empresa.com");
        personaEmpresa.setTelefono("87654321");
        personaEmpresa.setDireccionCompleta("Test Address");
        personaEmpresa.setCiudad("Test City");
        personaEmpresa.setPais("Guatemala");
        personaEmpresa.setCodigoPostal("01001");
        personaEmpresa.setEstado(Persona.Estado.ACTIVO);
        personaEmpresa = personaRepository.save(personaEmpresa);

        // Create test usuario empresa
        testUsuarioEmpresa = new Usuario();
        testUsuarioEmpresa.setPersona(personaEmpresa);
        testUsuarioEmpresa.setRol(empresaRol);
        testUsuarioEmpresa.setNombreUsuario("adminempresa");
        testUsuarioEmpresa.setContraseniaHash("hashedPassword");
        testUsuarioEmpresa.setDobleFactorHabilitado(false);
        testUsuarioEmpresa.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioEmpresa.setDebeCambiarContrasenia(false);
        testUsuarioEmpresa.setEsPrimeraVez(false);
        testUsuarioEmpresa.setIntentosFallidos(0);
        testUsuarioEmpresa = usuarioRepository.save(testUsuarioEmpresa);

        // Create test usuario resolver
        Persona personaResolver = new Persona();
        personaResolver.setNombre("Admin");
        personaResolver.setApellido("Resolver");
        personaResolver.setFechaNacimiento(LocalDate.of(1985, 1, 1));
        personaResolver.setDpi(generateUniqueDpi());
        personaResolver.setCorreo("resolver@empresa.com");
        personaResolver.setTelefono("11111111");
        personaResolver.setDireccionCompleta("Resolver Address");
        personaResolver.setCiudad("Test City");
        personaResolver.setPais("Guatemala");
        personaResolver.setCodigoPostal("01002");
        personaResolver.setEstado(Persona.Estado.ACTIVO);
        personaResolver = personaRepository.save(personaResolver);

        testUsuarioResuelve = new Usuario();
        testUsuarioResuelve.setPersona(personaResolver);
        testUsuarioResuelve.setRol(adminRol);
        testUsuarioResuelve.setNombreUsuario("resolver");
        testUsuarioResuelve.setContraseniaHash("hashedPassword");
        testUsuarioResuelve.setDobleFactorHabilitado(false);
        testUsuarioResuelve.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuarioResuelve.setDebeCambiarContrasenia(false);
        testUsuarioResuelve.setEsPrimeraVez(false);
        testUsuarioResuelve.setIntentosFallidos(0);
        testUsuarioResuelve = usuarioRepository.save(testUsuarioResuelve);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setUsuarioEmpresa(testUsuarioEmpresa);
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
        testSucursal.setUsuarioSucursal(testUsuarioEmpresa);
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

        // Create test vehiculo and ticket
        Persona propietario = new Persona();
        propietario.setNombre("Cliente");
        propietario.setApellido("Test");
        propietario.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        propietario.setDpi(generateUniqueDpi());
        propietario.setCorreo("cliente@test.com");
        propietario.setTelefono("22222222");
        propietario.setDireccionCompleta("Cliente Address");
        propietario.setCiudad("Test City");
        propietario.setPais("Guatemala");
        propietario.setCodigoPostal("01003");
        propietario.setEstado(Persona.Estado.ACTIVO);
        propietario = personaRepository.save(propietario);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPropietario(propietario);
        vehiculo.setPlaca("TEST123");
        vehiculo.setMarca("Test");
        vehiculo.setModelo("Model");
        vehiculo.setColor("Azul");
        vehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        vehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        vehiculo = vehiculoRepository.save(vehiculo);

        testTicket = new Ticket();
        testTicket.setSucursal(testSucursal);
        testTicket.setVehiculo(vehiculo);
        testTicket.setFolioNumerico("TEST001");
        testTicket.setTipoCliente(Ticket.TipoCliente.SIN_SUSCRIPCION); // Corregido
        testTicket.setEstado(Ticket.EstadoTicket.ACTIVO);
        testTicket.setFechaHoraEntrada(LocalDateTime.now()); // Corregido nombre del campo
        testTicket = ticketRepository.save(testTicket);

        // Create test incidencia
        testIncidencia = new IncidenciaTicket();
        testIncidencia.setTicket(testTicket);
        testIncidencia.setTipoIncidencia(IncidenciaTicket.TipoIncidencia.COMPROBANTE_PERDIDO);
        testIncidencia.setDescripcion("Test incidencia");
        testIncidencia.setFechaIncidencia(LocalDateTime.now());
        testIncidencia.setResuelto(false);
        testIncidencia = incidenciaTicketRepository.save(testIncidencia);
    }

    @Test
    void testObtenerDetalleIncidenciasPorEmpresa_Integration() throws Exception {
        // Arrange
        IncidenciasSucursalDTO mockIncidenciaDTO = new IncidenciasSucursalDTO();
        mockIncidenciaDTO.setIdTicket(testTicket.getId());
        mockIncidenciaDTO.setFolioNumerico("TEST001");

        IncidenciasSucursalDTO.IncidenciasTicketDTO incidenciaTicketDTO = new IncidenciasSucursalDTO.IncidenciasTicketDTO();
        incidenciaTicketDTO.setIdIncidencia(testIncidencia.getIdIncidencia());
        incidenciaTicketDTO.setTipoIncidencia("COMPROBANTE_PERDIDO");
        mockIncidenciaDTO.setIncidencias(incidenciaTicketDTO);

        when(incidenciaTicketSucursalService.obtenerIncidenciasSucursal(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(List.of(mockIncidenciaDTO));

        // Act
        List<DetalleSucursalesIncidenciasDTO> result = resolucionIncidenciasService
                .obtenerDetalleIncidenciasPorEmpresa(testUsuarioEmpresa.getIdUsuario());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DetalleSucursalesIncidenciasDTO detalleDTO = result.get(0);
        assertEquals(testSucursal.getIdSucursal(), detalleDTO.getIdSucursal());
        assertEquals("Sucursal Test", detalleDTO.getNombreSucursal());
        assertEquals("Test Address", detalleDTO.getDireccionSucursal());
        assertEquals("12345678", detalleDTO.getTelefonoSucursal());
        assertEquals(1, detalleDTO.getIncidenciasSucursalDTOList().size());

        verify(incidenciaTicketSucursalService).obtenerIncidenciasSucursal(testUsuarioEmpresa.getIdUsuario());
    }

    @Test
    void testResolverIncidencia_Integration() throws Exception {
        // Arrange
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(testIncidencia.getIdIncidencia());
        resolucionDTO.setIdUsuarioResuelve(testUsuarioResuelve.getIdUsuario());
        resolucionDTO.setObservacionesResolucion("Incidencia resuelta correctamente");

        // Act
        String result = resolucionIncidenciasService.resolverIncidencia(resolucionDTO);

        // Assert
        assertEquals("Incidencia con ID: " + testIncidencia.getIdIncidencia() + " resuelta exitosamente.", result);

        // Verify in database
        IncidenciaTicket updatedIncidencia = incidenciaTicketRepository.findById(testIncidencia.getIdIncidencia()).orElse(null);
        assertNotNull(updatedIncidencia);
        assertTrue(updatedIncidencia.isResuelto());
        assertEquals(testUsuarioResuelve.getIdUsuario(), updatedIncidencia.getResueltoPor());
        assertEquals("Incidencia resuelta correctamente", updatedIncidencia.getObservacionesResolucion());
        assertNotNull(updatedIncidencia.getFechaResolucion());
    }

    @Test
    void testCompleteWorkflow_Integration() throws Exception {
        // Arrange - Mock incidencia service response
        IncidenciasSucursalDTO mockIncidenciaDTO = new IncidenciasSucursalDTO();
        mockIncidenciaDTO.setIdTicket(testTicket.getId());

        IncidenciasSucursalDTO.IncidenciasTicketDTO incidenciaTicketDTO = new IncidenciasSucursalDTO.IncidenciasTicketDTO();
        incidenciaTicketDTO.setIdIncidencia(testIncidencia.getIdIncidencia());
        incidenciaTicketDTO.setTipoIncidencia("COMPROBANTE_PERDIDO");
        incidenciaTicketDTO.setResuelto(false);
        mockIncidenciaDTO.setIncidencias(incidenciaTicketDTO);

        when(incidenciaTicketSucursalService.obtenerIncidenciasSucursal(testUsuarioEmpresa.getIdUsuario()))
                .thenReturn(List.of(mockIncidenciaDTO));

        // Step 1: Get incidencias by empresa
        List<DetalleSucursalesIncidenciasDTO> incidencias = resolucionIncidenciasService
                .obtenerDetalleIncidenciasPorEmpresa(testUsuarioEmpresa.getIdUsuario());

        // Assert incidencias retrieved
        assertEquals(1, incidencias.size());
        assertEquals(1, incidencias.get(0).getIncidenciasSucursalDTOList().size());
        assertFalse(incidencias.get(0).getIncidenciasSucursalDTOList().get(0).getIncidencias().isResuelto());

        // Step 2: Resolve incidencia
        ResolucionIncidenciaDTO resolucionDTO = new ResolucionIncidenciaDTO();
        resolucionDTO.setIdIncidencia(testIncidencia.getIdIncidencia());
        resolucionDTO.setIdUsuarioResuelve(testUsuarioResuelve.getIdUsuario());
        resolucionDTO.setObservacionesResolucion("Workflow test - resuelto");

        String resolveResult = resolucionIncidenciasService.resolverIncidencia(resolucionDTO);

        // Assert resolution
        assertTrue(resolveResult.contains("resuelta exitosamente"));

        // Verify final state in database
        IncidenciaTicket finalIncidencia = incidenciaTicketRepository.findById(testIncidencia.getIdIncidencia()).orElse(null);
        assertNotNull(finalIncidencia);
        assertTrue(finalIncidencia.isResuelto());
        assertEquals("Workflow test - resuelto", finalIncidencia.getObservacionesResolucion());
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}
