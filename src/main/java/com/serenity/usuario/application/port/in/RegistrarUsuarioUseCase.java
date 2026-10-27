package com.serenity.usuario.application.port.in;

import com.serenity.usuario.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {
    Usuario registrar(String username, String email, String passwordPlano);
}
