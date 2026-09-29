package com.serenity.chat.application.port.out;
import com.serenity.chat.domain.model.*;
public interface ChatRepositoryPort {
 Conversacion abrir(Long usuarioId);
 Conversacion buscarPropia(Long id,Long usuarioId);
 void finalizar(Long id,Long usuarioId);
 void reportar(Long id,Long usuarioId,String motivo);
 void guardarMensaje(MensajeChat mensaje);
}
