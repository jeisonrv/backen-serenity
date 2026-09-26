package com.serenity.chat.infrastructure.adapter.out.persistence;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component
public class ChatRepositoryAdapter implements ChatRepositoryPort {
 private final ConversacionJpaRepository conversaciones;private final ReporteChatJpaRepository reportes;private final MensajeChatJpaRepository mensajes;
 public ChatRepositoryAdapter(ConversacionJpaRepository c,ReporteChatJpaRepository r,MensajeChatJpaRepository m){conversaciones=c;reportes=r;mensajes=m;}
 @Override public Conversacion abrir(Long usuario){return dominio(conversaciones.save(new ConversacionJpaEntity(null,usuario,1L,LocalDateTime.now(),null)));}
 @Override public Conversacion buscarPropia(Long id,Long usuario){return conversaciones.findByIdConversacionAndIdUsuario(id,usuario).map(this::dominio).orElseThrow(()->new IllegalStateException("Conversación no encontrada"));}
 @Override public void finalizar(Long id,Long usuario){var c=conversaciones.findByIdConversacionAndIdUsuario(id,usuario).orElseThrow(()->new IllegalStateException("Conversación no encontrada"));if(c.getFechaFin()==null){c.setFechaFin(LocalDateTime.now());conversaciones.save(c);}}
 @Override public void reportar(Long id,Long usuario,String motivo){reportes.save(new ReporteChatJpaEntity(id,usuario,motivo));}
 @Override public void guardarMensaje(MensajeChat m){mensajes.save(new MensajeChatJpaEntity(m.idConversacion(),m.remitente(),m.contenido()));}
 private Conversacion dominio(ConversacionJpaEntity c){return new Conversacion(c.getIdConversacion(),c.getIdUsuario(),c.getIdOyente(),c.getFechaInicio(),c.getFechaFin());}
}
