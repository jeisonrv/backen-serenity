package com.serenity.usuario.application.service;
import com.serenity.usuario.application.port.in.RestablecerPasswordUseCase;
import com.serenity.usuario.application.port.out.TokenRestablecimientoPasswordPort;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service
public class RestablecerPasswordService implements RestablecerPasswordUseCase {
 private final TokenRestablecimientoPasswordPort tokens;
 private final UsuarioRepositoryPort usuarios;
 private final PasswordEncoder encoder;
 public RestablecerPasswordService(TokenRestablecimientoPasswordPort tokens,UsuarioRepositoryPort usuarios,PasswordEncoder encoder){
  this.tokens=tokens;this.usuarios=usuarios;this.encoder=encoder;
 }
 @Override @Transactional public void restablecer(String token,String passwordNueva){
  if(token==null||token.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace no es válido o ha caducado. Solicita uno nuevo.");
  if(passwordNueva==null||passwordNueva.length()<8||passwordNueva.length()>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 8 y 72 caracteres.");
  String hash=SolicitarRestablecimientoPasswordService.sha256(token);
  Instant ahora=Instant.now();
  var validado=tokens.buscarValidoPorHash(hash,ahora).orElseThrow(()->new IllegalArgumentException("El enlace no es válido o ha caducado. Solicita uno nuevo."));
  Usuario usuario=usuarios.buscarPorId(validado.usuarioId()).orElseThrow(()->new IllegalArgumentException("El enlace no es válido."));
  usuario.setPasswordHash(encoder.encode(passwordNueva));
  usuarios.guardar(usuario);
  tokens.marcarUsado(hash,ahora);
 }
}