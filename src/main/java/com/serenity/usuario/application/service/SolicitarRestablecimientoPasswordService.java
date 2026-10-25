package com.serenity.usuario.application.service;
import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import com.serenity.usuario.application.port.in.SolicitarRestablecimientoPasswordUseCase;
import com.serenity.usuario.application.port.out.TokenRestablecimientoPasswordPort;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
@Service
public class SolicitarRestablecimientoPasswordService implements SolicitarRestablecimientoPasswordUseCase {
 private final UsuarioRepositoryPort usuarios;
 private final TokenRestablecimientoPasswordPort tokens;
 private final ProveedorCorreoPort correo;
 private final boolean emailHabilitado;
 private final String frontendUrl;
 private final SecureRandom random=new SecureRandom();
 public SolicitarRestablecimientoPasswordService(UsuarioRepositoryPort usuarios, TokenRestablecimientoPasswordPort tokens,
   ProveedorCorreoPort correo, @Value("${app.correo.habilitado:false}") boolean emailHabilitado,
   @Value("${app.auth.frontend-url:http://localhost:8080}") String frontendUrl){
  this.usuarios=usuarios; this.tokens=tokens; this.correo=correo; this.emailHabilitado=emailHabilitado;
  this.frontendUrl=frontendUrl.replaceAll("/+$","");
 }
 @Override @Transactional public void solicitar(String correoSolicitado){
  if(correoSolicitado==null || correoSolicitado.isBlank()) return;
  Usuario usuario=usuarios.buscarPorEmailIgnorandoMayusculas(correoSolicitado.trim()).orElse(null);
  if(usuario==null || usuario.getEmail()==null || usuario.getPasswordHash()==null ||
     usuario.getPasswordHash().isBlank() || !emailHabilitado) return;
  byte[] raw=new byte[32]; random.nextBytes(raw);
  String token=Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
  String hash=sha256(token);
  tokens.guardar(hash,usuario.getId(),Instant.now().plus(Duration.ofMinutes(30)));
  String link=frontendUrl+"/restablecer-password?token="+token;
  String nombre=usuario.getUsername()==null||usuario.getUsername().isBlank()?"Hola":escaparHtml(usuario.getUsername());
  String html="<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;color:#24364b;line-height:1.6\">"
   +"<h1 style=\"color:#5685b5\">Restablece tu contraseña de Serenity</h1>"
   +"<p>"+nombre+", recibimos una solicitud para cambiar la contraseña de tu cuenta.</p>"
   +"<p>El enlace caduca en 30 minutos y solo se puede usar una vez.</p>"
   +"<p><a href=\""+link+"\" style=\"display:inline-block;padding:12px 20px;background:#8dc5ff;color:#10243b;text-decoration:none;border-radius:8px\">Crear nueva contraseña</a></p>"
   +"<p>Si no solicitaste este cambio, puedes ignorar este mensaje.</p></div>";
  String texto="Restablece tu contraseña de Serenity (válido por 30 minutos): "+link+"\nSi no solicitaste este cambio, ignora este mensaje.";
  try { correo.enviar(usuario.getEmail(),"Recupera tu contraseña de Serenity",html,texto); }
  catch(RuntimeException ex) { /* no revelar si la cuenta existe ni devolver el token */ }
 }
 static String sha256(String value){
  try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
  catch(Exception e){throw new IllegalStateException("No se pudo crear el token de recuperación",e);}
 }
 private String escaparHtml(String value){return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}