package com.serenity.chat.application.port.in;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.domain.model.MensajeChat;
public interface ChatUseCase {
 Conversacion solicitar(Long usuarioId);
 Conversacion buscarPropia(Long conversacionId,Long usuarioId);
 void finalizar(Long conversacionId,Long usuarioId);
 void reportar(Long conversacionId,Long usuarioId,String motivo);
 void guardarMensaje(MensajeChat mensaje,Long usuarioId);
}
