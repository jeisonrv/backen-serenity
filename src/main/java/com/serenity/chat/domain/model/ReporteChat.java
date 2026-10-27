package com.serenity.chat.domain.model;
import java.time.LocalDateTime;
public record ReporteChat(Long id, Long idConversacion, Long idReportante, Long idReportado,
        String motivo, EstadoReporte estado, LocalDateTime fechaReporte, Long idModerador,
        String observacionModerador, LocalDateTime fechaResolucion) {
    public static ReporteChat nuevo(Long conversacion, Long reportante, Long reportado, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo es obligatorio");
        return new ReporteChat(null, conversacion, reportante, reportado, motivo.trim(), EstadoReporte.PENDIENTE,
                LocalDateTime.now(), null, null, null);
    }
    public ReporteChat resolver(boolean procedente, Long reportado, Long moderador, String observacion) {
        if (estado != EstadoReporte.PENDIENTE) throw new IllegalStateException("El reporte ya fue resuelto");
        if (observacion == null || observacion.isBlank()) throw new IllegalArgumentException("La observación es obligatoria");
        return new ReporteChat(id,idConversacion,idReportante,reportado,motivo,
                procedente ? EstadoReporte.PROCEDENTE : EstadoReporte.IMPROCEDENTE,fechaReporte,
                moderador,observacion.trim(),LocalDateTime.now());
    }
}
