package com.serenity.animo.application.port.out;

import com.serenity.animo.domain.model.EstadoAnimo;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface EstadoAnimoRepositoryPort {
    EstadoAnimo guardar(EstadoAnimo estado);
    List<EstadoAnimo> buscarPorUsuario(Long usuarioId);
    Optional<EstadoAnimo> buscarPorUsuarioYDia(Long usuarioId, LocalDate fechaDia);
    List<EstadoAnimo> buscarHistorial(Long usuarioId, LocalDate desde);
}
