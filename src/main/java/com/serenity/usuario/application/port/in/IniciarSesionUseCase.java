package com.serenity.usuario.application.port.in;

import com.serenity.usuario.domain.model.Usuario;

public interface IniciarSesionUseCase {
    record ResultadoLogin(Usuario usuario, String token) {}
    ResultadoLogin iniciarSesion(String email, String passwordPlano);
}