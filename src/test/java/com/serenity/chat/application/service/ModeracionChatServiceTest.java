package com.serenity.chat.application.service;

import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.application.port.out.ReporteChatRepositoryPort;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.domain.model.EstadoReporte;
import com.serenity.chat.domain.model.MensajeChat;
import com.serenity.chat.domain.model.ReporteChat;
import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeracionChatServiceTest {
    @Mock ReporteChatRepositoryPort reportes;
    @Mock ChatRepositoryPort chat;
    @Mock GestionarAccesoUseCase acceso;
    private ModeracionChatService servicio;

    @BeforeEach void setUp() { servicio = new ModeracionChatService(reportes, chat, acceso, 3); }

    @Test void listarReportesDelegaElEstadoSolicitado() {
        var lista = List.of(reporte(1L, 10L, 20L, EstadoReporte.PENDIENTE));
        when(reportes.listar(EstadoReporte.PENDIENTE)).thenReturn(lista);
        assertSame(lista, servicio.listarReportes(EstadoReporte.PENDIENTE));
    }

    @Test void detalleReporteObtieneMensajesYContadoresDelReportado() {
        var reporte = reporte(1L, 10L, 20L, EstadoReporte.PENDIENTE);
        var conversacion = conversacion(7L, 10L, 20L);
        var mensajes = List.of(new MensajeChat(7L, "usuario", "texto", LocalDateTime.now()));
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte));
        when(chat.buscarPorId(7L)).thenReturn(conversacion);
        when(chat.listarMensajes(7L)).thenReturn(mensajes);
        when(reportes.contarPorReportado(20L)).thenReturn(4L);
        when(reportes.contarPorReportadoYEstado(20L, EstadoReporte.PROCEDENTE)).thenReturn(2L);

        var detalle = servicio.detalleReporte(1L);

        assertSame(reporte, detalle.reporte());
        assertSame(conversacion, detalle.conversacion());
        assertEquals(mensajes, detalle.mensajes());
        assertEquals(4L, detalle.reportesTotalesDelReportado());
        assertEquals(2L, detalle.reportesProcedentesDelReportado());
    }

    @Test void detalleReporteDerivaReportadoDesdeConversacionCuandoFaltaEnReporte() {
        var reporte = reporte(1L, 10L, null, EstadoReporte.PENDIENTE);
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, 20L));
        when(chat.listarMensajes(7L)).thenReturn(List.of());
        when(reportes.contarPorReportado(20L)).thenReturn(0L);
        when(reportes.contarPorReportadoYEstado(20L, EstadoReporte.PROCEDENTE)).thenReturn(0L);

        assertEquals(0L, servicio.detalleReporte(1L).reportesTotalesDelReportado());
        verify(reportes).contarPorReportado(20L);
    }

    @Test void detalleReporteDerivaReportadoCuandoReportanteEsOyenteYNoHayReportado() {
        var reporte = reporte(1L, 20L, null, EstadoReporte.PENDIENTE);
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, 20L));
        when(chat.listarMensajes(7L)).thenReturn(List.of());
        when(reportes.contarPorReportado(10L)).thenReturn(1L);
        when(reportes.contarPorReportadoYEstado(10L, EstadoReporte.PROCEDENTE)).thenReturn(0L);

        assertEquals(1L, servicio.detalleReporte(1L).reportesTotalesDelReportado());
        verify(reportes).contarPorReportado(10L);
    }

    @Test void detalleReporteSinUsuarioReportadoMuestraContadoresEnCero() {
        var reporte = reporte(1L, 10L, null, EstadoReporte.PENDIENTE);
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, null));
        when(chat.listarMensajes(7L)).thenReturn(List.of());

        var detalle = servicio.detalleReporte(1L);

        assertEquals(0L, detalle.reportesTotalesDelReportado());
        assertEquals(0L, detalle.reportesProcedentesDelReportado());
        verify(reportes, never()).contarPorReportado(anyLong());
    }

    @Test void detalleReporteInexistenteLanzaNoEncontrado() {
        when(reportes.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> servicio.detalleReporte(99L));
        verifyNoInteractions(chat);
    }

    @Test void resolverReporteExigeObservacionYReportePendiente() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.resolverReporte(1L, 30L, true, " "));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.resolverReporte(1L, 30L, true, null));
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte(1L, 10L, 20L, EstadoReporte.IMPROCEDENTE)));
        assertThrows(ConflictException.class,
                () -> servicio.resolverReporte(1L, 30L, true, "revisado"));
    }

    @Test void resolverReporteProcedenteUsaUsuarioReportadoYAplicaUmbral() {
        var pendiente = reporte(1L, 10L, null, EstadoReporte.PENDIENTE);
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(pendiente));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, 20L));
        when(reportes.guardar(any(ReporteChat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reportes.contarPorReportadoYEstado(20L, EstadoReporte.PROCEDENTE)).thenReturn(3L);

        var resuelto = servicio.resolverReporte(1L, 30L, true, "  confirmado  ");

        assertEquals(EstadoReporte.PROCEDENTE, resuelto.estado());
        assertEquals(20L, resuelto.idReportado());
        assertEquals(30L, resuelto.idModerador());
        assertEquals("confirmado", resuelto.observacionModerador());
        verify(acceso).restringirSiAplica(20L, "Acumuló 3 reportes procedentes por mal uso del chat");
    }

    @Test void resolverReporteProcedenteNoRestringeDebajoDelUmbral() {
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte(1L, 10L, 20L, EstadoReporte.PENDIENTE)));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, 20L));
        when(reportes.guardar(any(ReporteChat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reportes.contarPorReportadoYEstado(20L, EstadoReporte.PROCEDENTE)).thenReturn(2L);

        assertEquals(EstadoReporte.PROCEDENTE,
                servicio.resolverReporte(1L, 30L, true, "confirmado").estado());
        verify(acceso, never()).restringirSiAplica(anyLong(), anyString());
    }

    @Test void resolverReporteImprocedenteNoConsultaUmbralNiRestringe() {
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte(1L, 10L, 20L, EstadoReporte.PENDIENTE)));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, 20L));
        when(reportes.guardar(any(ReporteChat.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(EstadoReporte.IMPROCEDENTE,
                servicio.resolverReporte(1L, 30L, false, "sin evidencia").estado());
        verify(reportes, never()).contarPorReportadoYEstado(anyLong(), any());
        verifyNoInteractions(acceso);
    }

    @Test void resolverReporteProcedenteSinReportadoIdentificableLanzaError() {
        when(reportes.buscarPorId(1L)).thenReturn(Optional.of(reporte(1L, 10L, null, EstadoReporte.PENDIENTE)));
        when(chat.buscarPorId(7L)).thenReturn(conversacion(7L, 10L, null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.resolverReporte(1L, 30L, true, "confirmado"));
        verify(reportes, never()).guardar(any());
    }

    @Test void operacionesDeRestriccionDeleganEnPuertoDeAcceso() {
        var usuario = new Usuario(20L, "ana", "masked", null, 1, 0, 0, 0);
        LocalDateTime hasta = LocalDateTime.now().plusDays(2);
        when(acceso.restringir(20L, "motivo", hasta, 30L)).thenReturn(usuario);
        when(acceso.levantarRestriccion(20L)).thenReturn(usuario);
        when(acceso.listarRestringidos()).thenReturn(List.of(usuario));

        assertSame(usuario, servicio.restringirUsuario(20L, 30L, "motivo", hasta));
        assertSame(usuario, servicio.levantarRestriccion(20L));
        assertEquals(List.of(usuario), servicio.listarRestringidos());
        verify(acceso).restringir(20L, "motivo", hasta, 30L);
        verify(acceso).levantarRestriccion(20L);
        verify(acceso).listarRestringidos();
    }

    private ReporteChat reporte(Long id, Long reportante, Long reportado, EstadoReporte estado) {
        return new ReporteChat(id, 7L, reportante, reportado, "spam", estado,
                LocalDateTime.now(), null, null, null);
    }

    private Conversacion conversacion(Long id, Long usuario, Long oyente) {
        return new Conversacion(id, usuario, oyente, LocalDateTime.now(), null);
    }
}
