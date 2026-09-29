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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController @RequestMapping("/api/chat")
public class ChatController {
 public record ConversacionResponse(Long idConversacion,Long idUsuario,Long idOyente,LocalDateTime fechaInicio,LocalDateTime fechaFin){}
 public record Mensaje(Long idConversacion,String remitente,String contenido,LocalDateTime fechaEnvio){}
 public record MensajeRequest(String contenido){}
 public record ReporteRequest(@NotNull Long idConversacion,@NotBlank String motivo){}
 private final ChatUseCase chat; private final SimpMessagingTemplate messaging;
 public ChatController(ChatUseCase chat,SimpMessagingTemplate s){this.chat=chat;messaging=s;}
 @PostMapping("/solicitar") @ResponseStatus(HttpStatus.CREATED)
 public ConversacionResponse solicitar(@AuthenticationPrincipal AuthenticatedUser user){return response(chat.solicitar(user.id()));}
 @PostMapping("/{id}/finalizar") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void finalizar(@PathVariable Long id,@AuthenticationPrincipal AuthenticatedUser user){chat.finalizar(id,user.id());}
 @PostMapping("/reporte") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void reportar(@Valid @RequestBody ReporteRequest req,@AuthenticationPrincipal AuthenticatedUser user){
  chat.reportar(req.idConversacion(),user.id(),req.motivo());
 }
 @MessageMapping("/conversacion/{id}/enviar")
 public void enviar(@DestinationVariable Long id,MensajeRequest req,SimpMessageHeaderAccessor headers){
  var authentication=(org.springframework.security.authentication.UsernamePasswordAuthenticationToken)headers.getUser();
  var user=(AuthenticatedUser)authentication.getPrincipal();
  for(var mensaje:chat.enviarMensaje(id,user.id(),req.contenido())){
   messaging.convertAndSend("/topic/conversacion/"+id,
    new Mensaje(mensaje.idConversacion(),mensaje.remitente(),mensaje.contenido(),mensaje.fechaEnvio()));
  }
 }
 private ConversacionResponse response(Conversacion c){return new ConversacionResponse(c.idConversacion(),c.idUsuario(),c.idOyente(),c.fechaInicio(),c.fechaFin());}
}
