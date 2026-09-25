package com.serenity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hola")
    public String saludar() {
        return "¡El backend de Serenity está respondiendo correctamente!";
    }
}