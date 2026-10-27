package com.serenity.chat.infrastructure.adapter.out.persistence;
import com.serenity.chat.application.port.out.ReporteChatRepositoryPort;
import com.serenity.chat.domain.model.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component
public class ReporteChatRepositoryAdapter implements ReporteChatRepositoryPort {
 private final ReporteChatJpaRepository repository;
 public ReporteChatRepositoryAdapter(ReporteChatJpaRepository repository){this.repository=repository;}
 @Override public ReporteChat guardar(ReporteChat r){return dominio(repository.save(new ReporteChatJpaEntity(r.id(),r.idConversacion(),r.idReportante(),r.idReportado(),r.motivo(),r.estado(),r.fechaReporte(),r.idModerador(),r.observacionModerador(),r.fechaResolucion())));}
 @Override public Optional<ReporteChat> buscarPorId(Long id){return repository.findById(id).map(this::dominio);}
 @Override public List<ReporteChat> listar(EstadoReporte estado){return (estado==null?repository.findAllByOrderByFechaReporteDesc():repository.findByEstadoOrderByFechaReporteDesc(estado)).stream().map(this::dominio).toList();}
 @Override public boolean existePendiente(Long c,Long r){return repository.existsByIdConversacionAndIdUsuarioAndEstado(c,r,EstadoReporte.PENDIENTE);}
 @Override public long contarPorReportado(Long id){return repository.countByIdReportado(id);}
 @Override public long contarPorReportadoYEstado(Long id,EstadoReporte e){return repository.countByIdReportadoAndEstado(id,e);}
 private ReporteChat dominio(ReporteChatJpaEntity e){return new ReporteChat(e.getId(),e.getIdConversacion(),e.getIdUsuario(),e.getIdReportado(),e.getMotivo(),e.getEstado(),e.getFechaReporte(),e.getIdModerador(),e.getObservacionModerador(),e.getFechaResolucion());}
}
