package com.serenity.diario.domain.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public record EntradaDiario(Long idEntrada, Long idUsuario, String titulo, String contenido, String tipoPrompt,
                            LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
    @JsonProperty("fechaRegistro")
    public LocalDateTime fechaRegistro() { return fechaCreacion; }
}
