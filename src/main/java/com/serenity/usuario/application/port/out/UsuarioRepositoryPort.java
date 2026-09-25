package com.serenity.usuario.application.port.out;

import com.serenity.usuario.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(Long id);
}
