package org.parkcontrol.apiparkcontrol.mapper;

import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.empresa.TarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.parkcontrol.apiparkcontrol.models.Empresa; // Necesario para el mock de la relación

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria para el TarifaBaseMap.
 */
public class TarifaBaseMapTest {

    // Instanciamos la implementación generada por MapStruct.
    private TarifaBaseMap mapper = new TarifaBaseMapImpl();

    // --- Constantes de Prueba ---
    private final Long ID_TARIFA_BASE = 10L;
    private final Long ID_EMPRESA = 1L; // El campo anidado
    private final BigDecimal PRECIO_POR_HORA = new BigDecimal("15.75");
    private final String MONEDA = "GTQ";
    private final LocalDate FECHA_INICIO = LocalDate.of(2025, 11, 1);
    private final LocalDate FECHA_FIN = LocalDate.of(2025, 12, 31);
    private final TarifaBase.EstadoTarifaBase ESTADO = TarifaBase.EstadoTarifaBase.VIGENTE;


    /** Crea una entidad TarifaBase completamente poblada con Mock para la relación Empresa. */
    private TarifaBase createMockEntity() {
        TarifaBase entity = new TarifaBase();
        entity.setIdTarifaBase(ID_TARIFA_BASE);
        entity.setPrecioPorHora(PRECIO_POR_HORA);
        entity.setMoneda(MONEDA);
        entity.setFechaVigenciaInicio(FECHA_INICIO);
        entity.setFechaVigenciaFin(FECHA_FIN);
        entity.setEstado(ESTADO);
        // La fecha de creación no se mapea, no es crucial para el test del mapper.

        // Mocking para la relación Empresa
        Empresa mockEmpresa = mock(Empresa.class);
        // Configuramos el valor que se debe extraer en el mapeo anidado
        when(mockEmpresa.getIdEmpresa()).thenReturn(ID_EMPRESA);
        entity.setEmpresa(mockEmpresa);

        return entity;
    }

    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE
        TarifaBase source = createMockEntity();

        // ACT
        TarifaBaseResponse target = mapper.map(source);

        // ASSERT: Verificar el mapeo
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Campos Directos
        assertEquals(ID_TARIFA_BASE, target.getIdTarifaBase(), "El ID de la tarifa no coincide.");
        assertEquals(0, PRECIO_POR_HORA.compareTo(target.getPrecioPorHora()), "El precio por hora no coincide.");
        assertEquals(MONEDA, target.getMoneda(), "La moneda no coincide.");
        assertEquals(FECHA_INICIO, target.getFechaVigenciaInicio(), "La fecha de inicio no coincide.");
        assertEquals(FECHA_FIN, target.getFechaVigenciaFin(), "La fecha de fin no coincide.");
        assertEquals(ESTADO, target.getEstado(), "El estado no coincide.");

        // Campo Anidado (@Mapping(source = "empresa.idEmpresa", target = "idEmpresa"))
        assertEquals(ID_EMPRESA, target.getIdEmpresa(),
                "El ID de Empresa mapeado (anidado) no coincide.");
    }

    @Test
    void map_shouldMapNullIdEmpresa_whenEmpresaIsNull() {
        // ARRANGE
        TarifaBase source = createMockEntity();
        source.setEmpresa(null); // Establece el objeto Empresa a null

        // ACT
        TarifaBaseResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");
        assertNull(target.getIdEmpresa(),
                "idEmpresa debe ser nulo si la relación es nula.");

        // Verificamos que los otros campos sigan mapeando
        assertEquals(ID_TARIFA_BASE, target.getIdTarifaBase());
        assertEquals(ESTADO, target.getEstado());
    }

    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        TarifaBaseResponse target = mapper.map(null);

        // ASSERT
        assertNull(target, "Mapear un objeto nulo debe devolver nulo.");
    }
}