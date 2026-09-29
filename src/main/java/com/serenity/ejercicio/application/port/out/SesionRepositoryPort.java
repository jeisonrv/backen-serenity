package com.serenity.ejercicio.application.port.out;

import com.serenity.ejercicio.domain.model.Sesion;
import java.util.List;

public interface SesionRepositoryPort {
    Sesion guardar(Sesion sesion);
    List<Sesion> buscarPorUsuario(Long usuarioId);
}
