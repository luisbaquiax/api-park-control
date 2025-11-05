package org.parkcontrol.apiparkcontrol.services.vehicleservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.mapper.UsuarioPersonaRolMap;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.repositories.VehiculoRepository;
import org.parkcontrol.apiparkcontrol.services.PersonaService;
import org.parkcontrol.apiparkcontrol.services.UsuarioService;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.parkcontrol.apiparkcontrol.mapper.VehicleMap;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository repository;

    @Mock
    private PersonaService personaService;

    @Mock
    private VehicleMap vehicleMap;

    @Mock
    private UsuarioPersonaRolMap usuarioPersonaRolMap;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VehiculoService vehiculoService;

    private VehiculoRequestDTO request;
    private Persona propietario;

    @BeforeEach
    void setUp() {
        request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Toyota");
        request.setModelo("Corolla");

        propietario = new Persona();
        propietario.setIdPersona(1L);
        propietario.setNombre("Luis");
        propietario.setApellido("Baquiax");
        propietario.setDpi("1234567890101");
    }

    @Test
    void createVehiculo_Correcto() {
        // GIVEN
        when(repository.findByPlaca(request.getPlaca())).thenReturn(null);
        when(personaService.findByDpi("1234567890101")).thenReturn(propietario);

        // WHEN
        MessageSuccess resultado = vehiculoService.create("1234567890101", request);

        // THEN
        assertEquals(201, resultado.getCode());
        assertTrue(resultado.getMessage().contains("P123ABC"));
        assertTrue(resultado.getMessage().contains("Luis"));
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void createVehiculo_YaExiste() {
        // GIVEN
        when(repository.findByPlaca(request.getPlaca())).thenReturn(new Vehiculo());

        // WHEN / THEN
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create("1234567890101", request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya existe"));
        verify(repository, never()).save(any(Vehiculo.class));
    }

    @Test
    void createVehiculo_PersonaNoExiste() {
        // GIVEN
        when(repository.findByPlaca(request.getPlaca())).thenReturn(null);
        when(personaService.findByDpi("1234567890101")).thenReturn(null);

        // WHEN / THEN
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create("1234567890101", request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("no se encuentra registrado"));
        verify(repository, never()).save(any(Vehiculo.class));
    }
}
