package com.serenity.chat.infrastructure.adapter.out.persistence;
import com.serenity.chat.domain.model.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReporteChatJpaRepository extends JpaRepository<ReporteChatJpaEntity,Long>{
 List<ReporteChatJpaEntity> findAllByOrderByFechaReporteDesc();
 List<ReporteChatJpaEntity> findByEstadoOrderByFechaReporteDesc(EstadoReporte estado);
 boolean existsByIdConversacionAndIdUsuarioAndEstado(Long conversacion,Long reportante,EstadoReporte estado);
 long countByIdReportado(Long reportado);
 long countByIdReportadoAndEstado(Long reportado,EstadoReporte estado);
}
