package com.serenity.chat.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.shared.consent.ConsentService;
import com.serenity.shared.consent.RequireSensitiveConsent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

 public record ConversacionResponse(
         Long idConversacion,
         Long idUsuario,
         Long idOyente,
         LocalDateTime fechaInicio,
         LocalDateTime fechaFin
 ) {}

 public record Mensaje(
         Long idConversacion,
         String remitente,
         String contenido,
         LocalDateTime fechaEnvio
 ) {}

 public record MensajeRequest(String contenido) {}

 public record ReporteRequest(
         @NotNull Long idConversacion,
         @NotBlank String motivo
 ) {}

 private final ChatUseCase chat;
 private final SimpMessagingTemplate messaging;
 private final ConsentService consentService;

 public ChatController(ChatUseCase chat, SimpMessagingTemplate messaging, ConsentService consentService) {
  this.chat = chat;
  this.messaging = messaging;
  this.consentService = consentService;
 }

 @PostMapping("/solicitar")
 @RequireSensitiveConsent
 @ResponseStatus(HttpStatus.CREATED)
 public ConversacionResponse solicitar(
         @AuthenticationPrincipal AuthenticatedUser user
 ) {
  return response(chat.solicitar(user.id()));
 }

 @GetMapping("/{id}/mensajes")
 @RequireSensitiveConsent
 public java.util.List<Mensaje> mensajes(
         @PathVariable Long id,
         @AuthenticationPrincipal AuthenticatedUser user
 ) {
  requireConversationOwnerConsent(id, user.id());
  return chat.listarMensajes(id, user.id())
          .stream()
          .map(m -> new Mensaje(
                  m.idConversacion(),
                  m.remitente(),
                  m.contenido(),
                  m.fechaEnvio()
          ))
          .toList();
 }

 @PostMapping("/{id}/finalizar")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void finalizar(
         @PathVariable Long id,
         @AuthenticationPrincipal AuthenticatedUser user
 ) {
  chat.finalizar(id, user.id());
 }

 @PostMapping("/reporte")
 @RequireSensitiveConsent
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void reportar(
         @Valid @RequestBody ReporteRequest req,
         @AuthenticationPrincipal AuthenticatedUser user
 ) {
  requireConversationOwnerConsent(req.idConversacion(), user.id());
  chat.reportar(req.idConversacion(), user.id(), req.motivo());
 }

 @MessageMapping("/conversacion/{id}/enviar")
 public void enviar(
         @DestinationVariable Long id,
         MensajeRequest req,
         SimpMessageHeaderAccessor headers
 ) {
  var principal = headers.getUser();

  if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)
          || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
   throw new MessagingException(
           "Se requiere una autenticación válida para enviar mensajes"
   );
  }

  if (!consentService.hasCurrentSensitiveConsent(user.id())) {
   throw new com.serenity.shared.consent.SensitiveConsentRequiredException();
  }
  requireConversationOwnerConsent(id, user.id());

  for (var mensaje : chat.enviarMensaje(id, user.id(), req.contenido())) {
   messaging.convertAndSend(
           "/topic/conversacion/" + id,
           new Mensaje(
                   mensaje.idConversacion(),
                   mensaje.remitente(),
                   mensaje.contenido(),
                   mensaje.fechaEnvio()
           )
   );
  }
 }

 private ConversacionResponse response(Conversacion c) {
  return new ConversacionResponse(
          c.idConversacion(),
          c.idUsuario(),
          c.idOyente(),
          c.fechaInicio(),
          c.fechaFin()
  );
 }

 private void requireConversationOwnerConsent(Long conversationId, Long participantId) {
  Conversacion conversation = chat.buscarParticipante(conversationId, participantId);
  if (!consentService.hasCurrentSensitiveConsent(conversation.idUsuario())) {
   throw new com.serenity.shared.consent.SensitiveConsentRequiredException();
  }
 }
}
