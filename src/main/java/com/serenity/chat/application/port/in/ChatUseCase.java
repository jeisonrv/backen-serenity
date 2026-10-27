package com.serenity.chat.application.port.in;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.domain.model.MensajeChat;
import java.util.List;
public interface ChatUseCase {
 Conversacion solicitar(Long usuarioId);
 void finalizar(Long conversacionId,Long usuarioId);
 void reportar(Long conversacionId,Long usuarioId,String motivo);
 void guardarMensaje(MensajeChat mensaje,Long usuarioId);
 List<MensajeChat> enviarMensaje(Long idConversacion, Long usuarioId, String contenido);
 Conversacion buscarParticipante(Long id, Long usuarioId);
 List<MensajeChat> listarMensajes(Long id, Long usuarioId);
 List<Conversacion> listarEnEspera();
 List<Conversacion> listarActivasDeOyente(Long oyenteId);
 Conversacion aceptar(Long id, Long oyenteId);
}
