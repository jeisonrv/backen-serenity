package com.serenity.ejercicio.infrastructure.adapter.in.rest.dto;

import com.serenity.ejercicio.domain.model.Sesion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionResponse {
    private Long idSesion;
    private Long idUsuario;
    private String tipoEjercicio;
    private Integer duracion;
    private Integer experienciaGanada;
    private LocalDateTime fechaCompletada;

    public static SesionResponse desde(Sesion sesion) {
        return new SesionResponse(sesion.getId(), sesion.getUsuarioId(), sesion.getTipoEjercicio(),
                sesion.getDuracionSegundos(), sesion.getXpGanado(), sesion.getFechaCompletado());
    }
}
