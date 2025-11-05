package org.parkcontrol.apiparkcontrol.controllers.errorapi;

import org.parkcontrol.apiparkcontrol.utils.ErrorApi;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
    @GetMapping("/dummy/error")
    public void throwError() {
        throw new ErrorApi(HttpStatus.BAD_REQUEST.value(), "Simulated error for testing");
    }
}

