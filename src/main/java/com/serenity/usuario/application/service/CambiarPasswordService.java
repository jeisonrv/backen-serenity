package com.serenity.usuario.application.service;

import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.in.CambiarPasswordUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CambiarPasswordService implements CambiarPasswordUseCase {

    private final UsuarioRepositoryPort repositorio;
    private final PasswordEncoder passwordEncoder;

    public CambiarPasswordService(
            UsuarioRepositoryPort repositorio,
            PasswordEncoder passwordEncoder
    ) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void cambiar(
            Long usuarioId,
            String passwordActual,
            String passwordNueva
    ) {
        if (passwordActual == null || passwordActual.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes escribir tu contraseña actual."
            );
        }

        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener mínimo 8 caracteres."
            );
        }

        Usuario usuario = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado."));

        if (usuario.getPasswordHash() == null
                || !passwordEncoder.matches(
                passwordActual,
                usuario.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta."
            );
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        repositorio.guardar(usuario);
    }
}
