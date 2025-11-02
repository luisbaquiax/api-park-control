package org.parkcontrol.apiparkcontrol.services.empresa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.parkcontrol.apiparkcontrol.models.Persona;
import org.parkcontrol.apiparkcontrol.repositories.PersonaRepository;
import org.parkcontrol.apiparkcontrol.services.PersonaService;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaService personaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePersona_Success() {
        Persona persona = new Persona();
        persona.setDpi("123456789");
        persona.setCorreo("correo@persona.com");
        persona.setNombre("Luis");
        persona.setApellido("Baquiax");

        when(personaRepository.findByDpi("123456789")).thenReturn(null);
        when(personaRepository.findByCorreo("correo@persona.com")).thenReturn(null);

        Persona personaGuardada = new Persona();
        personaGuardada.setIdPersona(1L);
        personaGuardada.setDpi("123456789");
        personaGuardada.setCorreo("correo@persona.com");
        personaGuardada.setNombre("Luis");
        personaGuardada.setApellido("Baquiax");

        when(personaRepository.save(any(Persona.class))).thenReturn(personaGuardada);

        Persona resultado = personaService.create(persona);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPersona());
        assertEquals("123456789", resultado.getDpi());
        assertEquals("correo@persona.com", resultado.getCorreo());

        verify(personaRepository, times(1)).findByDpi("123456789");
        verify(personaRepository, times(1)).findByCorreo("correo@persona.com");
        verify(personaRepository, times(1)).save(any(Persona.class));
    }

    @Test
    void testCreatePersona_DpiAlreadyExists() {
        Persona persona = new Persona();
        persona.setDpi("123456789");
        persona.setCorreo("correo@persona.com");

        when(personaRepository.findByDpi("123456789")).thenReturn(new Persona());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> personaService.create(persona));
        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("dpi 123456789 ya se encuentra en uso"));

        verify(personaRepository, times(1)).findByDpi("123456789");
        verify(personaRepository, never()).save(any(Persona.class));
    }

    @Test
    void testCreatePersona_CorreoAlreadyExists() {
        Persona persona = new Persona();
        persona.setDpi("123456789");
        persona.setCorreo("correo@persona.com");

        when(personaRepository.findByDpi("123456789")).thenReturn(null);
        when(personaRepository.findByCorreo("correo@persona.com")).thenReturn(new Persona());

        ErrorApi exception = assertThrows(ErrorApi.class, () -> personaService.create(persona));
        assertEquals(401, exception.getStatus());
        assertTrue(exception.getMessage().contains("correo correo@persona.com ya se encuentra en uso"));

        verify(personaRepository, times(1)).findByDpi("123456789");
        verify(personaRepository, times(1)).findByCorreo("correo@persona.com");
        verify(personaRepository, never()).save(any(Persona.class));
    }

    @Test
    void testFindByDpi() {
        Persona persona = new Persona();
        persona.setDpi("123456789");

        when(personaRepository.findByDpi("123456789")).thenReturn(persona);

        Persona resultado = personaService.findByDpi("123456789");
        assertNotNull(resultado);
        assertEquals("123456789", resultado.getDpi());

        verify(personaRepository, times(1)).findByDpi("123456789");
    }
}

