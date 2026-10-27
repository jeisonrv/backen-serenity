package com.serenity.ejercicio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {
    private Long id;
    private Long usuarioId;
    private String tipoEjercicio;
    private Integer duracionSegundos;
    private Integer xpGanado;
    private LocalDateTime fechaCompletado;

    public Sesion(Long usuarioId, String tipoEjercicio, Integer duracionSegundos) {
        this(null, usuarioId, tipoEjercicio, duracionSegundos,
                Math.max(1, duracionSegundos / 60), LocalDateTime.now());
    }
}
