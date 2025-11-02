package org.parkcontrol.apiparkcontrol.controllers.errorapi;

import org.junit.jupiter.api.Test;
import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleErrorApi_ReturnsExpectedResponse() {
        // Arrange
        int status = HttpStatus.BAD_REQUEST.value();
        String message = "Parámetros inválidos";
        ErrorApi exception = new ErrorApi(status, message);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleErrorApi(exception);

        // Assert
        assertNotNull(response, "La respuesta no debe ser nula");
        assertEquals(status, response.getStatusCodeValue(), "El código HTTP debe coincidir");

        Map<String, Object> body = response.getBody();
        assertNotNull(body, "El cuerpo no debe ser nulo");
        assertEquals(status, body.get("code"), "El código del cuerpo debe coincidir");
        assertEquals(message, body.get("message"), "El mensaje debe coincidir");
    }
}
