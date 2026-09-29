package com.serenity.ejercicio.application.port.in;

import com.serenity.ejercicio.domain.model.Sesion;
import java.util.List;

public interface ListarSesionesUseCase {
    List<Sesion> listar(Long usuarioId);
}
