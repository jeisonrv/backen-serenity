package com.serenity.chat.application.service;

import com.serenity.chat.application.port.out.ChatRepositoryPort;
import com.serenity.chat.application.port.out.ReporteChatRepositoryPort;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.domain.model.MensajeChat;
import com.serenity.chat.domain.model.ReporteChat;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock ChatRepositoryPort repository;
    @Mock GestionarAccesoUseCase acceso;
    @Mock GestionarDisponibilidadUseCase disponibilidad;
    @Mock ReporteChatRepositoryPort reportes;
    private ChatService servicio;

    @BeforeEach void setUp() { servicio = new ChatService(repository, acceso, disponibilidad, reportes); }

    @Test void solicitarAsignaAlPrimerOyenteDisponibleElegible() {
        var conversacion = new Conversacion(20L, 3L, 12L, LocalDateTime.now(), null);
        when(disponibilidad.listarDisponibles()).thenReturn(List.of(3L, 11L, 12L, 13L));
        when(acceso.esOyenteActivo(11L)).thenReturn(false);
        when(acceso.esOyenteActivo(12L)).thenReturn(true);
        when(repository.contarActivasDeOyente(12L)).thenReturn(0L);
        when(repository.abrir(3L, 12L)).thenReturn(conversacion);

        assertSame(conversacion, servicio.solicitar(3L));
        verify(acceso).verificarAcceso(3L);
        verify(repository).abrir(3L, 12L);
        verifyNoMoreInteractions(repository);
    }

    @Test void solicitarAbreEnEsperaCuandoNoHayOyentesElegibles() {
        when(disponibilidad.listarDisponibles()).thenReturn(List.of(3L, 11L));
        when(acceso.esOyenteActivo(11L)).thenReturn(true);
        when(repository.contarActivasDeOyente(11L)).thenReturn(1L);
        var conversacion = new Conversacion(21L, 3L, null, LocalDateTime.now(), null);
        when(repository.abrir(3L, null)).thenReturn(conversacion);

        assertSame(conversacion, servicio.solicitar(3L));
        verify(repository).abrir(3L, null);
    }

    @Test void delegaBusquedasListasYFinalizacion() {
        var conversacion = new Conversacion(4L, 2L, 8L, LocalDateTime.now(), null);
        var mensajes = List.of(new MensajeChat(4L, "usuario", "hola", LocalDateTime.now()));
        when(repository.buscarParticipante(4L, 2L)).thenReturn(conversacion);
        when(repository.listarMensajes(4L)).thenReturn(mensajes);
        when(repository.listarEnEspera()).thenReturn(List.of(conversacion));
        when(repository.listarActivasDeOyente(8L)).thenReturn(List.of(conversacion));

        assertSame(conversacion, servicio.buscarParticipante(4L, 2L));
        assertEquals(mensajes, servicio.listarMensajes(4L, 2L));
        assertEquals(List.of(conversacion), servicio.listarEnEspera());
        assertEquals(List.of(conversacion), servicio.listarActivasDeOyente(8L));
        servicio.finalizar(4L, 2L);
        verify(acceso, times(2)).verificarAcceso(2L);
        verify(repository).finalizar(4L, 2L);
    }

    @Test void aceptarAsignaOyenteYDevuelveConversacion() {
        var conversacion = new Conversacion(4L, 2L, 8L, LocalDateTime.now(), null);
        when(acceso.verificarAcceso(8L)).thenReturn(Rol.OYENTE);
        when(disponibilidad.estaDisponible(8L)).thenReturn(true);
        when(repository.contarActivasDeOyente(8L)).thenReturn(0L);
        when(repository.buscarParticipante(4L, 8L)).thenReturn(conversacion);

        assertSame(conversacion, servicio.aceptar(4L, 8L));
        verify(repository).asignarOyente(4L, 8L);
    }

    @Test void aceptarRechazaUsuariosQueNoSonOyentes() {
        when(acceso.verificarAcceso(8L)).thenReturn(Rol.USUARIO);
        assertThrows(IllegalArgumentException.class, () -> servicio.aceptar(4L, 8L));
        verifyNoInteractions(disponibilidad, repository);
    }

    @Test void aceptarRechazaOyenteNoDisponible() {
        when(acceso.verificarAcceso(8L)).thenReturn(Rol.OYENTE);
        when(disponibilidad.estaDisponible(8L)).thenReturn(false);
        assertThrows(ConflictException.class, () -> servicio.aceptar(4L, 8L));
        verify(repository, never()).asignarOyente(anyLong(), anyLong());
    }

    @Test void aceptarRechazaOyenteConOtraConversacionActiva() {
        when(acceso.verificarAcceso(8L)).thenReturn(Rol.OYENTE);
        when(disponibilidad.estaDisponible(8L)).thenReturn(true);
        when(repository.contarActivasDeOyente(8L)).thenReturn(1L);
        assertThrows(ConflictException.class, () -> servicio.aceptar(4L, 8L));
        verify(repository, never()).asignarOyente(anyLong(), anyLong());
    }

    @Test void reportarGuardaElReporteContraElOtroParticipante() {
        var conversacion = new Conversacion(4L, 2L, 8L, LocalDateTime.now(), null);
        when(repository.buscarParticipante(4L, 2L)).thenReturn(conversacion);
        when(reportes.existePendiente(4L, 2L)).thenReturn(false);

        servicio.reportar(4L, 2L, "  conducta inapropiada  ");

        ArgumentCaptor<ReporteChat> captor = ArgumentCaptor.forClass(ReporteChat.class);
        verify(reportes).guardar(captor.capture());
        assertEquals(4L, captor.getValue().idConversacion());
        assertEquals(2L, captor.getValue().idReportante());
        assertEquals(8L, captor.getValue().idReportado());
        assertEquals("conducta inapropiada", captor.getValue().motivo());
    }

    @Test void reportarRechazaMotivoVacioYReportePendienteDuplicado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.reportar(4L, 2L, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.reportar(4L, 2L, " "));
        when(repository.buscarParticipante(4L, 2L)).thenReturn(new Conversacion(4L, 2L, 8L, null, null));
        when(reportes.existePendiente(4L, 2L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> servicio.reportar(4L, 2L, "spam"));
        verify(reportes, never()).guardar(any());
    }

    @Test void reportarDesdeElOyenteIdentificaAlUsuarioComoReportado() {
        when(repository.buscarParticipante(4L, 8L)).thenReturn(
                new Conversacion(4L, 2L, 8L, null, null));
        when(reportes.existePendiente(4L, 8L)).thenReturn(false);

        servicio.reportar(4L, 8L, "spam");

        ArgumentCaptor<ReporteChat> captor = ArgumentCaptor.forClass(ReporteChat.class);
        verify(reportes).guardar(captor.capture());
        assertEquals(8L, captor.getValue().idReportante());
        assertEquals(2L, captor.getValue().idReportado());
    }

    @Test void guardarMensajeIgnoraContenidoNuloOVacio() {
        var conversacion = new Conversacion(4L, 2L, 8L, null, null);
        when(repository.buscarParticipante(4L, 2L)).thenReturn(conversacion);
        servicio.guardarMensaje(new MensajeChat(4L, "usuario", null, null), 2L);
        servicio.guardarMensaje(new MensajeChat(4L, "usuario", "  ", null), 2L);
        verify(repository, never()).guardarMensaje(any());
    }

    @Test void guardarMensajeRechazaConversacionFinalizadaYGuardaAutorCorrecto() {
        when(repository.buscarParticipante(4L, 2L)).thenReturn(
                new Conversacion(4L, 2L, 8L, null, LocalDateTime.now()));
        when(repository.buscarParticipante(4L, 8L)).thenReturn(
                new Conversacion(4L, 2L, 8L, null, null));
        assertThrows(ConflictException.class,
                () -> servicio.guardarMensaje(new MensajeChat(4L, "", "hola", null), 2L));

        servicio.guardarMensaje(new MensajeChat(4L, "", "hola", null), 8L);
        ArgumentCaptor<MensajeChat> captor = ArgumentCaptor.forClass(MensajeChat.class);
        verify(repository).guardarMensaje(captor.capture());
        assertEquals("oyente", captor.getValue().remitente());
        assertEquals("hola", captor.getValue().contenido());
    }

    @Test void guardarMensajeDesdeUsuarioConservaAutorUsuario() {
        when(repository.buscarParticipante(4L, 2L)).thenReturn(
                new Conversacion(4L, 2L, 8L, null, null));
        servicio.guardarMensaje(new MensajeChat(4L, "", "hola", null), 2L);
        ArgumentCaptor<MensajeChat> captor = ArgumentCaptor.forClass(MensajeChat.class);
        verify(repository).guardarMensaje(captor.capture());
        assertEquals("usuario", captor.getValue().remitente());
    }

    @Test void enviarMensajeEnConversacionAsignadaGuardaSoloMensajeDelRemitente() {
        when(repository.buscarParticipante(4L, 8L)).thenReturn(new Conversacion(4L, 2L, 8L, null, null));
        var mensajes = servicio.enviarMensaje(4L, 8L, "hola");
        assertEquals(1, mensajes.size());
        assertEquals("oyente", mensajes.get(0).remitente());
        assertEquals("hola", mensajes.get(0).contenido());
        verify(repository).guardarMensaje(mensajes.get(0));
    }

    @Test void enviarMensajeEnEsperaGuardaRespuestaAutomatica() {
        when(repository.buscarParticipante(4L, 2L)).thenReturn(new Conversacion(4L, 2L, null, null, null));
        var mensajes = servicio.enviarMensaje(4L, 2L, "hola");
        assertEquals(2, mensajes.size());
        assertEquals("usuario", mensajes.get(0).remitente());
        assertEquals("oyente", mensajes.get(1).remitente());
        assertEquals(mensajes.get(0).fechaEnvio(), mensajes.get(1).fechaEnvio());
        verify(repository, times(2)).guardarMensaje(any());
    }

    @Test void enviarMensajeIgnoraContenidoVacioYRechazaConversacionFinalizada() {
        when(repository.buscarParticipante(4L, 2L)).thenReturn(
                new Conversacion(4L, 2L, null, null, null),
                new Conversacion(4L, 2L, null, null, LocalDateTime.now()));
        assertTrue(servicio.enviarMensaje(4L, 2L, " ").isEmpty());
        assertThrows(ConflictException.class, () -> servicio.enviarMensaje(4L, 2L, "hola"));
        verify(repository, never()).guardarMensaje(any());
    }
}
