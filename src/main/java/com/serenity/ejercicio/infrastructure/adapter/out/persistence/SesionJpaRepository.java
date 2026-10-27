package com.serenity.ejercicio.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionJpaRepository extends JpaRepository<SesionJpaEntity, Long> {
    List<SesionJpaEntity> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndFechaCompletadoGreaterThanEqualAndFechaCompletadoLessThan(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );
}