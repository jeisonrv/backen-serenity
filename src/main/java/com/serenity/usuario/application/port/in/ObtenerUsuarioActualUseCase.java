package com.serenity.usuario.application.port.in;

import com.serenity.usuario.domain.model.Usuario;

public interface ObtenerUsuarioActualUseCase {
    Usuario obtener(Long usuarioId);
}
