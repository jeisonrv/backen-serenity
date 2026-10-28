package com.serenity.frase.infrastructure.adapter.in.rest;

import com.serenity.frase.application.port.in.ObtenerFraseDelDiaUseCase;
import com.serenity.frase.domain.model.FraseMotivacional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraseMotivacionalController {

    private final ObtenerFraseDelDiaUseCase obtenerFrase;

    public FraseMotivacionalController(
            ObtenerFraseDelDiaUseCase obtenerFrase
    ) {
        this.obtenerFrase = obtenerFrase;
    }

    @GetMapping("/api/frases/motivacional")
    public FraseMotivacional obtener() {
        return obtenerFrase.obtener();
    }
}