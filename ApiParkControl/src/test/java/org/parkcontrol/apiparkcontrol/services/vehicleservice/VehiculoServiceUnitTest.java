package org.parkcontrol.apiparkcontrol.services.vehicleservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculosPropietarioDTO;
import org.parkcontrol.apiparkcontrol.mapper.UsuarioPersonaRolMap;
import org.parkcontrol.apiparkcontrol.mapper.VehicleMap;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.VehiculoRepository;
import org.parkcontrol.apiparkcontrol.services.PersonaService;
import org.parkcontrol.apiparkcontrol.services.UsuarioService;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceUnitTest {

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
    private Vehiculo mockVehiculo;
    private Usuario mockUsuario;
    private VehicleResponsDTO mockVehicleResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup VehiculoRequestDTO
        request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setAnio(2020);
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setColor("Negro");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        request.setMarca("Toyota");
        request.setModelo("Corolla");

        // Setup Persona
        propietario = new Persona();
        propietario.setIdPersona(1L);
        propietario.setNombre("Luis");
        propietario.setApellido("Baquiax");
        propietario.setDpi("1234567890101");
        propietario.setCorreo("luis@test.com");
        propietario.setTelefono("12345678");
        propietario.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        propietario.setDireccionCompleta("Test Address");
        propietario.setCiudad("Guatemala");
        propietario.setPais("Guatemala");
        propietario.setCodigoPostal("01001");
        propietario.setEstado(Persona.Estado.ACTIVO);

        // Setup Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPlaca("P123ABC");
        mockVehiculo.setAnio(2020);
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        mockVehiculo.setColor("Negro");
        mockVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
        mockVehiculo.setMarca("Toyota");
        mockVehiculo.setModelo("Corolla");
        mockVehiculo.setPropietario(propietario);

        // Setup Usuario
        Rol rol = new Rol();
        rol.setIdRol(1L);
        rol.setNombreRol("CLIENTE");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setPersona(propietario);
        mockUsuario.setRol(rol);
        mockUsuario.setNombreUsuario("testuser");
        mockUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);

        // Setup VehicleResponseDTO
        mockVehicleResponseDTO = new VehicleResponsDTO();
        mockVehicleResponseDTO.setId(1L);
        mockVehicleResponseDTO.setPlaca("P123ABC");
        mockVehicleResponseDTO.setMarca("Toyota");
        mockVehicleResponseDTO.setModelo("Corolla");
        mockVehicleResponseDTO.setColor("Negro");
        mockVehicleResponseDTO.setAnio(2020);
        mockVehicleResponseDTO.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        mockVehicleResponseDTO.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);
    }

    // ==================== CREATE TESTS ====================
    
    @Test
    void create_Success() {
        // Given
        when(repository.findByPlaca(request.getPlaca())).thenReturn(null);
        when(personaService.findByDpi("1234567890101")).thenReturn(propietario);
        when(repository.save(any(Vehiculo.class))).thenReturn(mockVehiculo);

        // When
        MessageSuccess resultado = vehiculoService.create("1234567890101", request);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        assertTrue(resultado.getMessage().contains("P123ABC"));
        assertTrue(resultado.getMessage().contains("Luis"));
        assertTrue(resultado.getMessage().contains("Baquiax"));
        verify(repository, times(1)).findByPlaca(request.getPlaca());
        verify(personaService, times(1)).findByDpi("1234567890101");
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void create_VehiculoYaExiste() {
        // Given
        when(repository.findByPlaca(request.getPlaca())).thenReturn(mockVehiculo);

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create("1234567890101", request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya existe"));
        assertTrue(exception.getMessage().contains("P123ABC"));
        verify(repository, times(1)).findByPlaca(request.getPlaca());
        verify(personaService, never()).findByDpi(anyString());
        verify(repository, never()).save(any(Vehiculo.class));
    }

    @Test
    void create_PersonaNoExiste() {
        // Given
        when(repository.findByPlaca(request.getPlaca())).thenReturn(null);
        when(personaService.findByDpi("1234567890101")).thenReturn(null);

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create("1234567890101", request);
        });

        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("no se encuentra registrado"));
        assertTrue(exception.getMessage().contains("1234567890101"));
        verify(repository, times(1)).findByPlaca(request.getPlaca());
        verify(personaService, times(1)).findByDpi("1234567890101");
        verify(repository, never()).save(any(Vehiculo.class));
    }

    @Test
    void create_WithDosRuedas() {
        // Given
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        request.setPlaca("M456XYZ");
        
        when(repository.findByPlaca("M456XYZ")).thenReturn(null);
        when(personaService.findByDpi("1234567890101")).thenReturn(propietario);
        
        Vehiculo moto = new Vehiculo();
        moto.setPlaca("M456XYZ");
        moto.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        moto.setPropietario(propietario);
        
        when(repository.save(any(Vehiculo.class))).thenReturn(moto);

        // When
        MessageSuccess resultado = vehiculoService.create("1234567890101", request);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        assertTrue(resultado.getMessage().contains("M456XYZ"));
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void update_Success() {
        // Given
        VehiculoRequestDTO updateRequest = new VehiculoRequestDTO();
        updateRequest.setAnio(2021);
        updateRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        updateRequest.setColor("Blanco");
        updateRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);
        updateRequest.setMarca("Honda");
        updateRequest.setModelo("Civic");

        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(repository.save(any(Vehiculo.class))).thenReturn(mockVehiculo);

        // When
        MessageSuccess resultado = vehiculoService.update(1L, updateRequest);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        assertTrue(resultado.getMessage().contains("actualizado correctamente"));
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void update_VehiculoNoExiste() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.update(999L, request);
        });

        assertEquals(401, exception.getStatus());
        assertEquals("El vehiculo no existe", exception.getMessage());
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(Vehiculo.class));
    }

    @Test
    void update_AllFields() {
        // Given
        VehiculoRequestDTO fullUpdateRequest = new VehiculoRequestDTO();
        fullUpdateRequest.setAnio(2023);
        fullUpdateRequest.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        fullUpdateRequest.setColor("Rojo");
        fullUpdateRequest.setEstadoVehiculo(Vehiculo.EstadoVehiculo.INACTIVO);
        fullUpdateRequest.setMarca("Yamaha");
        fullUpdateRequest.setModelo("FZ-16");

        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(repository.save(any(Vehiculo.class))).thenAnswer(invocation -> {
            Vehiculo v = invocation.getArgument(0);
            assertEquals(2023, v.getAnio());
            assertEquals(Vehiculo.TipoVehiculo.DOS_RUEDAS, v.getTipoVehiculo());
            assertEquals("Rojo", v.getColor());
            assertEquals(Vehiculo.EstadoVehiculo.INACTIVO, v.getEstado());
            assertEquals("Yamaha", v.getMarca());
            assertEquals("FZ-16", v.getModelo());
            return v;
        });

        // When
        MessageSuccess resultado = vehiculoService.update(1L, fullUpdateRequest);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    // ==================== CHANGE STATUS TESTS ====================

    @Test
    void changeStatus_ToInactivo_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(repository.save(any(Vehiculo.class))).thenReturn(mockVehiculo);

        // When
        MessageSuccess resultado = vehiculoService.changeStatus("INACTIVO", 1L);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        assertTrue(resultado.getMessage().contains("actualizado correctamente"));
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Vehiculo.class));
    }
/*
    @Test
    void changeStatus_ToSuspendido_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(repository.save(any(Vehiculo.class))).thenReturn(mockVehiculo);

        // When
        MessageSuccess resultado = vehiculoService.changeStatus("SUSPENDIDO", 1L);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        verify(repository, times(1)).save(any(Vehiculo.class));
    }
*/
    @Test
    void changeStatus_VehiculoNoExiste() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.changeStatus("INACTIVO", 999L);
        });

        assertEquals(401, exception.getStatus());
        assertEquals("El vehiculo no existe", exception.getMessage());
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(Vehiculo.class));
    }

    @Test
    void changeStatus_ToActivo_Success() {
        // Given
        mockVehiculo.setEstado(Vehiculo.EstadoVehiculo.INACTIVO);
        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(repository.save(any(Vehiculo.class))).thenAnswer(invocation -> {
            Vehiculo v = invocation.getArgument(0);
            assertEquals(Vehiculo.EstadoVehiculo.ACTIVO, v.getEstado());
            return v;
        });

        // When
        MessageSuccess resultado = vehiculoService.changeStatus("ACTIVO", 1L);

        // Then
        assertNotNull(resultado);
        assertEquals(201, resultado.getCode());
        verify(repository, times(1)).save(any(Vehiculo.class));
    }

    // ==================== GET ALL BY PERSONA TESTS ====================

    @Test
    void getAllByPersona_Success() {
        // Given
        List<Usuario> usuarios = Arrays.asList(mockUsuario);
        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo);

        when(usuarioService.getUsersByRol("CLIENTE")).thenReturn(usuarios);
        when(repository.findByPropietarioIdPersona(1L)).thenReturn(vehiculos);
        when(vehicleMap.map(any(Vehiculo.class))).thenReturn(mockVehicleResponseDTO);
        when(usuarioPersonaRolMap.map(any(Usuario.class))).thenReturn(null);

        // When
        List<VehiculosPropietarioDTO> resultado = vehiculoService.getAllByPersona();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioService, times(1)).getUsersByRol("CLIENTE");
        verify(repository, times(1)).findByPropietarioIdPersona(1L);
        verify(vehicleMap, times(1)).map(any(Vehiculo.class));
    }

    @Test
    void getAllByPersona_EmptyUsers() {
        // Given
        when(usuarioService.getUsersByRol("CLIENTE")).thenReturn(new ArrayList<>());

        // When
        List<VehiculosPropietarioDTO> resultado = vehiculoService.getAllByPersona();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioService, times(1)).getUsersByRol("CLIENTE");
        verify(repository, never()).findByPropietarioIdPersona(anyLong());
    }

    @Test
    void getAllByPersona_MultipleUsers() {
        // Given
        Persona persona2 = new Persona();
        persona2.setIdPersona(2L);
        persona2.setNombre("Maria");
        persona2.setApellido("Garcia");

        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2L);
        usuario2.setPersona(persona2);

        Vehiculo vehiculo2 = new Vehiculo();
        vehiculo2.setId(2L);
        vehiculo2.setPlaca("XYZ789");
        vehiculo2.setPropietario(persona2);

        List<Usuario> usuarios = Arrays.asList(mockUsuario, usuario2);
        List<Vehiculo> vehiculos1 = Arrays.asList(mockVehiculo);
        List<Vehiculo> vehiculos2 = Arrays.asList(vehiculo2);

        when(usuarioService.getUsersByRol("CLIENTE")).thenReturn(usuarios);
        when(repository.findByPropietarioIdPersona(1L)).thenReturn(vehiculos1);
        when(repository.findByPropietarioIdPersona(2L)).thenReturn(vehiculos2);
        when(vehicleMap.map(any(Vehiculo.class))).thenReturn(mockVehicleResponseDTO);
        when(usuarioPersonaRolMap.map(any(Usuario.class))).thenReturn(null);

        // When
        List<VehiculosPropietarioDTO> resultado = vehiculoService.getAllByPersona();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repository, times(1)).findByPropietarioIdPersona(1L);
        verify(repository, times(1)).findByPropietarioIdPersona(2L);
    }

    @Test
    void getAllByPersona_UserWithNoVehicles() {
        // Given
        List<Usuario> usuarios = Arrays.asList(mockUsuario);

        when(usuarioService.getUsersByRol("CLIENTE")).thenReturn(usuarios);
        when(repository.findByPropietarioIdPersona(1L)).thenReturn(new ArrayList<>());
        when(usuarioPersonaRolMap.map(any(Usuario.class))).thenReturn(null);

        // When
        List<VehiculosPropietarioDTO> resultado = vehiculoService.getAllByPersona();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getVehiculos().isEmpty());
        verify(vehicleMap, never()).map(any(Vehiculo.class));
    }

    // ==================== GET ALL TESTS ====================

    @Test
    void getAll_Success() {
        // Given
        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo);

        when(repository.findAll()).thenReturn(vehiculos);
        when(vehicleMap.map(any(Vehiculo.class))).thenReturn(mockVehicleResponseDTO);

        // When
        List<VehicleResponsDTO> resultado = vehiculoService.getAll();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("P123ABC", resultado.get(0).getPlaca());
        verify(repository, times(1)).findAll();
        verify(vehicleMap, times(1)).map(any(Vehiculo.class));
    }

    @Test
    void getAll_EmptyList() {
        // Given
        when(repository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<VehicleResponsDTO> resultado = vehiculoService.getAll();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
        verify(vehicleMap, never()).map(any(Vehiculo.class));
    }

    @Test
    void getAll_MultipleVehicles() {
        // Given
        Vehiculo vehiculo2 = new Vehiculo();
        vehiculo2.setId(2L);
        vehiculo2.setPlaca("XYZ789");

        Vehiculo vehiculo3 = new Vehiculo();
        vehiculo3.setId(3L);
        vehiculo3.setPlaca("DEF456");

        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo, vehiculo2, vehiculo3);

        when(repository.findAll()).thenReturn(vehiculos);
        when(vehicleMap.map(any(Vehiculo.class))).thenReturn(mockVehicleResponseDTO);

        // When
        List<VehicleResponsDTO> resultado = vehiculoService.getAll();

        // Then
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(repository, times(1)).findAll();
        verify(vehicleMap, times(3)).map(any(Vehiculo.class));
    }

    @Test
    void getAll_WithDifferentVehicleTypes() {
        // Given
        Vehiculo moto = new Vehiculo();
        moto.setId(2L);
        moto.setPlaca("M123");
        moto.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);

        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo, moto);

        VehicleResponsDTO motoDTO = new VehicleResponsDTO();
        motoDTO.setId(2L);
        motoDTO.setPlaca("M123");
        motoDTO.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);

        when(repository.findAll()).thenReturn(vehiculos);
        when(vehicleMap.map(mockVehiculo)).thenReturn(mockVehicleResponseDTO);
        when(vehicleMap.map(moto)).thenReturn(motoDTO);

        // When
        List<VehicleResponsDTO> resultado = vehiculoService.getAll();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repository, times(1)).findAll();
    }

    // ==================== EDGE CASES ====================

    @Test
    void create_WithNullDpi() {
        // Given
        when(repository.findByPlaca(request.getPlaca())).thenReturn(null);
        when(personaService.findByDpi(null)).thenReturn(null);

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.create(null, request);
        });

        assertEquals(401, exception.getStatus());
        verify(personaService, times(1)).findByDpi(null);
    }

    @Test
    void update_WithNullId() {
        // Given
        when(repository.findById(null)).thenReturn(Optional.empty());

        // When / Then
        ErrorApi exception = assertThrows(ErrorApi.class, () -> {
            vehiculoService.update(null, request);
        });

        assertEquals(401, exception.getStatus());
    }

    @Test
    void changeStatus_WithInvalidStatus() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(mockVehiculo));

        // When / Then - IllegalArgumentException debido al enum inválido
        assertThrows(IllegalArgumentException.class, () -> {
            vehiculoService.changeStatus("ESTADO_INVALIDO", 1L);
        });
    }
}
