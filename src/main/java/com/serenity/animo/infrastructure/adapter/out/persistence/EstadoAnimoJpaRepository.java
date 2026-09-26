package com.serenity.animo.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface EstadoAnimoJpaRepository extends JpaRepository<EstadoAnimoJpaEntity, Long> {
    List<EstadoAnimoJpaEntity> findByIdUsuarioOrderByFechaRegistroDesc(Long idUsuario);
    Optional<EstadoAnimoJpaEntity> findByIdUsuarioAndFechaDia(Long idUsuario, LocalDate fechaDia);
    List<EstadoAnimoJpaEntity> findByIdUsuarioAndFechaDiaGreaterThanEqualOrderByFechaDiaDesc(Long idUsuario, LocalDate desde);
}
