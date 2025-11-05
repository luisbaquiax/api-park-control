package org.parkcontrol.apiparkcontrol.services.vehicleservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculosPropietarioDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class VehiculoServiceIntegrationTest {

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Persona testPersona;
    private Rol clienteRol;
    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        vehiculoRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        rolRepository.deleteAll();

        // Crear rol CLIENTE
        clienteRol = new Rol();
        clienteRol.setNombreRol("CLIENTE");
        clienteRol.setDescripcion("Cliente del sistema");
        clienteRol = rolRepository.save(clienteRol);

        // Crear persona de prueba
        testPersona = new Persona();
        testPersona.setNombre("Juan");
        testPersona.setApellido("Pérez");
        testPersona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testPersona.setDpi(generateUniqueDpi());
        testPersona.setCorreo("juan@test.com");
        testPersona.setTelefono("12345678");
        testPersona.setDireccionCompleta("Calle Principal");
        testPersona.setCiudad("Guatemala");
        testPersona.setPais("Guatemala");
        testPersona.setCodigoPostal("01001");
        testPersona.setEstado(Persona.Estado.ACTIVO);
        testPersona = personaRepository.save(testPersona);

        // Crear usuario
        testUsuario = new Usuario();
        testUsuario.setPersona(testPersona);
        testUsuario.setRol(clienteRol);
        testUsuario.setNombreUsuario("juanperez" + System.nanoTime());
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);
    }

    @Test
    void create_Success_Integration() {
        // Arrange
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Toyota");
        request.setModelo("Corolla");

        // Act
        MessageSuccess result = vehiculoService.create(testPersona.getDpi(), request);

        // Assert
        assertNotNull(result);
        assertEquals(201, result.getCode());
        assertTrue(result.getMessage().contains("P123ABC"));
        assertTrue(result.getMessage().contains("Juan"));

        // Verificar que el vehículo se guardó en la base de datos
        Vehiculo savedVehiculo = vehiculoRepository.findByPlaca("P123ABC");
        assertNotNull(savedVehiculo);
        assertEquals("P123ABC", savedVehiculo.getPlaca());
        assertEquals("Toyota", savedVehiculo.getMarca());
        assertEquals("Corolla", savedVehiculo.getModelo());
        assertEquals(Vehiculo.TipoVehiculo.CUATRO_RUEDAS, savedVehiculo.getTipoVehiculo());
        assertEquals(testPersona.getIdPersona(), savedVehiculo.getPropietario().getIdPersona());
    }

    @Test
    void create_VehiculoDuplicado_Integration() {
        // Arrange
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Toyota");
        request.setModelo("Corolla");

        // Crear primer vehículo
        vehiculoService.create(testPersona.getDpi(), request);

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create(testPersona.getDpi(), request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya existe"));
    }

    @Test
    void create_PersonaNoExiste_Integration() {
        // Arrange
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Toyota");
        request.setModelo("Corolla");

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create("9999999999999", request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("no se encuentra registrado"));
    }

    @Test
    void update_Success_Integration() {
        // Arrange
        VehiculoRequestDTO createRequest = new VehiculoRequestDTO();
        createRequest.setPlaca("P123ABC");
        createRequest.setAnio(2020);
        createRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        createRequest.setColor("Negro");
        createRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        createRequest.setMarca("Toyota");
        createRequest.setModelo("Corolla");

        vehiculoService.create(testPersona.getDpi(), createRequest);
        Vehiculo vehiculo = vehiculoRepository.findByPlaca("P123ABC");

        VehiculoRequestDTO updateRequest = new VehiculoRequestDTO();
        updateRequest.setAnio(2021);
        updateRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        updateRequest.setColor("Blanco");
        updateRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        updateRequest.setMarca("Honda");
        updateRequest.setModelo("Civic");

        // Act
        MessageSuccess result = vehiculoService.update(vehiculo.getId(), updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(201, result.getCode());

        // Verificar cambios en la base de datos
        Vehiculo updatedVehiculo = vehiculoRepository.findById(vehiculo.getId()).orElse(null);
        assertNotNull(updatedVehiculo);
        assertEquals(2021, updatedVehiculo.getAnio());
        assertEquals("Blanco", updatedVehiculo.getColor());
        assertEquals("Honda", updatedVehiculo.getMarca());
        assertEquals("Civic", updatedVehiculo.getModelo());
        assertEquals("P123ABC", updatedVehiculo.getPlaca()); // La placa no debe cambiar
    }

    @Test
    void update_VehiculoNoExiste_Integration() {
        // Arrange
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setAnio(2021);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Blanco");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Honda");
        request.setModelo("Civic");

        // Act & Assert
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.update(9999L, request);
        });

        assertEquals(401, exception.getStatus());
        assertEquals("El vehiculo no existe", exception.getMessage());
    }

    @Test
    void changeStatus_Success_Integration() {
        // Arrange
        VehiculoRequestDTO createRequest = new VehiculoRequestDTO();
        createRequest.setPlaca("P123ABC");
        createRequest.setAnio(2020);
        createRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        createRequest.setColor("Negro");
        createRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        createRequest.setMarca("Toyota");
        createRequest.setModelo("Corolla");

        vehiculoService.create(testPersona.getDpi(), createRequest);
        Vehiculo vehiculo = vehiculoRepository.findByPlaca("P123ABC");

        // Act
        MessageSuccess result = vehiculoService.changeStatus("INACTIVO", vehiculo.getId());

        // Assert
        assertNotNull(result);
        assertEquals(201, result.getCode());

        // Verificar cambio en la base de datos
        Vehiculo updatedVehiculo = vehiculoRepository.findById(vehiculo.getId()).orElse(null);
        assertNotNull(updatedVehiculo);
        assertEquals(Vehiculo.EstadoVehiculo.INACTIVO, updatedVehiculo.getEstado());
    }

    @Test
    void changeStatus_AllStates_Integration() {
        // Arrange
        VehiculoRequestDTO createRequest = new VehiculoRequestDTO();
        createRequest.setPlaca("P123ABC");
        createRequest.setAnio(2020);
        createRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        createRequest.setColor("Negro");
        createRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        createRequest.setMarca("Toyota");
        createRequest.setModelo("Corolla");

        vehiculoService.create(testPersona.getDpi(), createRequest);
        Vehiculo vehiculo = vehiculoRepository.findByPlaca("P123ABC");

        // Test INACTIVO
        vehiculoService.changeStatus("INACTIVO", vehiculo.getId());
        Vehiculo v1 = vehiculoRepository.findById(vehiculo.getId()).orElse(null);
        assertEquals(Vehiculo.EstadoVehiculo.INACTIVO, v1.getEstado());


        // Test ACTIVO again
        vehiculoService.changeStatus("ACTIVO", vehiculo.getId());
        Vehiculo v3 = vehiculoRepository.findById(vehiculo.getId()).orElse(null);
        assertEquals(Vehiculo.EstadoVehiculo.ACTIVO, v3.getEstado());
    }

    @Test
    void getAllByPersona_Success_Integration() {
        // Arrange - Crear múltiples vehículos para el usuario
        VehiculoRequestDTO request1 = createVehiculoRequest("P123ABC", "Toyota", "Corolla");
        VehiculoRequestDTO request2 = createVehiculoRequest("P456DEF", "Honda", "Civic");

        vehiculoService.create(testPersona.getDpi(), request1);
        vehiculoService.create(testPersona.getDpi(), request2);

        // Act
        List<VehiculosPropietarioDTO> result = vehiculoService.getAllByPersona();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Buscar el propietario de prueba en los resultados
        VehiculosPropietarioDTO propietarioDTO = result.stream()
                .filter(p -> p.getVehiculos().stream()
                        .anyMatch(v -> "P123ABC".equals(v.getPlaca())))
                .findFirst()
                .orElse(null);

        assertNotNull(propietarioDTO);
        assertTrue(propietarioDTO.getVehiculos().size() >= 2);
    }

    @Test
    void getAll_Success_Integration() {
        // Arrange
        VehiculoRequestDTO request1 = createVehiculoRequest("P123ABC", "Toyota", "Corolla");
        VehiculoRequestDTO request2 = createVehiculoRequest("P456DEF", "Honda", "Civic");
        VehiculoRequestDTO request3 = createVehiculoRequest("M789GHI", "Yamaha", "FZ");
        request3.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);

        vehiculoService.create(testPersona.getDpi(), request1);
        vehiculoService.create(testPersona.getDpi(), request2);
        vehiculoService.create(testPersona.getDpi(), request3);

        // Act
        List<VehicleResponsDTO> result = vehiculoService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 3);
        
        // Verificar que todos los vehículos creados están en el resultado
        assertTrue(result.stream().anyMatch(v -> "P123ABC".equals(v.getPlaca())));
        assertTrue(result.stream().anyMatch(v -> "P456DEF".equals(v.getPlaca())));
        assertTrue(result.stream().anyMatch(v -> "M789GHI".equals(v.getPlaca())));
    }

    @Test
    void create_MultipleDosRuedas_Integration() {
        // Arrange
        VehiculoRequestDTO moto1 = createVehiculoRequest("M111", "Yamaha", "FZ-16");
        moto1.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        
        VehiculoRequestDTO moto2 = createVehiculoRequest("M222", "Honda", "CB190");
        moto2.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);

        // Act
        MessageSuccess result1 = vehiculoService.create(testPersona.getDpi(), moto1);
        MessageSuccess result2 = vehiculoService.create(testPersona.getDpi(), moto2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        
        List<VehicleResponsDTO> vehiculos = vehiculoService.getAll();
        long dosRuedas = vehiculos.stream()
                .filter(v -> v.getTipoVehiculo() == Vehiculo.TipoVehiculo.DOS_RUEDAS)
                .count();
        
        assertTrue(dosRuedas >= 2);
    }

    @Test
    void create_MultipleCuatroRuedas_Integration() {
        // Arrange
        VehiculoRequestDTO auto1 = createVehiculoRequest("P111", "Toyota", "Corolla");
        VehiculoRequestDTO auto2 = createVehiculoRequest("P222", "Honda", "Civic");
        VehiculoRequestDTO auto3 = createVehiculoRequest("P333", "Mazda", "3");

        // Act
        vehiculoService.create(testPersona.getDpi(), auto1);
        vehiculoService.create(testPersona.getDpi(), auto2);
        vehiculoService.create(testPersona.getDpi(), auto3);

        // Assert
        List<VehicleResponsDTO> vehiculos = vehiculoService.getAll();
        long cuatroRuedas = vehiculos.stream()
                .filter(v -> v.getTipoVehiculo() == Vehiculo.TipoVehiculo.CUATRO_RUEDAS)
                .count();
        
        assertTrue(cuatroRuedas >= 3);
    }

    @Test
    void fullLifecycle_Integration() {
        // Arrange
        VehiculoRequestDTO createRequest = createVehiculoRequest("P999ZZZ", "Test", "Model");

        // Create
        MessageSuccess createResult = vehiculoService.create(testPersona.getDpi(), createRequest);
        assertNotNull(createResult);
        assertEquals(201, createResult.getCode());

        Vehiculo vehiculo = vehiculoRepository.findByPlaca("P999ZZZ");
        assertNotNull(vehiculo);
        Long vehiculoId = vehiculo.getId();

        // Update
        VehiculoRequestDTO updateRequest = new VehiculoRequestDTO();
        updateRequest.setAnio(2023);
        updateRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        updateRequest.setColor("Actualizado");
        updateRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        updateRequest.setMarca("MarcaActualizada");
        updateRequest.setModelo("ModeloActualizado");

        MessageSuccess updateResult = vehiculoService.update(vehiculoId, updateRequest);
        assertNotNull(updateResult);
        assertEquals(201, updateResult.getCode());

        // Change status
        MessageSuccess statusResult = vehiculoService.changeStatus("INACTIVO", vehiculoId);
        assertNotNull(statusResult);
        assertEquals(201, statusResult.getCode());

        // Verify final state
        Vehiculo finalVehiculo = vehiculoRepository.findById(vehiculoId).orElse(null);
        assertNotNull(finalVehiculo);
        assertEquals("P999ZZZ", finalVehiculo.getPlaca());
        assertEquals("MarcaActualizada", finalVehiculo.getMarca());
        assertEquals("ModeloActualizado", finalVehiculo.getModelo());
        assertEquals(Vehiculo.EstadoVehiculo.INACTIVO, finalVehiculo.getEstado());
        assertEquals(2023, finalVehiculo.getAnio());
    }

    // Helper methods

    private VehiculoRequestDTO createVehiculoRequest(String placa, String marca, String modelo) {
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setPlaca(placa);
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca(marca);
        request.setModelo(modelo);
        return request;
    }

    private String generateUniqueDpi() {
        return String.valueOf(System.nanoTime()).substring(0, 13);
    }
}
