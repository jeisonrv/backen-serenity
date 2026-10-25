package com.serenity.usuario.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;
import java.util.Optional;
public interface TokenRestablecimientoPasswordJpaRepository extends JpaRepository<TokenRestablecimientoPasswordJpaEntity, Long> {
 Optional<TokenRestablecimientoPasswordJpaEntity> findFirstByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(String tokenHash, Instant ahora);
}