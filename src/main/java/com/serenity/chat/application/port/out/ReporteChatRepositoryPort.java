package com.serenity.chat.application.port.out;
import com.serenity.chat.domain.model.EstadoReporte;
import com.serenity.chat.domain.model.ReporteChat;
import java.util.List;
import java.util.Optional;
public interface ReporteChatRepositoryPort {
 ReporteChat guardar(ReporteChat reporte);
 Optional<ReporteChat> buscarPorId(Long id);
 List<ReporteChat> listar(EstadoReporte estado);
 boolean existePendiente(Long conversacionId,Long reportanteId);
 long contarPorReportado(Long reportadoId);
 long contarPorReportadoYEstado(Long reportadoId,EstadoReporte estado);
}
