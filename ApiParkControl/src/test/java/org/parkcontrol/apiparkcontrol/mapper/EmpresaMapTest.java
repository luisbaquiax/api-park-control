package org.parkcontrol.apiparkcontrol.mapper;


import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.dto.empresa.EmpresaResponse;
import org.parkcontrol.apiparkcontrol.models.Empresa;
import org.parkcontrol.apiparkcontrol.models.Usuario;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmpresaMapTest {

    private EmpresaMap mapper = new EmpresaMapImpl();

    private final Long ID_EMPRESA = 1L;
    private final Long ID_USUARIO_EMPRESA = 99L; // El campo anidado
    private final String NOMBRE_COMERCIAL = "ParkControl GT";
    private final String RAZON_SOCIAL = "Servicios de Estacionamiento S.A.";
    private final String NIT = "1234567-8";
    private final Empresa.EstadoEmpresa ESTADO = Empresa.EstadoEmpresa.ACTIVA;
    private final LocalDateTime FECHA_REGISTRO = LocalDateTime.of(2025, 1, 15, 12, 0);


    /** Crea una entidad Empresa completamente poblada con Mock para la relación Usuario. */
    private Empresa createMockEntity() {
        Empresa entity = new Empresa();
        entity.setIdEmpresa(ID_EMPRESA);
        entity.setNombreComercial(NOMBRE_COMERCIAL);
        entity.setRazonSocial(RAZON_SOCIAL);
        entity.setNit(NIT);
        entity.setDireccionFiscal("Avenida Principal");
        entity.setTelefonoPrincipal("55551234");
        entity.setCorreoPrincipal("contacto@parkcontrol.com");
        entity.setEstado(ESTADO);
        entity.setFechaRegistro(FECHA_REGISTRO);

        // Mocking para la relación Usuario
        Usuario mockUsuario = mock(Usuario.class);
        // Configuramos el valor que se debe extraer en el mapeo anidado
        when(mockUsuario.getIdUsuario()).thenReturn(ID_USUARIO_EMPRESA);
        entity.setUsuarioEmpresa(mockUsuario);

        return entity;
    }

    @Test
    void map_shouldMapAllFieldsCorrectly() {
        // ARRANGE
        Empresa source = createMockEntity();

        // ACT
        EmpresaResponse target = mapper.map(source);

        // ASSERT: Verificar el mapeo
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");

        // Campos Directos
        assertEquals(ID_EMPRESA, target.getIdEmpresa(), "El ID de la empresa no coincide.");
        assertEquals(NOMBRE_COMERCIAL, target.getNombreComercial(), "El nombre comercial no coincide.");
        assertEquals(NIT, target.getNit(), "El NIT no coincide.");
        assertEquals(ESTADO, target.getEstado(), "El estado no coincide.");
        assertEquals(FECHA_REGISTRO, target.getFechaRegistro(), "La fecha de registro no coincide.");

        // Campo Anidado (@Mapping(target = "idUsuarioEmpresa", source = "usuarioEmpresa.idUsuario"))
        assertEquals(ID_USUARIO_EMPRESA, target.getIdUsuarioEmpresa(),
                "El ID de Usuario Empresa mapeado (anidado) no coincide.");
    }

    @Test
    void map_shouldReturnNull_whenInputIsNull() {
        // ARRANGE & ACT
        EmpresaResponse target = mapper.map(null);

        // ASSERT
        assertNull(target, "Mapear un objeto nulo debe devolver nulo.");
    }

    @Test
    void map_shouldMapNullIdUsuarioEmpresa_whenUsuarioEmpresaIsNull() {
        // ARRANGE
        Empresa source = createMockEntity();
        source.setUsuarioEmpresa(null); // Establece el objeto Usuario a null

        // ACT
        EmpresaResponse target = mapper.map(source);

        // ASSERT
        assertNotNull(target, "El DTO de respuesta no debe ser nulo.");
        assertNull(target.getIdUsuarioEmpresa(),
                "idUsuarioEmpresa debe ser nulo si la relación es nula.");
        // Verificamos que los otros campos sigan mapeando correctamente
        assertEquals(ID_EMPRESA, target.getIdEmpresa());
        assertEquals(NIT, target.getNit());
    }
}