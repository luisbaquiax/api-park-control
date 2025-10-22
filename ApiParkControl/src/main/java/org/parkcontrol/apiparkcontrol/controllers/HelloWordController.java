package org.parkcontrol.apiparkcontrol.controllers;

import org.parkcontrol.apiparkcontrol.utils.Encriptation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloWordController {
    @GetMapping
    public String helloWorld() {
        return "Hello World";
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/redis")
    public String testRedis() {
        redisTemplate.opsForValue().set("mensaje", "Hola desde Redis en AWS!");
        return redisTemplate.opsForValue().get("mensaje");
    }
}
