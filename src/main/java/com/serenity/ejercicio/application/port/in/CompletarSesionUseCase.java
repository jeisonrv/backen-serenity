package com.serenity.ejercicio.application.port.in;

import com.serenity.ejercicio.domain.model.Sesion;

public interface CompletarSesionUseCase {
    Sesion completar(Long usuarioId, String tipoEjercicio, int duracionSegundos);
}
