package com.serenity.usuario.application.port.out;

import com.serenity.usuario.domain.model.IdentidadGoogle;

import java.util.Optional;

public interface VerificarIdentidadGooglePort {
    Optional<IdentidadGoogle> verificar(String credential);
}
