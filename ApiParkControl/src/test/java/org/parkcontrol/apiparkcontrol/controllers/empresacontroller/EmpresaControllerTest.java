package org.parkcontrol.apiparkcontrol.controllers.empresacontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.parkcontrol.apiparkcontrol.controllers.empresa.EmpresaController;
import org.parkcontrol.apiparkcontrol.dto.empresa.EmpresaResponse;
import org.parkcontrol.apiparkcontrol.dto.empresa.RegisterEmpresa;
import org.parkcontrol.apiparkcontrol.mapper.EmpresaMap;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.services.EmpresaService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmpresaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private EmpresaMap mapper;

    @InjectMocks
    private EmpresaController empresaController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(empresaController).build();
    }

    @Test
    void testCreateEmpresa() throws Exception {
        RegisterEmpresa register = new RegisterEmpresa();
        register.setIdUsuario(1L);
        register.setNombreComercial("Comercial S.A.");
        register.setRazonSocial("Comercial Sociedad Anónima");
        register.setNit("123456789");
        register.setDireccionFiscal("Calle 1");
        register.setTelefonoPrincipal("5555-5555");
        register.setCorreoPrincipal("correo@empresa.com");
        register.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        empresa.setNit("123456789");

        EmpresaResponse response = new EmpresaResponse();
        response.setIdEmpresa(1L);
        response.setNit("123456789");

        when(empresaService.create(any(RegisterEmpresa.class))).thenReturn(empresa);
        when(mapper.map(any(Empresa.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/companies-managment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmpresa").value(1))
                .andExpect(jsonPath("$.nit").value("123456789"));

        verify(empresaService, times(1)).create(any(RegisterEmpresa.class));
        verify(mapper, times(1)).map(any(Empresa.class));
    }

    @Test
    void testUpdateEmpresa() throws Exception {
        Long idEmpresa = 1L;

        RegisterEmpresa register = new RegisterEmpresa();
        register.setNombreComercial("Nueva Comercial");
        register.setRazonSocial("Nueva Razón");
        register.setDireccionFiscal("Nueva Calle");
        register.setTelefonoPrincipal("1111-1111");
        register.setCorreoPrincipal("nuevo@correo.com");
        register.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(idEmpresa);

        EmpresaResponse response = new EmpresaResponse();
        response.setIdEmpresa(idEmpresa);

        when(empresaService.update(any(RegisterEmpresa.class), eq(idEmpresa))).thenReturn(empresa);
        when(mapper.map(any(Empresa.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/companies-managment/update/{id}", idEmpresa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmpresa").value(idEmpresa));

        verify(empresaService, times(1)).update(any(RegisterEmpresa.class), eq(idEmpresa));
        verify(mapper, times(1)).map(any(Empresa.class));
    }

    @Test
    void testGetAll() throws Exception {
        Empresa empresa1 = new Empresa();
        empresa1.setIdEmpresa(1L);
        Empresa empresa2 = new Empresa();
        empresa2.setIdEmpresa(2L);

        EmpresaResponse response1 = new EmpresaResponse();
        response1.setIdEmpresa(1L);
        EmpresaResponse response2 = new EmpresaResponse();
        response2.setIdEmpresa(2L);

        when(empresaService.getAll()).thenReturn(List.of(empresa1, empresa2));
        when(mapper.map(empresa1)).thenReturn(response1);
        when(mapper.map(empresa2)).thenReturn(response2);

        mockMvc.perform(get("/api/admin/companies-managment/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].idEmpresa").value(1))
                .andExpect(jsonPath("$[1].idEmpresa").value(2));

        verify(empresaService, times(1)).getAll();
        verify(mapper, times(1)).map(empresa1);
        verify(mapper, times(1)).map(empresa2);
    }

    @Test
    void testGetCompaniesByUser() throws Exception {
        Long idUser = 1L;

        Empresa empresa1 = new Empresa();
        empresa1.setIdEmpresa(1L);

        EmpresaResponse response1 = new EmpresaResponse();
        response1.setIdEmpresa(1L);

        when(empresaService.getComapniesByUser(idUser)).thenReturn(List.of(empresa1));
        when(mapper.map(empresa1)).thenReturn(response1);

        mockMvc.perform(get("/api/admin/companies-managment/get-by-user-company/{idUser}", idUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].idEmpresa").value(1));

        verify(empresaService, times(1)).getComapniesByUser(idUser);
        verify(mapper, times(1)).map(empresa1);
    }
}
