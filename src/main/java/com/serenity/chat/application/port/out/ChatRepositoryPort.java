package com.serenity.chat.application.port.out;
import com.serenity.chat.domain.model.*;
import java.util.List;
public interface ChatRepositoryPort {
 Conversacion abrir(Long usuarioId, Long idOyente);
 Conversacion buscarParticipante(Long id, Long usuarioId);
 Conversacion buscarPorId(Long id);
 List<MensajeChat> listarMensajes(Long idConversacion);
 List<Conversacion> listarEnEspera();
 List<Conversacion> listarActivasDeOyente(Long oyenteId);
 long contarActivasDeOyente(Long oyenteId);
 void asignarOyente(Long id, Long oyenteId);
 void finalizar(Long id,Long usuarioId);
 void guardarMensaje(MensajeChat mensaje);
}
