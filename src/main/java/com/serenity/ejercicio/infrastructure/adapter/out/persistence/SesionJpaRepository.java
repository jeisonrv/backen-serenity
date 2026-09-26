package com.serenity.ejercicio.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SesionJpaRepository extends JpaRepository<SesionJpaEntity, Long> {
    List<SesionJpaEntity> findByUsuarioId(Long usuarioId);
}