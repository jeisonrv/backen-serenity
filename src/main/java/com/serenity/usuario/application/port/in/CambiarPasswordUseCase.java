package com.serenity.usuario.application.port.in;

public interface CambiarPasswordUseCase {

    void cambiar(
            Long usuarioId,
            String passwordActual,
            String passwordNueva
    );
}
