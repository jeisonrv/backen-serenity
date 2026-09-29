package com.serenity.chat.application.service;
import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class ChatService implements ChatUseCase {
 private final ChatRepositoryPort repository;
 public ChatService(ChatRepositoryPort repository){this.repository=repository;}
 @Override public Conversacion solicitar(Long id){return repository.abrir(id);}
 @Override public Conversacion buscarPropia(Long id,Long usuario){return repository.buscarPropia(id,usuario);}
 @Override public void finalizar(Long id,Long usuario){repository.finalizar(id,usuario);}
 @Override public void reportar(Long id,Long usuario,String motivo){if(motivo==null||motivo.isBlank())throw new IllegalArgumentException("El motivo es obligatorio");repository.buscarPropia(id,usuario);repository.reportar(id,usuario,motivo);}
 @Override public void guardarMensaje(MensajeChat mensaje,Long usuario){var c=repository.buscarPropia(mensaje.idConversacion(),usuario);if(c.fechaFin()!=null)throw new IllegalStateException("La conversación ya finalizó");repository.guardarMensaje(new MensajeChat(mensaje.idConversacion(),mensaje.remitente(),mensaje.contenido(),LocalDateTime.now()));}
 @Override public List<MensajeChat> enviarMensaje(Long idConversacion,Long usuarioId,String contenido){
  var conversacion=repository.buscarPropia(idConversacion,usuarioId);
  if(conversacion.fechaFin()!=null)throw new IllegalStateException("La conversación ya finalizó");
  if(contenido==null||contenido.isBlank())return List.of();
  var ahora=LocalDateTime.now();
  var usuario=new MensajeChat(idConversacion,"usuario",contenido,ahora);
  var oyente=new MensajeChat(idConversacion,"oyente","Gracias por compartirlo. Estoy aquí para acompañarte. ¿Qué te gustaría explorar?",ahora);
  repository.guardarMensaje(usuario);
  repository.guardarMensaje(oyente);
  return List.of(usuario,oyente);
 }
}
