package org.parkcontrol.apiparkcontrol.controllers.suscripcion_cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.services.suscripcion_cliente.SuscripcionClienteService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionClienteControllerUnitTest {

    @Mock
    private SuscripcionClienteService suscripcionClienteService;

    @InjectMocks
    private SuscripcionClienteController suscripcionClienteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Test data
    private PlanesSuscripcionDTO mockPlanesSuscripcion;
    private ClientePlanesSuscripcionDTO mockClientePlanes;
    private List<VehiculoClienteDTO> mockVehiculos;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(suscripcionClienteController).build();
        objectMapper = new ObjectMapper();

        // Setup mock data
        setupMockData();
    }

    private void setupMockData() {
        // Mock empresa suscripciones
        ObtenerSucursalesEmpresaDTO.SucursalDTO sucursalDTO = new ObtenerSucursalesEmpresaDTO.SucursalDTO();
        sucursalDTO.setIdSucursal(1L);
        sucursalDTO.setNombre("Sucursal Centro");
        sucursalDTO.setDireccionCompleta("Avenida Principal 123");
        sucursalDTO.setCiudad("Guatemala");
        sucursalDTO.setDepartamento("Guatemala");
        sucursalDTO.setHoraApertura("08:00");
        sucursalDTO.setHoraCierre("18:00");
        sucursalDTO.setCapacidad2Ruedas(50);
        sucursalDTO.setCapacidad4Ruedas(100);
        sucursalDTO.setEstado("ACTIVA");
        sucursalDTO.setUsuario(null);

        DetalleTipoPlanDTO.ConfiguracionDescuentoDTO configDTO = new DetalleTipoPlanDTO.ConfiguracionDescuentoDTO();
        configDTO.setIdConfiguracionDescuento(1L);
        configDTO.setDescuentoMensual(15.0);
        configDTO.setDescuentoAnualAdicional(5.0);
        configDTO.setFechaVigenciaInicio("2024-01-01T00:00:00");
        configDTO.setFechaVigenciaFin("2024-12-31T23:59:59");
        configDTO.setEstadoConfiguracion("VIGENTE");
        configDTO.setIdUsuarioCreacion(1L);
        configDTO.setFechaCreacionDescuento("2024-01-01T00:00:00");

        DetalleTipoPlanDTO tipoPlanDTO = new DetalleTipoPlanDTO();
        tipoPlanDTO.setId(1L);
        tipoPlanDTO.setIdEmpresa(1L);
        tipoPlanDTO.setNombrePlan("WORKWEEK");
        tipoPlanDTO.setCodigoPlan("WW-001");
        tipoPlanDTO.setDescripcion("Plan Workweek");
        tipoPlanDTO.setPrecioPlan(350.0);
        tipoPlanDTO.setHorasDia(8);
        tipoPlanDTO.setHorasMensuales(160);
        tipoPlanDTO.setDiasAplicables("L-M-X-J-V");
        tipoPlanDTO.setCoberturaHoraria("08:00 - 18:00");
        tipoPlanDTO.setOrdenBeneficio(2);
        tipoPlanDTO.setActivo("VIGENTE");
        tipoPlanDTO.setFechaCreacion("2024-01-01T00:00:00");
        tipoPlanDTO.setConfiguracionDescuento(configDTO);

        PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaDTO = new PlanesSuscripcionDTO.EmpresaSuscripcionesDTO();
        empresaDTO.setIdEmpresa(1L);
        empresaDTO.setNombreComercial("Empresa Test");
        empresaDTO.setNit("1234567-8");
        empresaDTO.setRazonSocial("Empresa Test S.A.");
        empresaDTO.setTelefonoContacto("12345678");
        empresaDTO.setDireccionFiscal("Dirección Fiscal Test");
        empresaDTO.setSucursales(Arrays.asList(sucursalDTO));
        empresaDTO.setSuscripciones(Arrays.asList(tipoPlanDTO));

        mockPlanesSuscripcion = new PlanesSuscripcionDTO();
        mockPlanesSuscripcion.setEmpresasSuscripciones(Arrays.asList(empresaDTO));

        // Mock vehículos - corregir el campo id por el nombre correcto
        VehiculoClienteDTO vehiculoDTO = new VehiculoClienteDTO();
        vehiculoDTO.setIdVehiculo(1L);  // Verificar que este campo existe en el DTO
        vehiculoDTO.setPlaca("ABC123");
        vehiculoDTO.setMarca("Toyota");
        vehiculoDTO.setModelo("Corolla");
        vehiculoDTO.setColor("Blanco");
        vehiculoDTO.setTipoVehiculo("CUATRO_RUEDAS"); // Corregir el enum

        mockVehiculos = Arrays.asList(vehiculoDTO);

        // Mock cliente planes suscripción
        ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcionDTO = new ClientePlanesSuscripcionDTO.SuscripcionClienteDTO();
        suscripcionDTO.setIdSuscripcion(1L);
        suscripcionDTO.setPeriodoContratado("MENSUAL");
        suscripcionDTO.setDescuentoAplicado(0.0);
        suscripcionDTO.setPrecioPlan(350.0);
        suscripcionDTO.setHorasMensualesIncluidas(160);
        suscripcionDTO.setHorasConsumidas(45.5);
        suscripcionDTO.setFechaInicio("2024-01-01");
        suscripcionDTO.setFechaFin("2024-02-01");
        suscripcionDTO.setFechaCompra("2024-01-01");
        suscripcionDTO.setEstadoSuscripcion("ACTIVA");
        suscripcionDTO.setTarifaBaseReferencia(10.0);
        suscripcionDTO.setVehiculoClienteDTO(vehiculoDTO);
        suscripcionDTO.setTipoPlanSuscripcionDTO(tipoPlanDTO);
        suscripcionDTO.setSucursalesDisponibles(Arrays.asList(sucursalDTO));

        mockClientePlanes = new ClientePlanesSuscripcionDTO();
        mockClientePlanes.setIdCliente(1L);
        mockClientePlanes.setNombreCliente("cliente123");
        mockClientePlanes.setSuscripcionCliente(Arrays.asList(suscripcionDTO));
    }

    @Test
    void testGetPlanesDisponiblesParaCliente_Success() throws Exception {
        // Arrange
        when(suscripcionClienteService.obtenerPlanesSuscripcion()).thenReturn(mockPlanesSuscripcion);

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/planes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.empresasSuscripciones", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].idEmpresa", is(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].nombreComercial", is("Empresa Test")))
                .andExpect(jsonPath("$.empresasSuscripciones[0].nit", is("1234567-8")))
                .andExpect(jsonPath("$.empresasSuscripciones[0].sucursales", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].suscripciones", hasSize(1)))
                .andExpect(jsonPath("$.empresasSuscripciones[0].suscripciones[0].nombrePlan", is("WORKWEEK")));

        verify(suscripcionClienteService).obtenerPlanesSuscripcion();
    }

    @Test
    void testGetPlanesDisponiblesParaCliente_ServiceError() throws Exception {
        // Arrange
        when(suscripcionClienteService.obtenerPlanesSuscripcion())
                .thenThrow(new RuntimeException("Error en el servicio"));

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/planes"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener los planes de suscripción disponibles")));

        verify(suscripcionClienteService).obtenerPlanesSuscripcion();
    }

    @Test
    void testGetVehiculosPorCliente_Success() throws Exception {
        // Arrange
        Long idCliente = 1L;
        when(suscripcionClienteService.obtenerVehiculosCliente(idCliente)).thenReturn(mockVehiculos);

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/vehiculos/{idCliente}", idCliente))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idVehiculo", is(1))) // Verificar que este path es correcto
                .andExpect(jsonPath("$[0].placa", is("ABC123")))
                .andExpect(jsonPath("$[0].marca", is("Toyota")))
                .andExpect(jsonPath("$[0].modelo", is("Corolla")))
                .andExpect(jsonPath("$[0].color", is("Blanco")))
                .andExpect(jsonPath("$[0].tipoVehiculo", is("CUATRO_RUEDAS")));

        verify(suscripcionClienteService).obtenerVehiculosCliente(idCliente);
    }

    @Test
    void testGetVehiculosPorCliente_ServiceError() throws Exception {
        // Arrange
        Long idCliente = 1L;
        when(suscripcionClienteService.obtenerVehiculosCliente(idCliente))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/vehiculos/{idCliente}", idCliente))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener los vehículos del cliente")));

        verify(suscripcionClienteService).obtenerVehiculosCliente(idCliente);
    }

    @Test
    void testGetPlanesContratadosPorCliente_Success() throws Exception {
        // Arrange
        Long idCliente = 1L;
        when(suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(idCliente)).thenReturn(mockClientePlanes);

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/planes/{idCliente}", idCliente))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCliente", is(1)))
                .andExpect(jsonPath("$.nombreCliente", is("cliente123")))
                .andExpect(jsonPath("$.suscripcionCliente", hasSize(1)))
                .andExpect(jsonPath("$.suscripcionCliente[0].idSuscripcion", is(1)))
                .andExpect(jsonPath("$.suscripcionCliente[0].periodoContratado", is("MENSUAL")))
                .andExpect(jsonPath("$.suscripcionCliente[0].estadoSuscripcion", is("ACTIVA")))
                .andExpect(jsonPath("$.suscripcionCliente[0].precioPlan", is(350.0)));

        verify(suscripcionClienteService).obtenerPlanesSuscripcionPorCliente(idCliente);
    }

    @Test
    void testGetPlanesContratadosPorCliente_ServiceError() throws Exception {
        // Arrange
        Long idCliente = 999L;
        when(suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(idCliente))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/cliente/suscripciones/planes/{idCliente}", idCliente))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al obtener los planes de suscripción contratados")));

        verify(suscripcionClienteService).obtenerPlanesSuscripcionPorCliente(idCliente);
    }

    @Test
    void testCrearNuevaSuscripcion_Success() throws Exception {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN123456789");

        when(suscripcionClienteService.nuevaSuscripcionCliente(any(NuevaSuscripcionDTO.class)))
                .thenReturn("Nueva suscripción creada con éxito");

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaSuscripcionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Nueva suscripción creada con éxito")));

        verify(suscripcionClienteService).nuevaSuscripcionCliente(any(NuevaSuscripcionDTO.class));
    }

    @Test
    void testCrearNuevaSuscripcion_ServiceError() throws Exception {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(999L); // Vehículo inexistente

        when(suscripcionClienteService.nuevaSuscripcionCliente(any(NuevaSuscripcionDTO.class)))
                .thenThrow(new IllegalArgumentException("Vehículo no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaSuscripcionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al crear la nueva suscripción")));

        verify(suscripcionClienteService).nuevaSuscripcionCliente(any(NuevaSuscripcionDTO.class));
    }

    @Test
    void testRenovarSuscripcion_Success() throws Exception {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(1L);
        renovacionDTO.setNuevoPeriodoContratado("ANUAL");
        renovacionDTO.setMetodoPago("TRANSFERENCIA_BANCARIA");
        renovacionDTO.setNumeroTransaccion("TXN987654321");

        when(suscripcionClienteService.renovarSuscripcionCliente(any(RenovacionSuscripcionDTO.class)))
                .thenReturn("Suscripción renovada con éxito");

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/renovar-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renovacionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Suscripción renovada con éxito")));

        verify(suscripcionClienteService).renovarSuscripcionCliente(any(RenovacionSuscripcionDTO.class));
    }

    @Test
    void testRenovarSuscripcion_ServiceError() throws Exception {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(999L); // Suscripción inexistente

        when(suscripcionClienteService.renovarSuscripcionCliente(any(RenovacionSuscripcionDTO.class)))
                .thenThrow(new IllegalArgumentException("Suscripción no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/renovar-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renovacionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al renovar la suscripción")));

        verify(suscripcionClienteService).renovarSuscripcionCliente(any(RenovacionSuscripcionDTO.class));
    }

    @Test
    void testCrearNuevaSuscripcion_RuntimeException() throws Exception {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);

        when(suscripcionClienteService.nuevaSuscripcionCliente(any(NuevaSuscripcionDTO.class)))
                .thenThrow(new RuntimeException("Error interno del sistema"));

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/nueva-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaSuscripcionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al crear la nueva suscripción: Error interno del sistema")));
    }

    @Test
    void testRenovarSuscripcion_RuntimeException() throws Exception {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(1L);

        when(suscripcionClienteService.renovarSuscripcionCliente(any(RenovacionSuscripcionDTO.class)))
                .thenThrow(new RuntimeException("Error interno del sistema"));

        // Act & Assert
        mockMvc.perform(post("/api/cliente/suscripciones/renovar-suscripcion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renovacionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Error al renovar la suscripción: Error interno del sistema")));
    }
}
