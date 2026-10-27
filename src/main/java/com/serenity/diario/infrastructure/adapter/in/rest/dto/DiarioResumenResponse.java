package com.serenity.diario.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.serenity.diario.domain.model.EntradaDiario;
import java.time.LocalDateTime;

public record DiarioResumenResponse(
        Long idEntrada,
        Long idUsuario,
        String titulo,
        String contenido,
        String tipoPrompt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaRegistro) {

    public static DiarioResumenResponse desde(EntradaDiario entrada) {
        return new DiarioResumenResponse(entrada.idEntrada(), entrada.idUsuario(), entrada.titulo(),
                extracto(entrada.contenido()), entrada.tipoPrompt(), entrada.fechaCreacion());
    }

    private static String extracto(String contenido) {
        if (contenido == null) return "";
        String texto = contenido.strip();
        return texto.length() <= 120 ? texto : texto.substring(0, 120) + "…";
    }
}
