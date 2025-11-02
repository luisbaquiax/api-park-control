package org.parkcontrol.apiparkcontrol.mapper;

import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.bitacoraSistema.BitacoraSistemaResponse;
import org.parkcontrol.apiparkcontrol.models.BitacoraSistema;
import org.parkcontrol.apiparkcontrol.models.Usuario; // Necesitas esta clase para el mock
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitacoraSistemaMapTest {

    private BitacoraSistemaMap bitacoraSistemaMap = new BitacoraSistemaMapImpl();

    private final Long ID_BITACORA = 50L;
    private final String MODULO = "GESTION_EMPRESA";
    private final String ACCION = "CREATE";
    private final Long ID_USUARIO = 1L;
    private final String IP_ORIGEN = "192.168.1.10";
    private final LocalDateTime FECHA_ACCION = LocalDateTime.of(2025, 11, 1, 10, 30);


    private BitacoraSistema createMockBitacoraSistema() {
        BitacoraSistema bitacora = new BitacoraSistema();
        bitacora.setIdBitacora(ID_BITACORA);
        bitacora.setModulo(MODULO);
        bitacora.setAccion(ACCION);
        bitacora.setTablaAfectada("TARIFA_BASE");
        bitacora.setIdRegistroAfectado(10L);
        bitacora.setIpOrigen(IP_ORIGEN);
        bitacora.setDescripcion("Creación de nueva tarifa base");
        bitacora.setDatosAntes("{}");
        bitacora.setDatosDespues("{'precio': 15.00}");
        bitacora.setFechaAccion(FECHA_ACCION);

        Usuario mockUsuario = mock(Usuario.class);
        when(mockUsuario.getIdUsuario()).thenReturn(ID_USUARIO);
        bitacora.setUsuario(mockUsuario);

        return bitacora;
    }

    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE: Preparar la entidad
        BitacoraSistema source = createMockBitacoraSistema();

        // ACT: Ejecutar el mapeo
        BitacoraSistemaResponse target = bitacoraSistemaMap.map(source);

        // ASSERT: Verificar los campos
        assertNotNull(target, "El objeto de respuesta no debe ser nulo.");

        // Campos Directos
        assertEquals(ID_BITACORA, target.getIdBitacora(), "El ID de bitácora no coincide.");
        assertEquals(MODULO, target.getModulo(), "El módulo no coincide.");
        assertEquals(ACCION, target.getAccion(), "La acción no coincide.");
        assertEquals(FECHA_ACCION, target.getFechaAccion(), "La fecha no coincide.");
        assertEquals(IP_ORIGEN, target.getIpOrigen(), "La IP de origen no coincide.");

        // Campo Anidado (@Mapping(source = "usuario.idUsuario", target = "idUsuario"))
        assertEquals(ID_USUARIO, target.getIdUsuario(), "El ID de Usuario anidado no coincide.");
    }

    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        BitacoraSistemaResponse target = bitacoraSistemaMap.map(null);
        // ASSERT
        assertEquals(null, target, "Mapear un objeto nulo debe devolver nulo.");
    }

    @Test
    void map_shouldMapNullIdUsuario_whenUsuarioIsNull() {
        BitacoraSistema source = createMockBitacoraSistema();
        source.setUsuario(null);

        BitacoraSistemaResponse target = bitacoraSistemaMap.map(source);

        assertNotNull(target, "El objeto de respuesta no debe ser nulo.");
        assertEquals(null, target.getIdUsuario(), "El idUsuario debe ser nulo si el objeto Usuario es nulo.");
    }
}