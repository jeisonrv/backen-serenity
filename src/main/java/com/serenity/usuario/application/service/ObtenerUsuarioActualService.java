package com.serenity.usuario.application.service;

import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.in.ObtenerUsuarioActualUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class ObtenerUsuarioActualService implements ObtenerUsuarioActualUseCase {
    private final UsuarioRepositoryPort usuarios;

    public ObtenerUsuarioActualService(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public Usuario obtener(Long usuarioId) {
        return usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
