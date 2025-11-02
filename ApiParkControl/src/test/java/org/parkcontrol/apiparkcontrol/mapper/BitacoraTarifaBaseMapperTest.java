package org.parkcontrol.apiparkcontrol.mapper;

import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.empresa.BitacoraTarifaBaseResponse;
import org.parkcontrol.apiparkcontrol.models.BitacoraTarifaBase;
import org.parkcontrol.apiparkcontrol.models.TarifaBase;
import org.parkcontrol.apiparkcontrol.models.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitacoraTarifaBaseMapperTest {

    private BitacoraTarifaBaseMapper mapper = new BitacoraTarifaBaseMapperImpl();

    private final Long ID_BITACORA = 100L;
    private final Long ID_TARIFA_BASE = 5L;
    private final Long ID_USUARIO_RESPONSABLE = 2L;
    private final BigDecimal PRECIO_ANTERIOR = new BigDecimal("10.00");
    private final BigDecimal PRECIO_NUEVO = new BigDecimal("12.50");
    private final LocalDateTime FECHA_CAMBIO = LocalDateTime.of(2025, 10, 31, 15, 0);
    private final BitacoraTarifaBase.Accion ACCION = BitacoraTarifaBase.Accion.ACTUALIZACION;
    private final String OBSERVACIONES = "Cambio por ajuste trimestral.";


    private BitacoraTarifaBase createMockEntity() {
        BitacoraTarifaBase entity = new BitacoraTarifaBase();
        entity.setIdBitacoraTarifa(ID_BITACORA);
        entity.setAccion(ACCION);
        entity.setPrecioAnterior(PRECIO_ANTERIOR);
        entity.setPrecioNuevo(PRECIO_NUEVO);
        entity.setFechaCambio(FECHA_CAMBIO);
        entity.setObservaciones(OBSERVACIONES);

        // Mocking para la relación TarifaBase
        TarifaBase mockTarifaBase = mock(TarifaBase.class);
        when(mockTarifaBase.getIdTarifaBase()).thenReturn(ID_TARIFA_BASE);
        entity.setTarifaBase(mockTarifaBase);

        // Mocking para la relación Usuario
        Usuario mockUsuario = mock(Usuario.class);
        when(mockUsuario.getIdUsuario()).thenReturn(ID_USUARIO_RESPONSABLE);
        entity.setUsuarioResponsable(mockUsuario);

        return entity;
    }

    /**
     * Prueba 1: Mapeo exitoso de campos directos y anidados (el caso feliz)
     */
    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE
        BitacoraTarifaBase source = createMockEntity();

        // ACT
        BitacoraTarifaBaseResponse target = mapper.map(source);

        // ASSERT: Verificar todos los mapeos, incluyendo los anidados
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Campos Directos
        assertEquals(ID_BITACORA, target.getIdBitacoraTarifa(), "El ID de bitácora no coincide.");
        assertEquals(ACCION, target.getAccion(), "La acción no coincide.");
        assertEquals(FECHA_CAMBIO, target.getFechaCambio(), "La fecha de cambio no coincide.");
        assertEquals(OBSERVACIONES, target.getObservaciones(), "Las observaciones no coinciden.");

        // Campos BigDecimal (usamos compareTo para una comparación precisa)
        assertEquals(0, PRECIO_ANTERIOR.compareTo(target.getPrecioAnterior()), "El precio anterior no coincide.");
        assertEquals(0, PRECIO_NUEVO.compareTo(target.getPrecioNuevo()), "El precio nuevo no coincide.");

        // Campos Anidados (¡Los más importantes!)
        assertEquals(ID_TARIFA_BASE, target.getIdTarifaBase(),
                "El ID de TarifaBase mapeado (anidado) no coincide.");
        assertEquals(ID_USUARIO_RESPONSABLE, target.getIdUsuarioResponsable(),
                "El ID de Usuario Responsable mapeado (anidado) no coincide.");
    }

    /**
     * Prueba 2: Manejo de relaciones nulas (TarifaBase nula)
     */
    @Test
    void map_shouldMapNullIdTarifaBase_whenTarifaBaseIsNull() {
        // ARRANGE
        BitacoraTarifaBase source = createMockEntity();
        source.setTarifaBase(null); // Eliminamos la relación

        // ACT
        BitacoraTarifaBaseResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");
        assertNull(target.getIdTarifaBase(), "idTarifaBase debe ser nulo si la relación es nula.");

        // Verificamos que el otro mapeo anidado siga funcionando
        assertEquals(ID_USUARIO_RESPONSABLE, target.getIdUsuarioResponsable());
    }

    /**
     * Prueba 3: Manejo de relaciones nulas (Usuario nulo)
     */
    @Test
    void map_shouldMapNullIdUsuarioResponsable_whenUsuarioResponsableIsNull() {
        // ARRANGE
        BitacoraTarifaBase source = createMockEntity();
        source.setUsuarioResponsable(null); // Eliminamos la relación

        // ACT
        BitacoraTarifaBaseResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");
        assertNull(target.getIdUsuarioResponsable(),
                "idUsuarioResponsable debe ser nulo si la relación es nula.");

        // Verificamos que el otro mapeo anidado siga funcionando
        assertEquals(ID_TARIFA_BASE, target.getIdTarifaBase());
    }

    /**
     * Prueba 4: Manejo de objeto de entrada nulo
     */
    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        BitacoraTarifaBaseResponse target = mapper.map(null);
        // ASSERT
        assertNull(target, "Mapear un objeto nulo debe devolver nulo.");
    }
}