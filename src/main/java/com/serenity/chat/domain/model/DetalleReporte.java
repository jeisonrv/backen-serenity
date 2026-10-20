package com.serenity.chat.domain.model;
import java.util.List;
public record DetalleReporte(ReporteChat reporte, Conversacion conversacion, List<MensajeChat> mensajes,
        long reportesTotalesDelReportado, long reportesProcedentesDelReportado) {}
