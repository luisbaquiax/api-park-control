package org.parkcontrol.apiparkcontrol.services.bitacoratarifabase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.repositories.BitacoraTarifaBaseRepository;
import org.parkcontrol.apiparkcontrol.repositories.EmpresaRepository;
import org.parkcontrol.apiparkcontrol.services.BitacoraTarifaBaseService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BitacoraTarifaBaseServiceTest {

    @Mock
    private BitacoraTarifaBaseRepository bitacoraTarifaBaseRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private BitacoraTarifaBaseService service;

    public BitacoraTarifaBaseServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetByEmpresa_Success() {
        Long idUsuario = 1L;

        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(100L);

        BitacoraTarifaBase bit1 = new BitacoraTarifaBase();
        BitacoraTarifaBase bit2 = new BitacoraTarifaBase();

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario)).thenReturn(List.of(empresa));
        when(bitacoraTarifaBaseRepository.findByTarifaBase_Empresa_IdEmpresa(100L)).thenReturn(List.of(bit1, bit2));

        List<BitacoraTarifaBase> result = service.getByEmpresa(idUsuario);

        assertEquals(2, result.size());
        verify(empresaRepository, times(1)).findByUsuarioEmpresa_IdUsuario(idUsuario);
        verify(bitacoraTarifaBaseRepository, times(1)).findByTarifaBase_Empresa_IdEmpresa(100L);
    }

    @Test
    void testGetByEmpresa_NoEmpresa() {
        Long idUsuario = 2L;

        when(empresaRepository.findByUsuarioEmpresa_IdUsuario(idUsuario)).thenReturn(List.of());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> service.getByEmpresa(idUsuario));
        assertEquals(403, exception.getStatus());
        assertEquals("El usuario no tiene una empresa asociada", exception.getMessage());
    }
}

