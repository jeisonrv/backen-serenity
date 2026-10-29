package com.serenity.oyente.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import com.serenity.oyente.infrastructure.adapter.in.rest.OyenteController.CambiarEstadoRequest;
import com.serenity.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OyenteControllerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    GestionarDisponibilidadUseCase disponibilidad;

    @Mock
    ChatUseCase chat;

    private OyenteController controller;
    private final AuthenticatedUser user = new AuthenticatedUser(7L, "oyente@test.com");

    @BeforeEach
    void setUp() {
        controller = new OyenteController(disponibilidad, chat);
    }

    @Test
    void estado_devuelveDisponibilidadDelUsuario() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 27, 20, 0);
        when(disponibilidad.obtener(7L).disponible()).thenReturn(true);
        when(disponibilidad.obtener(7L).fechaActualizacion()).thenReturn(fecha);

        var r = controller.estado(user);

        assertTrue(r.disponible());
        assertEquals(fecha, r.fechaActualizacion());
    }

    @Test
    void cambiarEstado_delegaYDevuelveNuevoEstado() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 27, 21, 0);
        when(disponibilidad.cambiar(7L, true).disponible()).thenReturn(true);
        when(disponibilidad.cambiar(7L, true).fechaActualizacion()).thenReturn(fecha);

        var r = controller.cambiarEstado(new CambiarEstadoRequest(true), user);

        assertTrue(r.disponible());
        assertEquals(fecha, r.fechaActualizacion());
    }

    @Test
    void disponibles_sinCharlas_devuelveListaVacia() {
        when(chat.listarEnEspera()).thenReturn(List.of());

        var r = controller.disponibles();

        assertFalse(r.hayCharlas());
        assertEquals(0, r.total());
        assertTrue(r.charlas().isEmpty());
    }

    @Test
    void disponibles_calculaMinutosEnEsperaYNuncaNegativos() {
        Conversacion pasada = mock(Conversacion.class);
        when(pasada.idConversacion()).thenReturn(1L);
        when(pasada.fechaInicio()).thenReturn(LocalDateTime.now().minusMinutes(10));

        Conversacion futura = mock(Conversacion.class);
        when(futura.idConversacion()).thenReturn(2L);
        when(futura.fechaInicio()).thenReturn(LocalDateTime.now().plusHours(1));

        when(chat.listarEnEspera()).thenReturn(List.of(pasada, futura));

        var r = controller.disponibles();

        assertTrue(r.hayCharlas());
        assertEquals(2, r.total());
        assertTrue(r.charlas().get(0).minutosEnEspera() >= 10);
        assertEquals(0, r.charlas().get(1).minutosEnEspera());
    }

    @Test
    void activas_mapeaConversacionesDelOyente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 27, 18, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 10, 27, 18, 30);
        Conversacion c = mock(Conversacion.class);
        when(c.idConversacion()).thenReturn(5L);
        when(c.fechaInicio()).thenReturn(inicio);
        when(c.fechaFin()).thenReturn(fin);
        when(chat.listarActivasDeOyente(7L)).thenReturn(List.of(c));

        var r = controller.activas(user);

        assertEquals(1, r.size());
        assertEquals(5L, r.get(0).idConversacion());
        assertEquals(inicio, r.get(0).fechaInicio());
        assertEquals(fin, r.get(0).fechaFin());
    }

    @Test
    void aceptar_devuelveConversacionAceptada() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 27, 19, 0);
        Conversacion c = mock(Conversacion.class);
        when(c.idConversacion()).thenReturn(9L);
        when(c.fechaInicio()).thenReturn(inicio);
        when(c.fechaFin()).thenReturn(null);
        when(chat.aceptar(9L, 7L)).thenReturn(c);

        var r = controller.aceptar(9L, user);

        assertEquals(9L, r.idConversacion());
        assertEquals(inicio, r.fechaInicio());
        assertNull(r.fechaFin());
    }
}
