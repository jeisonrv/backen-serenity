package com.serenity.usuario.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "tokens_restablecimiento_password",
       indexes = @Index(name = "idx_token_restablecimiento_hash", columnList = "token_hash", unique = true))
public class TokenRestablecimientoPasswordJpaEntity {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
 @Column(name = "usuario_id", nullable = false) private Long usuarioId;
 @Column(name = "expira_en", nullable = false) private Instant expiraEn;
 @Column(name = "usado_en") private Instant usadoEn;
 protected TokenRestablecimientoPasswordJpaEntity() {}
 public TokenRestablecimientoPasswordJpaEntity(String tokenHash, Long usuarioId, Instant expiraEn) {
  this.tokenHash=tokenHash; this.usuarioId=usuarioId; this.expiraEn=expiraEn;
 }
 public String getTokenHash(){return tokenHash;}
 public Long getUsuarioId(){return usuarioId;}
 public Instant getExpiraEn(){return expiraEn;}
 public Instant getUsadoEn(){return usadoEn;}
 public void setUsadoEn(Instant usadoEn){this.usadoEn=usadoEn;}
}