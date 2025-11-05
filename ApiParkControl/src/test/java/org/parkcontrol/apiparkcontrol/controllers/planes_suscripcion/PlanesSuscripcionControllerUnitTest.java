package org.parkcontrol.apiparkcontrol.controllers.planes_suscripcion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.*;
import org.parkcontrol.apiparkcontrol.services.planes_suscripcion.PlanesSuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@WebMvcTest(controllers = PlanesSuscripcionController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
class PlanesSuscripcionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanesSuscripcionService planesSuscripcionService;

    @Autowired
    private ObjectMapper objectMapper;

    private DetalleTipoPlanDTO mockDetalleTipoPlan;
    private NuevoPlanDTO mockNuevoPlan;

    @BeforeEach
    void setUp() {
        // Setup mock DetalleTipoPlanDTO
        DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configDTO = new DetalleTipoPlanDTO.ConfiguracionDescuentoDTO();
        configDTO.setIdConfiguracionDescuento(1L);
        configDTO.setDescuentoMensual(15.00);
        configDTO.setDescuentoAnualAdicional(5.00);
        configDTO.setFechaVigenciaInicio("2024-01-01T00:00:00");
        configDTO.setFechaVigenciaFin("2024-12-31T23:59:59");
        configDTO.setEstadoConfiguracion("VIGENTE");
        configDTO.setIdUsuarioCreacion(1L);
        configDTO.setFechaCreacionDescuento("2024-01-01T00:00:00");

        mockDetalleTipoPlan = new DetalleTipoPlanDTO();
        mockDetalleTipoPlan.setId(1L);
        mockDetalleTipoPlan.setIdEmpresa(1L);
        mockDetalleTipoPlan.setNombrePlan("WORKWEEK");
        mockDetalleTipoPlan.setCodigoPlan("WW-001");
        mockDetalleTipoPlan.setDescripcion("Plan Workweek");
        mockDetalleTipoPlan.setPrecioPlan(300.00);
        mockDetalleTipoPlan.setHorasDia(8);
        mockDetalleTipoPlan.setHorasMensuales(160);
        mockDetalleTipoPlan.setDiasAplicables("L-M-X-J-V");
        mockDetalleTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        mockDetalleTipoPlan.setOrdenBeneficio(2);
        mockDetalleTipoPlan.setActivo("VIGENTE");
        mockDetalleTipoPlan.setFechaCreacion("2024-01-01T00:00:00");
        mockDetalleTipoPlan.setConfiguracionDescuento(configDTO);

        // Setup mock NuevoPlanDTO
        mockNuevoPlan = new NuevoPlanDTO();
        mockNuevoPlan.setIdEmpresa(1L);
        mockNuevoPlan.setNombrePlan("WORKWEEK");
        mockNuevoPlan.setCodigoPlan("WW-001");
        mockNuevoPlan.setDescripcion("Plan Workweek para uso de lunes a viernes");
        mockNuevoPlan.setPrecioPlan(300.00);
        mockNuevoPlan.setHorasMensuales(160);
        mockNuevoPlan.setDiasAplicables("L-M-X-J-V");
        mockNuevoPlan.setCoberturaHoraria("08:00 - 18:00");
        mockNuevoPlan.setDescuentoMensual(15.00);
        mockNuevoPlan.setDescuentoAnualAdicional(5.00);
        mockNuevoPlan.setFechaVigenciaInicio("2024-01-01");
        mockNuevoPlan.setFechaVigenciaFin("2024-12-31");
        mockNuevoPlan.setIdUsuarioCreacion(1L);
    }

    @Test
    void testGetPlanesPorEmpresa_Success() throws Exception {
        // Arrange
        List<DetalleTipoPlanDTO> mockResponse = Arrays.asList(mockDetalleTipoPlan);
        when(planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].idEmpresa", is(1)))
                .andExpect(jsonPath("$[0].nombrePlan", is("WORKWEEK")))
                .andExpect(jsonPath("$[0].codigoPlan", is("WW-001")))
                .andExpect(jsonPath("$[0].descripcion", is("Plan Workweek")))
                .andExpect(jsonPath("$[0].precioPlan", is(300.0)))
                .andExpect(jsonPath("$[0].horasDia", is(8)))
                .andExpect(jsonPath("$[0].horasMensuales", is(160)))
                .andExpect(jsonPath("$[0].diasAplicables", is("L-M-X-J-V")))
                .andExpect(jsonPath("$[0].coberturaHoraria", is("08:00 - 18:00")))
                .andExpect(jsonPath("$[0].ordenBeneficio", is(2)))
                .andExpect(jsonPath("$[0].activo", is("VIGENTE")))
                .andExpect(jsonPath("$[0].configuracionDescuento.idConfiguracionDescuento", is(1)))
                .andExpect(jsonPath("$[0].configuracionDescuento.descuentoMensual", is(15.0)))
                .andExpect(jsonPath("$[0].configuracionDescuento.descuentoAnualAdicional", is(5.0)));

        verify(planesSuscripcionService).obtenerPlanesSuscripcionPorEmpresa(1L);
    }

    @Test
    void testGetPlanesPorEmpresa_EmptyList() throws Exception {
        // Arrange
        when(planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(planesSuscripcionService).obtenerPlanesSuscripcionPorEmpresa(1L);
    }

    @Test
    void testGetPlanesPorEmpresa_ServiceException() throws Exception {
        // Arrange
        when(planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(1L))
                .thenThrow(new RuntimeException("Error al obtener planes"));

        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener los planes de suscripción")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener planes")));

        verify(planesSuscripcionService).obtenerPlanesSuscripcionPorEmpresa(1L);
    }

    @Test
    void testGetPlanesPorEmpresa_InvalidIdUsuario() throws Exception {
        // Arrange
        when(planesSuscripcionService.obtenerPlanesSuscripcionPorEmpresa(-1L))
                .thenThrow(new IllegalArgumentException("ID de usuario inválido"));

        // Act & Assert
        mockMvc.perform(get("/api/empresas/planes-suscripcion/planes/{idUsuario}", -1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("ID de usuario inválido")));

        verify(planesSuscripcionService).obtenerPlanesSuscripcionPorEmpresa(-1L);
    }

    @Test
    void testCrearNuevoPlan_Success() throws Exception {
        // Arrange
        String expectedMessage = "Nuevo plan de suscripción creado con éxito para la empresa con ID: 1";
        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class))).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevoPlan)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_ServiceException() throws Exception {
        // Arrange
        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new RuntimeException("Ya existe un plan con el nombre WORKWEEK"));

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevoPlan)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al crear el nuevo plan de suscripción")))
                .andExpect(jsonPath("$.message", containsString("Ya existe un plan con el nombre WORKWEEK")));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(planesSuscripcionService, never()).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(planesSuscripcionService, never()).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_EmpresaNotFound() throws Exception {
        // Arrange
        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new RuntimeException("Empresa no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevoPlan)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Empresa no encontrada con ID: 999")));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_ValidationErrors() throws Exception {
        // Arrange - Plan with validation issues
        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new RuntimeException("Los porcentajes de descuento no cumplen con las reglas de negocio"));

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevoPlan)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Los porcentajes de descuento no cumplen con las reglas de negocio")));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testEditarPlanSuscripcion_Success() throws Exception {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(1L);
        editarPlanDTO.setIdEmpresa(1L);
        editarPlanDTO.setNombrePlan("WORKWEEK");
        editarPlanDTO.setCodigoPlan("WW-002");
        editarPlanDTO.setDescripcion("Plan Workweek actualizado");
        editarPlanDTO.setPrecioPlan(320.00);
        editarPlanDTO.setHorasMensuales(170);
        editarPlanDTO.setDiasAplicables("L-M-X-J-V");
        editarPlanDTO.setCoberturaHoraria("08:00 - 19:00");
        editarPlanDTO.setDescuentoMensual(16.00);
        editarPlanDTO.setDescuentoAnualAdicional(6.00);
        editarPlanDTO.setFechaVigenciaInicio("2024-02-01");
        editarPlanDTO.setFechaVigenciaFin("2024-12-31");
        editarPlanDTO.setIdUsuarioCreacion(1L);

        String expectedMessage = "Plan de suscripción editado con éxito para la empresa con ID: 1";
        when(planesSuscripcionService.editarPlanSuscripcion(any(NuevoPlanDTO.class))).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarPlanDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(planesSuscripcionService).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testEditarPlanSuscripcion_ServiceException() throws Exception {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(999L);

        when(planesSuscripcionService.editarPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new RuntimeException("Tipo de plan no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al editar el plan de suscripción")))
                .andExpect(jsonPath("$.message", containsString("Tipo de plan no encontrado con ID: 999")));

        verify(planesSuscripcionService).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testEditarPlanSuscripcion_InvalidRequestBody() throws Exception {
        // Arrange - Invalid JSON
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(planesSuscripcionService, never()).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testEditarPlanSuscripcion_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        verify(planesSuscripcionService, never()).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_AllPlanTypes() throws Exception {
        // Test creating all different plan types
        String[] planTypes = {"FULL_ACCESS", "WORKWEEK", "OFFICE_LIGHT", "DIARIO_FLEXIBLE", "NOCTURNO"};
        String expectedMessage = "Plan creado exitosamente";

        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class))).thenReturn(expectedMessage);

        for (String planType : planTypes) {
            NuevoPlanDTO planDTO = new NuevoPlanDTO();
            planDTO.setIdEmpresa(1L);
            planDTO.setNombrePlan(planType);
            planDTO.setCodigoPlan(planType + "-001");
            planDTO.setDescripcion("Plan " + planType);
            planDTO.setPrecioPlan(200.00);
            planDTO.setHorasMensuales(120);
            planDTO.setDiasAplicables("L-M-X-J-V");
            planDTO.setCoberturaHoraria("08:00 - 18:00");
            planDTO.setDescuentoMensual(10.00);
            planDTO.setDescuentoAnualAdicional(3.00);
            planDTO.setFechaVigenciaInicio("2024-01-01");
            planDTO.setFechaVigenciaFin("2024-12-31");
            planDTO.setIdUsuarioCreacion(1L);

            mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(planDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.message", is(expectedMessage)));
        }

        verify(planesSuscripcionService, times(5)).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testEditarPlanSuscripcion_ValidationErrors() throws Exception {
        // Arrange
        NuevoPlanDTO editarPlanDTO = new NuevoPlanDTO();
        editarPlanDTO.setIdTipoPlan(1L);

        when(planesSuscripcionService.editarPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new RuntimeException("Los porcentajes de descuento no cumplen con las reglas de negocio"));

        // Act & Assert
        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editarPlanDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Los porcentajes de descuento no cumplen con las reglas de negocio")));

        verify(planesSuscripcionService).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testCrearNuevoPlan_NullFields() throws Exception {
        // Arrange - DTO with null required fields
        NuevoPlanDTO nullFieldsDTO = new NuevoPlanDTO();
        nullFieldsDTO.setIdEmpresa(null);
        nullFieldsDTO.setNombrePlan(null);

        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class)))
                .thenThrow(new IllegalArgumentException("Campos requeridos no pueden ser nulos"));

        // Act & Assert
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullFieldsDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Campos requeridos no pueden ser nulos")));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
    }

    @Test
    void testMultipleSuccessfulOperations() throws Exception {
        // Test multiple successful operations in sequence
        String createMessage = "Plan creado exitosamente";
        String editMessage = "Plan editado exitosamente";

        when(planesSuscripcionService.crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class))).thenReturn(createMessage);
        when(planesSuscripcionService.editarPlanSuscripcion(any(NuevoPlanDTO.class))).thenReturn(editMessage);

        // Create plan
        mockMvc.perform(post("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockNuevoPlan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(createMessage)));

        // Edit plan
        NuevoPlanDTO editDTO = new NuevoPlanDTO();
        editDTO.setIdTipoPlan(1L);
        editDTO.setCodigoPlan("WW-002");

        mockMvc.perform(put("/api/empresas/planes-suscripcion/planes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is(editMessage)));

        verify(planesSuscripcionService).crearNuevoPlanSuscripcion(any(NuevoPlanDTO.class));
        verify(planesSuscripcionService).editarPlanSuscripcion(any(NuevoPlanDTO.class));
    }
}
