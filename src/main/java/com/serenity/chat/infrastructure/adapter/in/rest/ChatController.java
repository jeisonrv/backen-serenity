package com.serenity.chat.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.domain.model.MensajeChat;
import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController @RequestMapping("/api/chat")
public class ChatController {
 public record ConversacionResponse(Long idConversacion,Long idUsuario,Long idOyente,LocalDateTime fechaInicio,LocalDateTime fechaFin){}
 public record Mensaje(Long idConversacion,String remitente,String contenido,LocalDateTime fechaEnvio){}
 public record MensajeRequest(String contenido){}
 public record ReporteRequest(Long idConversacion,String motivo){}
 private final ChatUseCase chat; private final SimpMessagingTemplate messaging;
 public ChatController(ChatUseCase chat,SimpMessagingTemplate s){this.chat=chat;messaging=s;}
 @PostMapping("/solicitar")
 public ConversacionResponse solicitar(@AuthenticationPrincipal AuthenticatedUser user){return response(chat.solicitar(user.id()));}
 @PostMapping("/{id}/finalizar") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void finalizar(@PathVariable Long id,@AuthenticationPrincipal AuthenticatedUser user){chat.finalizar(id,user.id());}
 @PostMapping("/reporte") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void reportar(@RequestBody ReporteRequest req,@AuthenticationPrincipal AuthenticatedUser user){
  if(req.idConversacion()==null)throw new IllegalArgumentException("La conversación es obligatoria");
  chat.reportar(req.idConversacion(),user.id(),req.motivo());
 }
 @MessageMapping("/conversacion/{id}/enviar")
 public void enviar(@DestinationVariable Long id,MensajeRequest req,SimpMessageHeaderAccessor headers){
  var authentication=(org.springframework.security.authentication.UsernamePasswordAuthenticationToken)headers.getUser();
  var user=(AuthenticatedUser)authentication.getPrincipal();chat.buscarPropia(id,user.id());
  if(req.contenido()==null||req.contenido().isBlank())return;
  publicar(new Mensaje(id,"usuario",req.contenido(),LocalDateTime.now()),user.id());
  publicar(new Mensaje(id,"oyente","Gracias por compartirlo. Estoy aquí para acompañarte. ¿Qué te gustaría explorar?",LocalDateTime.now()),user.id());
 }
 private void publicar(Mensaje mensaje,Long usuarioId){chat.guardarMensaje(new MensajeChat(mensaje.idConversacion(),mensaje.remitente(),mensaje.contenido(),mensaje.fechaEnvio()),usuarioId);messaging.convertAndSend("/topic/conversacion/"+mensaje.idConversacion(),mensaje);}
 private ConversacionResponse response(Conversacion c){return new ConversacionResponse(c.idConversacion(),c.idUsuario(),c.idOyente(),c.fechaInicio(),c.fechaFin());}
}
