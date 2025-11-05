package org.parkcontrol.apiparkcontrol.controllers.empresa_sucursal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.CreateSucursalDTO;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.UsuarioSucursalDTO;
import org.parkcontrol.apiparkcontrol.services.empresa_sucursal.EmpresaSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = EmpresaSucursalController.class)
@Import(TestSecurityConfig.class)
class EmpresaSucursalControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmpresaSucursalService empresaSucursalService;

    @Autowired
    private ObjectMapper objectMapper;

    private ObtenerSucursalesEmpresaDTO mockSucursalEmpresa;
    private CreateSucursalDTO createSucursalDTO;

    @BeforeEach
    void setUp() {
        // Setup mock usuario sucursal
        UsuarioSucursalDTO usuarioSucursal = new UsuarioSucursalDTO();
        usuarioSucursal.setNombre("Juan");
        usuarioSucursal.setApellido("Pérez");
        usuarioSucursal.setFechaNacimiento("1985-01-15");
        usuarioSucursal.setDpi("1234567890123");
        usuarioSucursal.setCorreo("juan.perez@sucursal.com");
        usuarioSucursal.setTelefono("12345678");
        usuarioSucursal.setDireccionCompleta("Calle Principal 123");
        usuarioSucursal.setCiudad("Guatemala");
        usuarioSucursal.setPais("Guatemala");
        usuarioSucursal.setCodigoPostal("01001");
        usuarioSucursal.setNombreUsuario("jperez");
        usuarioSucursal.setDobleFactorHabilitado(false);
        usuarioSucursal.setEstado("ACTIVO");

        // Setup mock sucursal
        ObtenerSucursalesEmpresaDTO.SucursalDTO sucursal = new ObtenerSucursalesEmpresaDTO.SucursalDTO();
        sucursal.setIdSucursal(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccionCompleta("Avenida Principal 456");
        sucursal.setCiudad("Guatemala");
        sucursal.setDepartamento("Guatemala");
        sucursal.setHoraApertura("08:00");
        sucursal.setHoraCierre("18:00");
        sucursal.setCapacidad2Ruedas(50);
        sucursal.setCapacidad4Ruedas(100);
        sucursal.setEstado("ACTIVA");
        sucursal.setUsuario(usuarioSucursal);

        mockSucursalEmpresa = new ObtenerSucursalesEmpresaDTO();
        mockSucursalEmpresa.setSucursalDTO(sucursal);

        // Setup CreateSucursalDTO
        createSucursalDTO = new CreateSucursalDTO();
        createSucursalDTO.setIdEmpresa(1L);
        // Usuario data
        createSucursalDTO.setNombre("María");
        createSucursalDTO.setApellido("González");
        createSucursalDTO.setFechaNacimiento("1990-05-20");
        createSucursalDTO.setDpi("9876543210987");
        createSucursalDTO.setCorreo("maria.gonzalez@nueva.com");
        createSucursalDTO.setTelefono("87654321");
        createSucursalDTO.setDireccionCompleta("Zona 10, Guatemala");
        createSucursalDTO.setCiudad("Guatemala");
        createSucursalDTO.setPais("Guatemala");
        createSucursalDTO.setCodigoPostal("01010");
        createSucursalDTO.setNombreUsuario("mgonzalez");
        createSucursalDTO.setContraseniaHash("password123");
        createSucursalDTO.setDobleFactorHabilitado(true);
        // Sucursal data
        createSucursalDTO.setNombreSucursal("Nueva Sucursal");
        createSucursalDTO.setDireccionCompletaSucursal("Boulevard Los Próceres");
        createSucursalDTO.setCiudadSucursal("Guatemala");
        createSucursalDTO.setDepartamentoSucursal("Guatemala");
        createSucursalDTO.setHoraApertura("07:00");
        createSucursalDTO.setHoraCierre("19:00");
        createSucursalDTO.setCapacidad2Ruedas(60);
        createSucursalDTO.setCapacidad4Ruedas(120);
        createSucursalDTO.setLatitud(14.6349);
        createSucursalDTO.setLongitud(-90.5069);
        createSucursalDTO.setTelefonoContactoSucursal("22334455");
        createSucursalDTO.setCorreoContactoSucursal("nueva@sucursal.com");
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_Success() throws Exception {
        // Arrange
        List<ObtenerSucursalesEmpresaDTO> mockResponse = Arrays.asList(mockSucursalEmpresa);
        when(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(1L)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sucursalDTO.idSucursal", is(1)))
                .andExpect(jsonPath("$[0].sucursalDTO.nombre", is("Sucursal Centro")))
                .andExpect(jsonPath("$[0].sucursalDTO.direccionCompleta", is("Avenida Principal 456")))
                .andExpect(jsonPath("$[0].sucursalDTO.ciudad", is("Guatemala")))
                .andExpect(jsonPath("$[0].sucursalDTO.departamento", is("Guatemala")))
                .andExpect(jsonPath("$[0].sucursalDTO.horaApertura", is("08:00")))
                .andExpect(jsonPath("$[0].sucursalDTO.horaCierre", is("18:00")))
                .andExpect(jsonPath("$[0].sucursalDTO.capacidad2Ruedas", is(50)))
                .andExpect(jsonPath("$[0].sucursalDTO.capacidad4Ruedas", is(100)))
                .andExpect(jsonPath("$[0].sucursalDTO.estado", is("ACTIVA")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.nombre", is("Juan")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.apellido", is("Pérez")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.dpi", is("1234567890123")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.correo", is("juan.perez@sucursal.com")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.nombreUsuario", is("jperez")))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.dobleFactorHabilitado", is(false)))
                .andExpect(jsonPath("$[0].sucursalDTO.usuario.estado", is("ACTIVO")));

        verify(empresaSucursalService).obtenerUsuariosSucursalPorEmpresa(1L);
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_EmptyList() throws Exception {
        // Arrange
        when(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(empresaSucursalService).obtenerUsuariosSucursalPorEmpresa(1L);
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_ServiceException() throws Exception {
        // Arrange
        when(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(1L))
                .thenThrow(new RuntimeException("Error al obtener sucursales"));

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener los usuarios de sucursal")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener sucursales")));

        verify(empresaSucursalService).obtenerUsuariosSucursalPorEmpresa(1L);
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_InvalidIdEmpresa() throws Exception {
        // Arrange
        when(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(-1L))
                .thenThrow(new IllegalArgumentException("ID de empresa inválido"));

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", -1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("ID de empresa inválido")));

        verify(empresaSucursalService).obtenerUsuariosSucursalPorEmpresa(-1L);
    }

    @Test
    void testCreateSucursal_Success() throws Exception {
        // Arrange
        String expectedMessage = "Sucursal creada exitosamente";
        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class))).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSucursalDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_ServiceException() throws Exception {
        // Arrange
        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class)))
                .thenThrow(new RuntimeException("Empresa no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSucursalDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al crear la sucursal")))
                .andExpect(jsonPath("$.message", containsString("Empresa no encontrada")));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(empresaSucursalService, never()).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(empresaSucursalService, never()).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_NullFields() throws Exception {
        // Arrange - DTO with null required fields
        CreateSucursalDTO nullFieldsDTO = new CreateSucursalDTO();
        nullFieldsDTO.setIdEmpresa(null);
        nullFieldsDTO.setNombre(null);

        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class)))
                .thenThrow(new IllegalArgumentException("Campos requeridos no pueden ser nulos"));

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullFieldsDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Campos requeridos no pueden ser nulos")));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_DuplicatedData() throws Exception {
        // Arrange
        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class)))
                .thenThrow(new RuntimeException("DPI ya registrado en el sistema"));

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSucursalDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("DPI ya registrado en el sistema")));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_MultipleSuccessCreations() throws Exception {
        // Test para verificar múltiples creaciones exitosas
        String expectedMessage = "Sucursal creada exitosamente";
        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class))).thenReturn(expectedMessage);

        // Primera creación
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSucursalDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        // Segunda creación
        CreateSucursalDTO secondSucursal = new CreateSucursalDTO();
        secondSucursal.setIdEmpresa(2L);
        secondSucursal.setNombre("Carlos");
        secondSucursal.setApellido("Rodríguez");
        secondSucursal.setNombreSucursal("Sucursal Norte");

        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondSucursal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(empresaSucursalService, times(2)).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testGetUsuariosSucursalByEmpresa_LargeDataSet() throws Exception {
        // Arrange - Simulate large dataset
        List<ObtenerSucursalesEmpresaDTO> largeMockResponse = Arrays.asList(
            mockSucursalEmpresa,
            mockSucursalEmpresa,
            mockSucursalEmpresa
        );

        when(empresaSucursalService.obtenerUsuariosSucursalPorEmpresa(1L)).thenReturn(largeMockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/empresa-sucursal/sucursales/{idEmpresa}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].sucursalDTO.nombre", is("Sucursal Centro")))
                .andExpect(jsonPath("$[1].sucursalDTO.nombre", is("Sucursal Centro")))
                .andExpect(jsonPath("$[2].sucursalDTO.nombre", is("Sucursal Centro")));

        verify(empresaSucursalService).obtenerUsuariosSucursalPorEmpresa(1L);
    }

    @Test
    void testCreateSucursal_ValidationErrors() throws Exception {
        // Test adicional para validaciones específicas del DTO
        CreateSucursalDTO invalidDTO = new CreateSucursalDTO();
        // Solo establecer algunos campos para simular validaciones faltantes
        invalidDTO.setIdEmpresa(1L);
        invalidDTO.setNombre("Test");
        // Faltan campos obligatorios como apellido, dpi, etc.

        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class)))
                .thenThrow(new RuntimeException("Campos obligatorios faltantes"));

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Campos obligatorios faltantes")));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }

    @Test
    void testCreateSucursal_BusinessLogicValidation() throws Exception {
        // Test para validaciones de lógica de negocio
        when(empresaSucursalService.crearNuevaSucursal(any(CreateSucursalDTO.class)))
                .thenThrow(new RuntimeException("Nombre de usuario ya existe"));

        // Act & Assert
        mockMvc.perform(post("/api/empresa-sucursal/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSucursalDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Nombre de usuario ya existe")));

        verify(empresaSucursalService).crearNuevaSucursal(any(CreateSucursalDTO.class));
    }
}
