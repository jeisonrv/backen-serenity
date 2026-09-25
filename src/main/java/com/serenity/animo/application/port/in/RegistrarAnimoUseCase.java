package com.serenity.animo.application.port.in;

import com.serenity.animo.domain.model.EstadoAnimo;

public interface RegistrarAnimoUseCase { EstadoAnimo registrar(Long usuarioId, int valor); }
