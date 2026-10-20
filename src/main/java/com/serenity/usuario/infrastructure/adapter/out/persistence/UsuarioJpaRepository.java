package com.serenity.usuario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmail(String email);
    Optional<UsuarioJpaEntity> findByEmailIgnoreCase(String email);
    Optional<UsuarioJpaEntity> findByGoogleSubject(String googleSubject);
    List<UsuarioJpaEntity> findByRestringidoTrueOrderByFechaRestriccionDesc();
}
