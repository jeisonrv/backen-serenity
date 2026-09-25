package com.serenity.ejercicio.infrastructure.adapter.out.persistence;

import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.ejercicio.domain.model.Sesion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SesionRepositoryAdapter implements SesionRepositoryPort {
    private final SesionJpaRepository repository;

    @Override
    public Sesion guardar(Sesion sesion) {
        SesionJpaEntity entity = new SesionJpaEntity(
                sesion.getId(),
                sesion.getUsuarioId(),
                sesion.getTipoEjercicio(),
                sesion.getDuracionSegundos(),
                sesion.getXpGanado(),
                sesion.getFechaCompletado()
        );
        return desdeEntidad(repository.save(entity), sesion.getFechaCompletado());
    }

    @Override
    public List<Sesion> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(entity -> desdeEntidad(entity, entity.getFechaCompletado()))
                .toList();
    }

    private Sesion desdeEntidad(SesionJpaEntity entity, java.time.LocalDateTime fechaCompletado) {
        return new Sesion(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getTipoEjercicio(),
                entity.getDuracionSegundos(),
                entity.getXpGanado(),
                fechaCompletado
        );
    }
}
