package org.parkcontrol.apiparkcontrol.controllers.empresacontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.controllers.empresa.GestionTarifaController;
import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.mapper.TarifaBaseMap;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.parkcontrol.apiparkcontrol.services.TarifaBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.parkcontrol.apiparkcontrol.dto.messagesuccess.MessageSuccess;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionTarifaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GestionTarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TarifaBaseService tarifaBaseService;

    @MockitoBean
    private TarifaBaseMap tarifaBaseMap;

    private final String BASE_URL = "/api/empresa/tarifa";
    private final Long ID_USUARIO = 1L;
    private final Long ID_TARIFA = 10L;

    private TarifaBaseResponse getTarifaBaseResponse() {
        TarifaBaseResponse response = new TarifaBaseResponse();
        response.setIdTarifaBase(ID_TARIFA);
        response.setIdEmpresa(ID_USUARIO);
        response.setPrecioPorHora(new BigDecimal("15.50"));
        response.setMoneda("GTQ");
        response.setFechaVigenciaInicio(LocalDate.now());
        response.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);
        return response;
    }

    private TarifaBase getTarifaBase() {
        return new TarifaBase();
    }

    private MessageSuccess getMessageSuccess(String message) {
        MessageSuccess success = new MessageSuccess();
        success.setCode(200);
        success.setMessage(message);
        return success;
    }

    @Test
    void create_shouldReturnCreatedTarifaBase() throws Exception {
        TarifaBaseResponse requestDto = getTarifaBaseResponse();
        TarifaBaseResponse responseDto = getTarifaBaseResponse();

        TarifaBase mockEntity = getTarifaBase();

        when(tarifaBaseService.create(any(TarifaBaseResponse.class), any(Long.class)))
                .thenReturn(mockEntity);

        when(tarifaBaseMap.map(mockEntity))
                .thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL + "/create/{idUsuario}", ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTarifaBase").value(ID_TARIFA))
                .andExpect(jsonPath("$.precioPorHora").value(responseDto.getPrecioPorHora().doubleValue()));
    }

    @Test
    void update_shouldReturnUpdatedTarifaBase() throws Exception {
        TarifaBaseResponse requestDto = getTarifaBaseResponse();
        TarifaBaseResponse responseDto = getTarifaBaseResponse();
        responseDto.setPrecioPorHora(new BigDecimal("20.00"));

        TarifaBase mockEntity = getTarifaBase();

        when(tarifaBaseService.update(any(TarifaBaseResponse.class), any(Long.class)))
                .thenReturn(mockEntity);

        when(tarifaBaseMap.map(mockEntity))
                .thenReturn(responseDto);

        mockMvc.perform(put(BASE_URL + "/update/{idUsuario}", ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                // Corregido: Usamos .doubleValue() para la aserción numérica.
                .andExpect(jsonPath("$.precioPorHora").value(responseDto.getPrecioPorHora().doubleValue()));
    }

    @Test
    void getByStatus_shouldReturnTarifaBase() throws Exception {
        TarifaBase.EstadoTarifaBase estado = TarifaBase.EstadoTarifaBase.VIGENTE;
        TarifaBaseResponse responseDto = getTarifaBaseResponse();

        when(tarifaBaseService.findTarifaBaseByEmpresaIdByEstado(any(TarifaBase.EstadoTarifaBase.class), any(Long.class)))
                .thenReturn(getTarifaBase());
        when(tarifaBaseMap.map(any(TarifaBase.class)))
                .thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/get-by-status/{estado}/{idUsuario}", estado, ID_USUARIO))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estado.toString()))
                .andExpect(jsonPath("$.idTarifaBase").value(ID_TARIFA));
    }

   /* @Test
    void getByEmpresa_shouldReturnListOfTarifas() throws Exception {
        TarifaBase t1 = getTarifaBase();
        TarifaBase t2 = getTarifaBase();
        List<TarifaBase> serviceResult = List.of(t1, t2);

        TarifaBaseResponse r1 = getTarifaBaseResponse();
        TarifaBaseResponse r2 = getTarifaBaseResponse();
        r2.setIdTarifaBase(11L);

        when(tarifaBaseService.findAllByEmpresa(any(Long.class)))
                .thenReturn(serviceResult);
        when(tarifaBaseMap.map(t1)).thenReturn(r1);
        when(tarifaBaseMap.map(t2)).thenReturn(r2);


        mockMvc.perform(get(BASE_URL + "/get-by-empresa/{idUsuario}", ID_USUARIO))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idTarifaBase").value(r1.getIdTarifaBase()))
                .andExpect(jsonPath("$[1].idTarifaBase").value(r2.getIdTarifaBase()));
    }
*/
    @Test
    void activar_shouldReturnSuccessMessage() throws Exception {
        MessageSuccess success = getMessageSuccess("Tarifa activada exitosamente");
        when(tarifaBaseService.activarTarifaBase(any(Long.class), any(Long.class)))
                .thenReturn(success);

        mockMvc.perform(put(BASE_URL + "/activar/{idTarifa}/{idUsuario}", ID_TARIFA, ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tarifa activada exitosamente"));
    }

    @Test
    void desactivar_shouldReturnSuccessMessage() throws Exception {
        MessageSuccess success = getMessageSuccess("Tarifa desactivada exitosamente");
        when(tarifaBaseService.desactivarTarifaBase(any(Long.class), any(Long.class)))
                .thenReturn(success);

        mockMvc.perform(put(BASE_URL + "/desactivar/{idTarifa}/{idUsuario}", ID_TARIFA, ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tarifa desactivada exitosamente"));
    }
}