package com.serenity.usuario.application.port.out;

import com.serenity.usuario.domain.model.Usuario;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepositoryPort {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorEmailIgnorandoMayusculas(String email);
    Optional<Usuario> buscarPorGoogleSubject(String googleSubject);
    Optional<Usuario> buscarPorId(Long id);
    List<Usuario> listarRestringidos();
}
