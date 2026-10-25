package com.serenity.usuario.application.port.out;
import java.time.Instant;
import java.util.Optional;
public interface TokenRestablecimientoPasswordPort {
 void guardar(String tokenHash, Long usuarioId, Instant expiraEn);
 Optional<TokenRestablecimiento> buscarValidoPorHash(String tokenHash, Instant ahora);
 void marcarUsado(String tokenHash, Instant usadoEn);
 record TokenRestablecimiento(String tokenHash, Long usuarioId, Instant expiraEn) {}
}