package org.parkcontrol.apiparkcontrol.controllers.errorapi;

import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.parkcontrol.apiparkcontrol.utils.ErrorApi.class)
    public ResponseEntity<Map<String, Object>> handleErrorApi(ErrorApi ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", ex.getStatus());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }
}