package com.serenity.chat.application.port.in;
import com.serenity.chat.domain.model.DetalleReporte;
import com.serenity.chat.domain.model.EstadoReporte;
import com.serenity.chat.domain.model.ReporteChat;
import com.serenity.usuario.domain.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
public interface ModeracionChatUseCase {
 List<ReporteChat> listarReportes(EstadoReporte estado);
 DetalleReporte detalleReporte(Long id);
 ReporteChat resolverReporte(Long reporteId,Long moderadorId,boolean procedente,String observacion);
 Usuario restringirUsuario(Long usuarioId,Long moderadorId,String motivo,LocalDateTime hasta);
 Usuario levantarRestriccion(Long usuarioId);
 List<Usuario> listarRestringidos();
}
