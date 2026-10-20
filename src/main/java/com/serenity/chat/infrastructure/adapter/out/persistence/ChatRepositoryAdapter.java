package com.serenity.chat.infrastructure.adapter.out.persistence;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.shared.exception.ConflictException;
import java.util.List;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component
public class ChatRepositoryAdapter implements ChatRepositoryPort {
 private final ConversacionJpaRepository conversaciones;private final MensajeChatJpaRepository mensajes;
 public ChatRepositoryAdapter(ConversacionJpaRepository c,MensajeChatJpaRepository m){conversaciones=c;mensajes=m;}
 @Override public Conversacion abrir(Long usuario,Long oyente){return dominio(conversaciones.save(new ConversacionJpaEntity(null,usuario,oyente,LocalDateTime.now(),null)));}
 @Override public Conversacion buscarParticipante(Long id,Long usuario){return conversaciones.buscarParticipante(id,usuario).map(this::dominio).orElseThrow(()->new ResourceNotFoundException("Conversación no encontrada"));}
 @Override public Conversacion buscarPorId(Long id){return conversaciones.findById(id).map(this::dominio).orElseThrow(()->new ResourceNotFoundException("Conversación no encontrada"));}
 @Override public List<MensajeChat> listarMensajes(Long id){return mensajes.findByIdConversacionOrderByFechaEnvioAscIdAsc(id).stream().map(m->new MensajeChat(m.getIdConversacion(),m.getRemitente(),m.getContenido(),m.getFechaEnvio())).toList();}
 @Override public List<Conversacion> listarEnEspera(){return conversaciones.findByIdOyenteIsNullAndFechaFinIsNullOrderByFechaInicioAsc().stream().map(this::dominio).toList();}
 @Override public List<Conversacion> listarActivasDeOyente(Long id){return conversaciones.findByIdOyenteAndFechaFinIsNullOrderByFechaInicioAsc(id).stream().map(this::dominio).toList();}
 @Override public long contarActivasDeOyente(Long id){return conversaciones.countByIdOyenteAndFechaFinIsNull(id);}
 @Override public void asignarOyente(Long id,Long oyente){if(conversaciones.asignarOyente(id,oyente)==0){if(!conversaciones.existsById(id))throw new ResourceNotFoundException("Conversación no encontrada");throw new ConflictException("La charla ya fue tomada por otro oyente o ya finalizó");}}
 @Override public void finalizar(Long id,Long usuario){var c=conversaciones.buscarParticipante(id,usuario).orElseThrow(()->new ResourceNotFoundException("Conversación no encontrada"));if(c.getFechaFin()==null){c.setFechaFin(LocalDateTime.now());conversaciones.save(c);}}
 @Override public void guardarMensaje(MensajeChat m){mensajes.save(new MensajeChatJpaEntity(m.idConversacion(),m.remitente(),m.contenido()));}
 private Conversacion dominio(ConversacionJpaEntity c){return new Conversacion(c.getIdConversacion(),c.getIdUsuario(),c.getIdOyente(),c.getFechaInicio(),c.getFechaFin());}
}
