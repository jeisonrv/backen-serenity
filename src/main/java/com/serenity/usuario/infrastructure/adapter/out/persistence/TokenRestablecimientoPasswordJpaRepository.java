package com.serenity.usuario.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;
public interface TokenRestablecimientoPasswordJpaRepository extends JpaRepository<TokenRestablecimientoPasswordJpaEntity, Long> {
 Optional<TokenRestablecimientoPasswordJpaEntity> findFirstByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(String tokenHash, Instant ahora);
}