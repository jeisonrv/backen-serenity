package com.serenity.chat.application.service;
import com.serenity.chat.application.port.in.ModeracionChatUseCase;
import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.application.port.out.ReporteChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @Transactional
public class ModeracionChatService implements ModeracionChatUseCase {
 private final ReporteChatRepositoryPort reportes; private final ChatRepositoryPort chat; private final GestionarAccesoUseCase acceso;
 private final int umbral;
 public ModeracionChatService(ReporteChatRepositoryPort reportes,ChatRepositoryPort chat,GestionarAccesoUseCase acceso,
        @Value("${app.moderacion.reportes-procedentes-para-restringir:3}") int umbral){this.reportes=reportes;this.chat=chat;this.acceso=acceso;this.umbral=umbral;}
 @Override @Transactional(readOnly=true) public List<ReporteChat> listarReportes(EstadoReporte estado){return reportes.listar(estado);}
 @Override @Transactional(readOnly=true) public DetalleReporte detalleReporte(Long id){ReporteChat r=buscar(id);Conversacion c=chat.buscarPorId(r.idConversacion());Long reportado=resolverReportado(r,c);return new DetalleReporte(r,c,chat.listarMensajes(c.idConversacion()),reportado==null?0:reportes.contarPorReportado(reportado),reportado==null?0:reportes.contarPorReportadoYEstado(reportado,EstadoReporte.PROCEDENTE));}
 @Override public ReporteChat resolverReporte(Long id,Long moderador,boolean procedente,String observacion){
  if(observacion==null||observacion.isBlank())throw new IllegalArgumentException("La observación es obligatoria");
  ReporteChat r=buscar(id);if(r.estado()!=EstadoReporte.PENDIENTE)throw new ConflictException("El reporte ya fue resuelto");
  Conversacion c=chat.buscarPorId(r.idConversacion());Long reportado=resolverReportado(r,c);
  if(procedente&&reportado==null)throw new IllegalArgumentException("No se pudo identificar al usuario reportado");
  ReporteChat resuelto=r.resolver(procedente,reportado,moderador,observacion);reportes.guardar(resuelto);
  if(procedente){long total=reportes.contarPorReportadoYEstado(reportado,EstadoReporte.PROCEDENTE);if(total>=umbral)acceso.restringirSiAplica(reportado,"Acumuló "+total+" reportes procedentes por mal uso del chat");}
  return resuelto;
 }
 @Override public Usuario restringirUsuario(Long id,Long moderador,String motivo,LocalDateTime hasta){return acceso.restringir(id,motivo,hasta,moderador);}
 @Override public Usuario levantarRestriccion(Long id){return acceso.levantarRestriccion(id);}
 @Override @Transactional(readOnly=true) public List<Usuario> listarRestringidos(){return acceso.listarRestringidos();}
 private ReporteChat buscar(Long id){return reportes.buscarPorId(id).orElseThrow(()->new ResourceNotFoundException("Reporte no encontrado"));}
 private Long resolverReportado(ReporteChat r,Conversacion c){if(r.idReportado()!=null)return r.idReportado();if(r.idReportante().equals(c.idUsuario()))return c.idOyente();return c.idUsuario();}
}
