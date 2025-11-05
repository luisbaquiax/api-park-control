package org.parkcontrol.apiparkcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiParkControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiParkControlApplication.class, args);
    }

}
