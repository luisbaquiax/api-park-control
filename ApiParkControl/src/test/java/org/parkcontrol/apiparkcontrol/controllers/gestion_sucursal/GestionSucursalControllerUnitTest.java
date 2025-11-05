package org.parkcontrol.apiparkcontrol.controllers.gestion_sucursal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
import org.parkcontrol.apiparkcontrol.services.gestion_sucursal.GestionSucursalService;
import org.parkcontrol.apiparkcontrol.services.gestion_sucursal.GestionTarifaSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = GestionSucursalController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
class GestionSucursalControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestionSucursalService gestionSucursalService;

    @MockBean
    private GestionTarifaSucursalService gestionTarifaSucursalService;

    @Autowired
    private ObjectMapper objectMapper;

    private GetSucursalDTO mockGetSucursalDTO;
    private EditarSucursalDTO mockEditarSucursalDTO;
    private NuevaTarifaSucursalDTO mockNuevaTarifaDTO;
    private TarifaSucursalDTO mockTarifaSucursalDTO;
    private List<TarifaSucursalDTO> mockTarifasList;

    @BeforeEach
    void setUp() {
        // Setup mock empresa
        GetSucursalDTO.EmpresaSucursalDTO empresaDTO = new GetSucursalDTO.EmpresaSucursalDTO(
                1L,
                "Test Company",
                "Test Company S.A.",
                "1234567-8",
                "Test Fiscal Address",
                "12345678",
                "empresa@test.com"
        );

        // Setup mock sucursal - el servicio debe estar devolviendo un DTO diferente al GetSucursalDTO
        // Ya que el JSON que recibes tiene "direccionCompletaSucursal" en lugar de "direccionCompleta"
        mockGetSucursalDTO = new GetSucursalDTO(
                1L,
                "Sucursal Centro",
                "Avenida Principal 456",
                "Guatemala",
                "Guatemala",
                "08:00",
                "18:00",
                50,
                100,
                14.6349,
                -90.5069,
                "22334455",
                "sucursal@test.com",
                "ACTIVA",
                empresaDTO
        );

        // Setup mock editar sucursal DTO
        mockEditarSucursalDTO = new EditarSucursalDTO();
        mockEditarSucursalDTO.setIdSucursal(1L);
        mockEditarSucursalDTO.setNombreSucursal("Sucursal Centro Actualizada");
        mockEditarSucursalDTO.setDireccionCompletaSucursal("Nueva Avenida 789");
        mockEditarSucursalDTO.setCiudadSucursal("Guatemala");
        mockEditarSucursalDTO.setDepartamentoSucursal("Guatemala");
        mockEditarSucursalDTO.setHoraApertura("07:00");
        mockEditarSucursalDTO.setHoraCierre("19:00");
        mockEditarSucursalDTO.setCapacidad2Ruedas(60);
        mockEditarSucursalDTO.setCapacidad4Ruedas(120);
        mockEditarSucursalDTO.setLatitud(14.6400);
        mockEditarSucursalDTO.setLongitud(-90.5100);
        mockEditarSucursalDTO.setTelefonoContactoSucursal("22334466");
        mockEditarSucursalDTO.setCorreoContactoSucursal("nueva@sucursal.com");
        mockEditarSucursalDTO.setEstadoSucursal("ACTIVA");

        // Setup mock nueva tarifa DTO
        mockNuevaTarifaDTO = new NuevaTarifaSucursalDTO();
        mockNuevaTarifaDTO.setIdUsuarioSucursal(1L);
        mockNuevaTarifaDTO.setPrecioPorHora("15.00");
        mockNuevaTarifaDTO.setMoneda("GTQ");
        mockNuevaTarifaDTO.setFechaVigenciaInicio("2024-01-01");
        mockNuevaTarifaDTO.setFechaVigenciaFin("2024-12-31");
        mockNuevaTarifaDTO.setEsTarifaBase(false);

        // Setup mock tarifa sucursal DTO
        mockTarifaSucursalDTO = new TarifaSucursalDTO();
        mockTarifaSucursalDTO.setIdUsuarioSucursal(1L);
        mockTarifaSucursalDTO.setIdTarifaSucursal(1L);
        mockTarifaSucursalDTO.setPrecioPorHora(15.00);
        mockTarifaSucursalDTO.setMoneda("GTQ");
        mockTarifaSucursalDTO.setFechaVigenciaInicio("2024-01-01T00:00:00");
        mockTarifaSucursalDTO.setFechaVigenciaFin("2024-12-31T23:59:59");
        mockTarifaSucursalDTO.setEstado("VIGENTE");

        mockTarifasList = Arrays.asList(mockTarifaSucursalDTO);
    }

    @Test
    void testGetMiSucursal_Success() throws Exception {
        // Arrange
        when(gestionSucursalService.obtenerSucursalPorUsuario(1L)).thenReturn(mockGetSucursalDTO);

        // Act & Assert - Actualizar para coincidir con la respuesta real del servicio
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idSucursal", is(1)))
                .andExpect(jsonPath("$.nombreSucursal", is("Sucursal Centro")))
                .andExpect(jsonPath("$.direccionCompletaSucursal", is("Avenida Principal 456")))
                .andExpect(jsonPath("$.ciudadSucursal", is("Guatemala")))
                .andExpect(jsonPath("$.departamentoSucursal", is("Guatemala")))
                .andExpect(jsonPath("$.horaApertura", is("08:00")))
                .andExpect(jsonPath("$.horaCierre", is("18:00")))
                .andExpect(jsonPath("$.capacidad2Ruedas", is(50)))
                .andExpect(jsonPath("$.capacidad4Ruedas", is(100)))
                .andExpect(jsonPath("$.latitud", is(14.6349)))
                .andExpect(jsonPath("$.longitud", is(-90.5069)))
                .andExpect(jsonPath("$.telefonoContactoSucursal", is("22334455")))
                .andExpect(jsonPath("$.correoContactoSucursal", is("sucursal@test.com")))
                .andExpect(jsonPath("$.estadoSucursal", is("ACTIVA")));

        verify(gestionSucursalService).obtenerSucursalPorUsuario(1L);
    }

    @Test
    void testGetMiSucursal_ServiceException() throws Exception {
        // Arrange
        when(gestionSucursalService.obtenerSucursalPorUsuario(1L))
                .thenThrow(new RuntimeException("Sucursal no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener la sucursal")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada")));

        verify(gestionSucursalService).obtenerSucursalPorUsuario(1L);
    }

    @Test
    void testGetMiSucursal_InvalidIdUsuario() throws Exception {
        // Arrange
        when(gestionSucursalService.obtenerSucursalPorUsuario(-1L))
                .thenThrow(new RuntimeException("Usuario no válido"));

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", -1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Usuario no válido")));

        verify(gestionSucursalService).obtenerSucursalPorUsuario(-1L);
    }

    @Test
    void testEditarMiSucursal_Success() throws Exception {
        // Arrange
        String expectedMessage = "Sucursal editada exitosamente.";
        when(gestionSucursalService.editarSucursal(any(EditarSucursalDTO.class))).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockEditarSucursalDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionSucursalService).editarSucursal(any(EditarSucursalDTO.class));
    }

    @Test
    void testEditarMiSucursal_ServiceException() throws Exception {
        // Arrange
        when(gestionSucursalService.editarSucursal(any(EditarSucursalDTO.class)))
                .thenThrow(new RuntimeException("Error al actualizar sucursal"));

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockEditarSucursalDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al editar la sucursal")))
                .andExpect(jsonPath("$.message", containsString("Error al actualizar sucursal")));

        verify(gestionSucursalService).editarSucursal(any(EditarSucursalDTO.class));
    }

    @Test
    void testEditarMiSucursal_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(gestionSucursalService, never()).editarSucursal(any(EditarSucursalDTO.class));
    }

    @Test
    void testEditarMiSucursal_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(gestionSucursalService, never()).editarSucursal(any(EditarSucursalDTO.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_Success() throws Exception {
        // Arrange
        String expectedMessage = "Nueva tarifa de sucursal creada exitosamente.";
        when(gestionTarifaSucursalService.crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class)))
                .thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevaTarifaDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionTarifaSucursalService).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_ServiceException() throws Exception {
        // Arrange
        when(gestionTarifaSucursalService.crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class)))
                .thenThrow(new Exception("Sucursal no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevaTarifaDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al crear la nueva tarifa de sucursal")))
                .andExpect(jsonPath("$.message", containsString("Sucursal no encontrada")));

        verify(gestionTarifaSucursalService).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(gestionTarifaSucursalService, never()).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_TarifaBase() throws Exception {
        // Arrange - Test with tarifa base
        NuevaTarifaSucursalDTO tarifaBaseDTO = new NuevaTarifaSucursalDTO();
        tarifaBaseDTO.setIdUsuarioSucursal(1L);
        tarifaBaseDTO.setPrecioPorHora("20.00");
        tarifaBaseDTO.setMoneda("GTQ");
        tarifaBaseDTO.setFechaVigenciaInicio("2024-01-01");
        tarifaBaseDTO.setFechaVigenciaFin("2024-12-31");
        tarifaBaseDTO.setEsTarifaBase(true);

        String expectedMessage = "Nueva tarifa base de sucursal creada exitosamente.";
        when(gestionTarifaSucursalService.crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class)))
                .thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifaBaseDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionTarifaSucursalService).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
    }

    @Test
    void testEditarTarifaSucursal_Success() throws Exception {
        // Arrange
        String expectedMessage = "Tarifa de sucursal editada exitosamente.";
        when(gestionTarifaSucursalService.editarTarifaSucursal(any(TarifaSucursalDTO.class)))
                .thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockTarifaSucursalDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionTarifaSucursalService).editarTarifaSucursal(any(TarifaSucursalDTO.class));
    }

    @Test
    void testEditarTarifaSucursal_ServiceException() throws Exception {
        // Arrange
        when(gestionTarifaSucursalService.editarTarifaSucursal(any(TarifaSucursalDTO.class)))
                .thenThrow(new Exception("Tarifa no encontrada"));

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockTarifaSucursalDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al editar la tarifa de sucursal")))
                .andExpect(jsonPath("$.message", containsString("Tarifa no encontrada")));

        verify(gestionTarifaSucursalService).editarTarifaSucursal(any(TarifaSucursalDTO.class));
    }

    @Test
    void testEditarTarifaSucursal_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(gestionTarifaSucursalService, never()).editarTarifaSucursal(any(TarifaSucursalDTO.class));
    }

    @Test
    void testGetTarifasSucursalPorUsuario_Success() throws Exception {
        // Arrange
        when(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(1L)).thenReturn(mockTarifasList);

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuarioSucursal", is(1)))
                .andExpect(jsonPath("$[0].idTarifaSucursal", is(1)))
                .andExpect(jsonPath("$[0].precioPorHora", is(15.0)))
                .andExpect(jsonPath("$[0].moneda", is("GTQ")))
                .andExpect(jsonPath("$[0].fechaVigenciaInicio", is("2024-01-01T00:00:00")))
                .andExpect(jsonPath("$[0].fechaVigenciaFin", is("2024-12-31T23:59:59")))
                .andExpect(jsonPath("$[0].estado", is("VIGENTE")));

        verify(gestionTarifaSucursalService).obtenerTarifasPorIdUsuario(1L);
    }

    @Test
    void testGetTarifasSucursalPorUsuario_EmptyList() throws Exception {
        // Arrange
        when(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(gestionTarifaSucursalService).obtenerTarifasPorIdUsuario(1L);
    }

    @Test
    void testGetTarifasSucursalPorUsuario_ServiceException() throws Exception {
        // Arrange
        when(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(1L))
                .thenThrow(new Exception("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener las tarifas de sucursal")))
                .andExpect(jsonPath("$.message", containsString("Usuario no encontrado")));

        verify(gestionTarifaSucursalService).obtenerTarifasPorIdUsuario(1L);
    }

    @Test
    void testGetTarifasSucursalPorUsuario_MultipleTarifas() throws Exception {
        // Arrange - Multiple tarifas
        TarifaSucursalDTO tarifa2 = new TarifaSucursalDTO();
        tarifa2.setIdUsuarioSucursal(1L);
        tarifa2.setIdTarifaSucursal(2L);
        tarifa2.setPrecioPorHora(18.00);
        tarifa2.setMoneda("GTQ");
        tarifa2.setFechaVigenciaInicio("2024-06-01T00:00:00");
        tarifa2.setFechaVigenciaFin("2024-12-31T23:59:59");
        tarifa2.setEstado("PROGRAMADO");

        List<TarifaSucursalDTO> multipleTarifas = Arrays.asList(mockTarifaSucursalDTO, tarifa2);
        when(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(1L)).thenReturn(multipleTarifas);

        // Act & Assert
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].precioPorHora", is(15.0)))
                .andExpect(jsonPath("$[0].estado", is("VIGENTE")))
                .andExpect(jsonPath("$[1].precioPorHora", is(18.0)))
                .andExpect(jsonPath("$[1].estado", is("PROGRAMADO")));

        verify(gestionTarifaSucursalService).obtenerTarifasPorIdUsuario(1L);
    }

    @Test
    void testEditarMiSucursal_AllFieldsUpdated() throws Exception {
        // Arrange - Test with all fields updated
        EditarSucursalDTO completeUpdateDTO = new EditarSucursalDTO();
        completeUpdateDTO.setIdSucursal(1L);
        completeUpdateDTO.setNombreSucursal("Sucursal Completamente Nueva");
        completeUpdateDTO.setDireccionCompletaSucursal("Dirección Totalmente Nueva");
        completeUpdateDTO.setCiudadSucursal("Nueva Ciudad");
        completeUpdateDTO.setDepartamentoSucursal("Nuevo Departamento");
        completeUpdateDTO.setHoraApertura("06:00");
        completeUpdateDTO.setHoraCierre("20:00");
        completeUpdateDTO.setCapacidad2Ruedas(80);
        completeUpdateDTO.setCapacidad4Ruedas(160);
        completeUpdateDTO.setLatitud(15.0000);
        completeUpdateDTO.setLongitud(-91.0000);
        completeUpdateDTO.setTelefonoContactoSucursal("99887766");
        completeUpdateDTO.setCorreoContactoSucursal("nueva@completa.com");
        completeUpdateDTO.setEstadoSucursal("MANTENIMIENTO");

        String expectedMessage = "Sucursal completamente actualizada.";
        when(gestionSucursalService.editarSucursal(any(EditarSucursalDTO.class))).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionSucursalService).editarSucursal(any(EditarSucursalDTO.class));
    }

    @Test
    void testCrearNuevaTarifaSucursal_WithoutEndDate() throws Exception {
        // Arrange - Test creating tarifa without end date
        NuevaTarifaSucursalDTO tarifaSinFin = new NuevaTarifaSucursalDTO();
        tarifaSinFin.setIdUsuarioSucursal(1L);
        tarifaSinFin.setPrecioPorHora("25.00");
        tarifaSinFin.setMoneda("GTQ");
        tarifaSinFin.setFechaVigenciaInicio("2024-01-01");
        tarifaSinFin.setFechaVigenciaFin(null); // No end date
        tarifaSinFin.setEsTarifaBase(false);

        String expectedMessage = "Nueva tarifa sin fecha fin creada exitosamente.";
        when(gestionTarifaSucursalService.crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class)))
                .thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarifaSinFin)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionTarifaSucursalService).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
    }

    @Test
    void testEditarTarifaSucursal_ChangeState() throws Exception {
        // Arrange - Test changing tarifa state
        TarifaSucursalDTO changeStateDTO = new TarifaSucursalDTO();
        changeStateDTO.setIdUsuarioSucursal(1L);
        changeStateDTO.setIdTarifaSucursal(1L);
        changeStateDTO.setPrecioPorHora(15.00);
        changeStateDTO.setMoneda("GTQ");
        changeStateDTO.setFechaVigenciaInicio("2024-01-01T00:00:00");
        changeStateDTO.setFechaVigenciaFin("2024-12-31T23:59:59");
        changeStateDTO.setEstado("HISTORICO");

        String expectedMessage = "Estado de tarifa cambiado exitosamente.";
        when(gestionTarifaSucursalService.editarTarifaSucursal(any(TarifaSucursalDTO.class)))
                .thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeStateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(gestionTarifaSucursalService).editarTarifaSucursal(any(TarifaSucursalDTO.class));
    }

    @Test
    void testMultipleOperationsSequence() throws Exception {
        // Test sequence: Get sucursal -> Edit sucursal -> Create tarifa -> Get tarifas

        // Step 1: Get sucursal
        when(gestionSucursalService.obtenerSucursalPorUsuario(1L)).thenReturn(mockGetSucursalDTO);
        mockMvc.perform(get("/api/sucursal/mi-sucursal/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk());

        // Step 2: Edit sucursal
        when(gestionSucursalService.editarSucursal(any(EditarSucursalDTO.class)))
                .thenReturn("Sucursal editada exitosamente.");
        mockMvc.perform(put("/api/sucursal/mi-sucursal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockEditarSucursalDTO)))
                .andExpect(status().isOk());

        // Step 3: Create tarifa
        when(gestionTarifaSucursalService.crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class)))
                .thenReturn("Nueva tarifa creada exitosamente.");
        mockMvc.perform(post("/api/sucursal/mi-sucursal/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevaTarifaDTO)))
                .andExpect(status().isOk());

        // Step 4: Get tarifas
        when(gestionTarifaSucursalService.obtenerTarifasPorIdUsuario(1L)).thenReturn(mockTarifasList);
        mockMvc.perform(get("/api/sucursal/mi-sucursal/tarifas/{idUsuarioSucursal}", 1L))
                .andExpect(status().isOk());

        // Verify all service calls
        verify(gestionSucursalService).obtenerSucursalPorUsuario(1L);
        verify(gestionSucursalService).editarSucursal(any(EditarSucursalDTO.class));
        verify(gestionTarifaSucursalService).crearNuevaTarifaSucursal(any(NuevaTarifaSucursalDTO.class));
        verify(gestionTarifaSucursalService).obtenerTarifasPorIdUsuario(1L);
    }
}
