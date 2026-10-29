package com.serenity.chat.application.service;
import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.application.port.out.ReporteChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serenity.usuario.domain.model.Rol;
import java.time.LocalDateTime;
import java.util.List;
@Service
@Transactional
public class ChatService implements ChatUseCase {
 private static final Logger log=LoggerFactory.getLogger(ChatService.class);
 private final ChatRepositoryPort repository;
 private final GestionarAccesoUseCase acceso;
 private final GestionarDisponibilidadUseCase disponibilidad;
 private final ReporteChatRepositoryPort reportes;
 public ChatService(ChatRepositoryPort repository,GestionarAccesoUseCase acceso,GestionarDisponibilidadUseCase disponibilidad,ReporteChatRepositoryPort reportes){this.repository=repository;this.acceso=acceso;this.disponibilidad=disponibilidad;this.reportes=reportes;}
 @Override public Conversacion solicitar(Long id){log.info("Solicitud de chat recibida");acceso.verificarAcceso(id);Long oyente=disponibilidad.listarDisponibles().stream().filter(o->!o.equals(id)&&acceso.esOyenteActivo(o)&&repository.contarActivasDeOyente(o)==0).findFirst().orElse(null);Conversacion conversacion=repository.abrir(id,oyente);log.info("Solicitud de chat procesada");return conversacion;}
 @Override public Conversacion buscarParticipante(Long id,Long usuario){return repository.buscarParticipante(id,usuario);}
 @Override public List<MensajeChat> listarMensajes(Long id,Long usuario){acceso.verificarAcceso(usuario);repository.buscarParticipante(id,usuario);return repository.listarMensajes(id);}
 @Override public List<Conversacion> listarEnEspera(){return repository.listarEnEspera();}
 @Override public List<Conversacion> listarActivasDeOyente(Long oyente){return repository.listarActivasDeOyente(oyente);}
 @Override public Conversacion aceptar(Long id,Long oyente){if(acceso.verificarAcceso(oyente)!=Rol.OYENTE)throw new IllegalArgumentException("Solo un oyente puede aceptar una charla");if(!disponibilidad.estaDisponible(oyente))throw new ConflictException("Debes marcarte como disponible para aceptar una charla");if(repository.contarActivasDeOyente(oyente)>0)throw new ConflictException("Ya tienes una charla activa");repository.asignarOyente(id,oyente);return repository.buscarParticipante(id,oyente);}
 @Override public void finalizar(Long id,Long usuario){acceso.verificarAcceso(usuario);repository.finalizar(id,usuario);log.info("Conversación de chat finalizada");}
 @Override public void reportar(Long id,Long usuario,String motivo){acceso.verificarAcceso(usuario);if(motivo==null||motivo.isBlank())throw new IllegalArgumentException("El motivo es obligatorio");var conversacion=repository.buscarParticipante(id,usuario);if(reportes.existePendiente(id,usuario))throw new ConflictException("Ya existe un reporte pendiente de tu parte para esta conversación");Long reportado=usuario.equals(conversacion.idUsuario())?conversacion.idOyente():conversacion.idUsuario();reportes.guardar(ReporteChat.nuevo(id,usuario,reportado,motivo));log.info("Reporte de conversación registrado");}
 @Override public void guardarMensaje(MensajeChat mensaje,Long usuario){acceso.verificarAcceso(usuario);var c=repository.buscarParticipante(mensaje.idConversacion(),usuario);if(c.fechaFin()!=null)throw new ConflictException("La conversación ya finalizó");if(mensaje.contenido()==null||mensaje.contenido().isBlank())return;String remitente=usuario.equals(c.idUsuario())?"usuario":"oyente";repository.guardarMensaje(new MensajeChat(mensaje.idConversacion(),remitente,mensaje.contenido(),LocalDateTime.now()));}
 @Override public List<MensajeChat> enviarMensaje(Long idConversacion,Long usuarioId,String contenido){
  acceso.verificarAcceso(usuarioId);
  var conversacion=repository.buscarParticipante(idConversacion,usuarioId);
  if(conversacion.fechaFin()!=null)throw new ConflictException("La conversación ya finalizó");
  if(contenido==null||contenido.isBlank())return List.of();
  var ahora=LocalDateTime.now();
  String remitente=usuarioId.equals(conversacion.idUsuario())?"usuario":"oyente";
  var usuario=new MensajeChat(idConversacion,remitente,contenido,ahora);
  repository.guardarMensaje(usuario);
  if(conversacion.idOyente()!=null)return List.of(usuario);
  var oyente=new MensajeChat(idConversacion,"oyente","Gracias por compartirlo. Estoy aquí para acompañarte. ¿Qué te gustaría explorar?",ahora);
  repository.guardarMensaje(oyente);
  return List.of(usuario,oyente);
 }
}
