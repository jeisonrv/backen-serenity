package com.serenity.usuario.infrastructure.adapter.out.persistence;
import com.serenity.usuario.application.port.out.TokenRestablecimientoPasswordPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
@Component
public class TokenRestablecimientoPasswordAdapter implements TokenRestablecimientoPasswordPort {
 private final TokenRestablecimientoPasswordJpaRepository repository;
 public TokenRestablecimientoPasswordAdapter(TokenRestablecimientoPasswordJpaRepository repository){this.repository=repository;}
 @Override @Transactional public void guardar(String tokenHash, Long usuarioId, Instant expiraEn){
  repository.save(new TokenRestablecimientoPasswordJpaEntity(tokenHash,usuarioId,expiraEn));
 }
 @Override public Optional<TokenRestablecimiento> buscarValidoPorHash(String tokenHash, Instant ahora){
  return repository.findFirstByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(tokenHash,ahora)
   .map(e->new TokenRestablecimiento(e.getTokenHash(),e.getUsuarioId(),e.getExpiraEn()));
 }
 @Override @Transactional public void marcarUsado(String tokenHash, Instant usadoEn){
  repository.findFirstByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(tokenHash,usadoEn)
   .ifPresent(e->e.setUsadoEn(usadoEn));
 }
}