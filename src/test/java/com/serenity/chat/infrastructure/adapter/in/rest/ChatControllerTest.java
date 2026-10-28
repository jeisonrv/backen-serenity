package com.serenity.chat.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.infrastructure.adapter.in.rest.ChatController.MensajeRequest;
import com.serenity.chat.infrastructure.adapter.in.rest.ChatController.ReporteRequest;
import com.serenity.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock ChatUseCase chat;
    @Mock SimpMessagingTemplate messaging;

    private ChatController controller;
    private final AuthenticatedUser user = new AuthenticatedUser(7L, "u@test.com");

    @BeforeEach
    void setUp() {
        controller = new ChatController(chat, messaging);
    }

    @Test
    void solicitar_devuelveConversacionCreada() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 27, 20, 0);
        Conversacion c = mock(Conversacion.class);
        when(c.idConversacion()).thenReturn(1L);
        when(c.idUsuario()).thenReturn(7L);
        when(c.idOyente()).thenReturn(null);
        when(c.fechaInicio()).thenReturn(inicio);
        when(c.fechaFin()).thenReturn(null);
        when(chat.solicitar(7L)).thenReturn(c);

        var r = controller.solicitar(user);

        assertEquals(1L, r.idConversacion());
        assertEquals(7L, r.idUsuario());
        assertNull(r.idOyente());
        assertEquals(inicio, r.fechaInicio());
        assertNull(r.fechaFin());
    }

    @Test
    void finalizar_delegaEnCasoDeUso() {
        controller.finalizar(3L, user);
        verify(chat).finalizar(3L, 7L);
    }

    @Test
    void reportar_delegaConMotivo() {
        controller.reportar(new ReporteRequest(3L, "spam"), user);
        verify(chat).reportar(3L, 7L, "spam");
    }

    @Test
    void enviar_sinUsuarioAutenticado_lanzaMessagingException() {
        var headers = SimpMessageHeaderAccessor.create();

        assertThrows(MessagingException.class,
                () -> controller.enviar(3L, new MensajeRequest("hola"), headers));
        verifyNoInteractions(chat, messaging);
    }

    @Test
    void enviar_conPrincipalDeOtroTipo_lanzaMessagingException() {
        var headers = SimpMessageHeaderAccessor.create();
        headers.setUser(new UsernamePasswordAuthenticationToken("texto", null));

        assertThrows(MessagingException.class,
                () -> controller.enviar(3L, new MensajeRequest("hola"), headers));
        verifyNoInteractions(chat, messaging);
    }
}