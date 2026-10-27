package com.serenity.animo.application.port.in;

import com.serenity.animo.domain.model.EstadoAnimo;
import java.util.List;
import java.util.Optional;

public interface ListarAnimoUseCase {
    List<EstadoAnimo> listar(Long usuarioId);
    Optional<EstadoAnimo> obtenerHoy(Long usuarioId);
    List<EstadoAnimo> historial(Long usuarioId, int dias);
}
