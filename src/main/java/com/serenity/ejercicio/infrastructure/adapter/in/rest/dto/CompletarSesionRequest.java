package com.serenity.ejercicio.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompletarSesionRequest(@NotBlank String tipoEjercicio, @NotNull @Positive Integer duracion) {}
