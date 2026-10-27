package com.serenity.notificacion.application.port.in;

import com.serenity.usuario.domain.model.Usuario;

public interface EnviarCorreoTransaccionalUseCase {
    void enviarBienvenida(Usuario usuario);
}
