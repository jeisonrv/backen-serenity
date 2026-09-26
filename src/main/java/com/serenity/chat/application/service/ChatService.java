package com.serenity.chat.application.service;
import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
public class ChatService implements ChatUseCase {
 private final ChatRepositoryPort repository;
 public ChatService(ChatRepositoryPort repository){this.repository=repository;}
 @Override public Conversacion solicitar(Long id){return repository.abrir(id);}
 @Override public Conversacion buscarPropia(Long id,Long usuario){return repository.buscarPropia(id,usuario);}
 @Override public void finalizar(Long id,Long usuario){repository.finalizar(id,usuario);}
 @Override public void reportar(Long id,Long usuario,String motivo){if(motivo==null||motivo.isBlank())throw new IllegalArgumentException("El motivo es obligatorio");repository.buscarPropia(id,usuario);repository.reportar(id,usuario,motivo);}
 @Override public void guardarMensaje(MensajeChat mensaje,Long usuario){var c=repository.buscarPropia(mensaje.idConversacion(),usuario);if(c.fechaFin()!=null)throw new IllegalStateException("La conversación ya finalizó");repository.guardarMensaje(new MensajeChat(mensaje.idConversacion(),mensaje.remitente(),mensaje.contenido(),LocalDateTime.now()));}
}
