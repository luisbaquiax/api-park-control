package org.parkcontrol.apiparkcontrol.mapper;


import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.vehiculo.VehicleResponsDTO;
import org.parkcontrol.apiparkcontrol.models.Vehiculo;
import org.parkcontrol.apiparkcontrol.models.Persona; // Necesario para el mock del propietario

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria para el VehicleMap.
 * Verifica el mapeo directo y el mapeo anidado de la relación Propietario (Persona).
 */
public class VehicleMapTest {

    private VehicleMap mapper = new VehicleMapImpl();

    private final Long ID_VEHICULO = 10L;
    private final Long ID_PERSONA_PROPIETARIO = 101L; // El campo anidado
    private final String PLACA = "P987XYZ";
    private final Vehiculo.TipoVehiculo TIPO = Vehiculo.TipoVehiculo.CUATRO_RUEDAS;
    private final String MARCA = "Toyota";
    private final String MODELO = "Corolla";
    private final Integer ANIO = 2020;
    private final String COLOR = "Gris";
    private final Vehiculo.EstadoVehiculo ESTADO = Vehiculo.EstadoVehiculo.ACTIVO;

    /** Crea una entidad Vehiculo completamente poblada con Mock para la relación Propietario (Persona). */
    private Vehiculo createMockEntity() {
        Vehiculo entity = new Vehiculo();
        entity.setId(ID_VEHICULO);
        entity.setPlaca(PLACA);
        entity.setTipoVehiculo(TIPO);
        entity.setMarca(MARCA);
        entity.setModelo(MODELO);
        entity.setAnio(ANIO);
        entity.setColor(COLOR);
        entity.setEstado(ESTADO);

        // Mocking para la relación Propietario (Persona)
        Persona mockPersona = mock(Persona.class);
        // Configuramos el valor que se debe extraer en el mapeo anidado
        when(mockPersona.getIdPersona()).thenReturn(ID_PERSONA_PROPIETARIO);
        entity.setPropietario(mockPersona);

        return entity;
    }

    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE
        Vehiculo source = createMockEntity();

        // ACT
        VehicleResponsDTO target = mapper.map(source);

        // ASSERT: Verificar el mapeo
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Campos Directos
        assertEquals(ID_VEHICULO, target.getId(), "El ID del vehículo no coincide.");
        assertEquals(PLACA, target.getPlaca(), "La placa no coincide.");
        assertEquals(TIPO, target.getTipoVehiculo(), "El tipo de vehículo no coincide.");
        assertEquals(MARCA, target.getMarca(), "La marca no coincide.");
        assertEquals(MODELO, target.getModelo(), "El modelo no coincide.");
        assertEquals(ANIO, target.getAnio(), "El año no coincide.");
        assertEquals(COLOR, target.getColor(), "El color no coincide.");
        assertEquals(ESTADO, target.getEstado(), "El estado no coincide.");

        // Campo Anidado (@Mapping(source = "propietario.idPersona", target = "idPersona"))
        assertEquals(ID_PERSONA_PROPIETARIO, target.getIdPersona(),
                "El ID de Persona del propietario mapeado no coincide.");
    }

    @Test
    void map_shouldMapNullIdPersona_whenPropietarioIsNull() {
        // ARRANGE
        Vehiculo source = createMockEntity();
        source.setPropietario(null); // Establece el objeto Propietario (Persona) a null

        // ACT
        VehicleResponsDTO target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");
        assertNull(target.getIdPersona(),
                "idPersona debe ser nulo si la relación propietario es nula.");

        // Verificamos que los otros campos sigan mapeando
        assertEquals(ID_VEHICULO, target.getId());
        assertEquals(PLACA, target.getPlaca());
    }

    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        VehicleResponsDTO target = mapper.map(null);

        // ASSERT
        assertNull(target, "Mapear un objeto nulo debe devolver nulo.");
    }
}