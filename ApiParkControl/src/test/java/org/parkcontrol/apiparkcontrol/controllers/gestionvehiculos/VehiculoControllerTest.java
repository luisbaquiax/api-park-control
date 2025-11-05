package org.parkcontrol.apiparkcontrol.controllers.gestionvehiculos;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.parkcontrol.apiparkcontrol.controllers.vehiculoController.VehiculoController;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculoRequestDTO;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehiculosPropietarioDTO;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.services.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculoController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehiculoService vehiculoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateVehiculo() throws Exception {
        // Arrange
        String dpi = "1234567890101";
        VehiculoRequestDTO request = new VehiculoRequestDTO();
        request.setPlaca("P123ABC");
        request.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        request.setMarca("Toyota");
        request.setModelo("Yaris");
        request.setAnio(2022);
        request.setColor("Rojo");
        request.setEstadoVehiculo(Vehiculo.EstadoVehiculo.ACTIVO);

        MessageSuccess mockResponse = new MessageSuccess(201, "Vehículo P123ABC creado con éxito");

        Mockito.when(vehiculoService.create(eq(dpi), any(VehiculoRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/gestion-vehiculos/create/{dpi}", dpi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Spring por defecto responde 200 aunque el mensaje tenga 201
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Vehículo P123ABC creado con éxito"));

        Mockito.verify(vehiculoService, Mockito.times(1))
                .create(eq(dpi), any(VehiculoRequestDTO.class));
    }

    //Test para cambiar estado del vehículo
    @Test
    void testChangeStatus() throws Exception {
        MessageSuccess response = new MessageSuccess(200, "Estado del vehículo actualizado a INACTIVO");

        when(vehiculoService.changeStatus("INACTIVO", 1L)).thenReturn(response);

        mockMvc.perform(put("/api/gestion-vehiculos/change-status/{status}/{idVehiculo}", "INACTIVO", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Estado del vehículo actualizado a INACTIVO"));
    }

    //Test para obtener vehículos por cliente
    @Test
    void testGetByIdPersona() throws Exception {
        VehicleResponsDTO v1 = new VehicleResponsDTO();
        v1.setId(1L);
        v1.setIdPersona(10L);
        v1.setPlaca("P123ABC");
        v1.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        v1.setMarca("Toyota");
        v1.setModelo("Corolla");
        v1.setAnio(2020);
        v1.setColor("Negro");
        v1.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);

        VehicleResponsDTO v2 = new VehicleResponsDTO();
        v2.setId(2L);
        v2.setIdPersona(10L);
        v2.setPlaca("P987XYZ");
        v2.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        v2.setMarca("Honda");
        v2.setModelo("Civic");
        v2.setAnio(2021);
        v2.setColor("Gris");
        v2.setEstado(Vehiculo.EstadoVehiculo.INACTIVO);

        VehiculosPropietarioDTO propietario = new VehiculosPropietarioDTO();
        propietario.setVehiculos(List.of(v1, v2));

        when(vehiculoService.getAllByPersona()).thenReturn(List.of(propietario));

        mockMvc.perform(get("/api/gestion-vehiculos/get-by-client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehiculos[0].placa").value("P123ABC"))
                .andExpect(jsonPath("$[0].vehiculos[1].estado").value("INACTIVO"));
    }

    // 🧩 Test para obtener todos los vehículos
    @Test
    void testGetAllVehiculos() throws Exception {
        VehicleResponsDTO v1 = new VehicleResponsDTO();
        v1.setId(1L);
        v1.setIdPersona(5L);
        v1.setPlaca("P111AAA");
        v1.setTipoVehiculo(Vehiculo.TipoVehiculo.DOS_RUEDAS);
        v1.setMarca("Yamaha");
        v1.setModelo("R15");
        v1.setAnio(2019);
        v1.setColor("Azul");
        v1.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);

        VehicleResponsDTO v2 = new VehicleResponsDTO();
        v2.setId(2L);
        v2.setIdPersona(6L);
        v2.setPlaca("P222BBB");
        v2.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        v2.setMarca("Mazda");
        v2.setModelo("CX5");
        v2.setAnio(2022);
        v2.setColor("Rojo");
        v2.setEstado(Vehiculo.EstadoVehiculo.VENDIDO);

        when(vehiculoService.getAll()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/gestion-vehiculos/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value("P111AAA"))
                .andExpect(jsonPath("$[1].estado").value("VENDIDO"));
    }
}
