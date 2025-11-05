package org.parkcontrol.apiparkcontrol.mapper;


import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.empresa.UsuarioPersonaRolResponse;
import org.parkcontrol.apiparkcontrol.models.Usuario;
import org.parkcontrol.apiparkcontrol.models.Persona; // Necesario para la relación
import org.parkcontrol.apiparkcontrol.models.Rol;     // Necesario para la relación

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria para UsuarioPersonaRolMap.
 * Se prueba el mapeo de tres niveles: Usuario -> Persona, Usuario -> Rol.
 */
public class UsuarioPersonaRolMapTest {

    // Instanciamos la implementación generada por MapStruct.
    private UsuarioPersonaRolMap mapper = new UsuarioPersonaRolMapImpl();

    // --- Constantes de Prueba ---
    private final Long ID_USUARIO = 1L;
    private final String NOMBRE_USUARIO = "admin.parking";
    private final Long ID_PERSONA = 50L;
    private final String NOMBRE_ROL = "ADMIN";
    private final String NOMBRE_PERSONA = "Luis";
    private final String APELLIDO_PERSONA = "Perez";
    private final String DPI = "1234567890123";


    /** Crea una entidad Usuario completamente poblada con Mocks para Persona y Rol. */
    private Usuario createMockEntity() {
        Usuario entity = new Usuario();
        entity.setIdUsuario(ID_USUARIO);
        entity.setNombreUsuario(NOMBRE_USUARIO);
        // Los campos booleanos no se mapean, no son cruciales aquí, pero se pueden setear.

        // 1. Mocking para la relación Rol
        Rol mockRol = mock(Rol.class);
        when(mockRol.getNombreRol()).thenReturn(NOMBRE_ROL); // Mapeo de 2do nivel
        entity.setRol(mockRol);

        // 2. Mocking para la relación Persona
        Persona mockPersona = mock(Persona.class);
        when(mockPersona.getIdPersona()).thenReturn(ID_PERSONA);
        when(mockPersona.getNombre()).thenReturn(NOMBRE_PERSONA);
        when(mockPersona.getApellido()).thenReturn(APELLIDO_PERSONA);
        when(mockPersona.getDpi()).thenReturn(DPI);
        entity.setPersona(mockPersona);

        return entity;
    }

    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE
        Usuario source = createMockEntity();

        // ACT
        UsuarioPersonaRolResponse target = mapper.map(source);

        // ASSERT: Verificar el mapeo
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Campos de Usuario (directo)
        assertEquals(ID_USUARIO, target.getIdUsuario(), "El ID de usuario no coincide.");
        assertEquals(NOMBRE_USUARIO, target.getNombreUsuario(), "El nombre de usuario no coincide.");

        // Campos del Rol (Anidado: Usuario -> Rol)
        assertEquals(NOMBRE_ROL, target.getNombreRol(), "El nombre del rol no coincide.");

        // Campos de Persona (Anidado: Usuario -> Persona)
        assertEquals(ID_PERSONA, target.getIdPersona(), "El ID de persona no coincide.");
        assertEquals(NOMBRE_PERSONA, target.getNombre(), "El nombre de la persona no coincide.");
        assertEquals(APELLIDO_PERSONA, target.getApellido(), "El apellido de la persona no coincide.");
        assertEquals(DPI, target.getDpi(), "El DPI no coincide.");
    }

    @Test
    void map_shouldMapPersonaFieldsAsNull_whenPersonaIsNull() {
        // ARRANGE
        Usuario source = createMockEntity();
        source.setPersona(null); // Eliminamos la relación Persona

        // ACT
        UsuarioPersonaRolResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Los campos de Persona deben ser nulos
        assertNull(target.getIdPersona(), "idPersona debe ser nulo.");
        assertNull(target.getNombre(), "nombre debe ser nulo.");
        assertNull(target.getApellido(), "apellido debe ser nulo.");
        assertNull(target.getDpi(), "DPI debe ser nulo.");

        // El resto del mapeo debe seguir funcionando (Rol y Usuario directo)
        assertEquals(ID_USUARIO, target.getIdUsuario());
        assertEquals(NOMBRE_ROL, target.getNombreRol());
    }

    @Test
    void map_shouldMapNombreRolAsNull_whenRolIsNull() {
        // ARRANGE
        Usuario source = createMockEntity();
        source.setRol(null); // Eliminamos la relación Rol

        // ACT
        UsuarioPersonaRolResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // El campo de Rol debe ser nulo
        assertNull(target.getNombreRol(), "nombreRol debe ser nulo.");

        // El resto del mapeo debe seguir funcionando (Persona y Usuario directo)
        assertEquals(ID_USUARIO, target.getIdUsuario());
        assertEquals(NOMBRE_PERSONA, target.getNombre());
    }

    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        UsuarioPersonaRolResponse target = mapper.map(null);

        // ASSERT
        assertNull(target, "Mapear un objeto nulo debe devolver nulo.");
    }
}