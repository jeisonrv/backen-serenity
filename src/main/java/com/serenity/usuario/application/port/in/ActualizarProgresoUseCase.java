package com.serenity.usuario.application.port.in;

public interface ActualizarProgresoUseCase {
    void actualizar(Long usuarioId, int xpGanado);
}