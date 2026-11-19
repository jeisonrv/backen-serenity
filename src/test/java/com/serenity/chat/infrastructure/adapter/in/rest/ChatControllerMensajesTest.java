package com.serenity.chat.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.MensajeChat;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.chat.infrastructure.adapter.in.rest.ChatController.MensajeRequest;
import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.shared.consent.ConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerMensajesTest {

    @Mock ChatUseCase chat;
    @Mock SimpMessagingTemplate messaging;
    @Mock ConsentService consentService;

    private ChatController controller;
    private final AuthenticatedUser user = new AuthenticatedUser(7L, "u@test.com");

    @BeforeEach
    void setUp() {
        when(consentService.hasCurrentSensitiveConsent(7L)).thenReturn(true);
        controller = new ChatController(chat, messaging, consentService);
    }

    @Test
    void mensajes_mapeaMensajesDeLaConversacion() {
        LocalDateTime f1 = LocalDateTime.of(2026, 10, 27, 20, 0);
        LocalDateTime f2 = LocalDateTime.of(2026, 10, 27, 20, 1);
        when(chat.buscarParticipante(3L, 7L)).thenReturn(new Conversacion(3L, 7L, 8L, f1, null));
        when(chat.listarMensajes(3L, 7L)).thenReturn(List.of(
                new MensajeChat(3L, "USUARIO", "hola", f1),
                new MensajeChat(3L, "OYENTE", "buenas", f2)));

        var r = controller.mensajes(3L, user);

        assertEquals(2, r.size());
        assertEquals("hola", r.get(0).contenido());
        assertEquals("USUARIO", r.get(0).remitente());
        assertEquals(f1, r.get(0).fechaEnvio());
        assertEquals("buenas", r.get(1).contenido());
        assertEquals(3L, r.get(1).idConversacion());
    }

    @Test
    void enviar_conUsuarioValido_publicaCadaMensajeEnElTopic() {
        LocalDateTime f1 = LocalDateTime.of(2026, 10, 27, 20, 0);
        LocalDateTime f2 = LocalDateTime.of(2026, 10, 27, 20, 1);
        when(chat.buscarParticipante(3L, 7L)).thenReturn(new Conversacion(3L, 7L, 8L, f1, null));
        when(chat.enviarMensaje(3L, 7L, "hola")).thenReturn(List.of(
                new MensajeChat(3L, "USUARIO", "hola", f1),
                new MensajeChat(3L, "SISTEMA", "recibido", f2)));

        var headers = SimpMessageHeaderAccessor.create();
        headers.setUser(new UsernamePasswordAuthenticationToken(user, null));

        controller.enviar(3L, new MensajeRequest("hola"), headers);

        ArgumentCaptor<ChatController.Mensaje> captor =
                ArgumentCaptor.forClass(ChatController.Mensaje.class);
        verify(messaging, times(2)).convertAndSend(eq("/topic/conversacion/3"), captor.capture());

        assertEquals("hola", captor.getAllValues().get(0).contenido());
        assertEquals("recibido", captor.getAllValues().get(1).contenido());
        assertEquals(f2, captor.getAllValues().get(1).fechaEnvio());
    }

    @Test
    void enviarVariasConversacionesPublicaUnicodeEnTopicsSeparados() {
        when(chat.buscarParticipante(3L, 7L)).thenReturn(new Conversacion(3L, 7L, 8L, null, null));
        when(chat.buscarParticipante(4L, 7L)).thenReturn(new Conversacion(4L, 7L, 9L, null, null));
        when(chat.enviarMensaje(3L, 7L, "Hola 😊")).thenReturn(
                List.of(new MensajeChat(3L, "usuario", "Hola 😊", LocalDateTime.now())));
        when(chat.enviarMensaje(4L, 7L, "Café 東京")).thenReturn(
                List.of(new MensajeChat(4L, "usuario", "Café 東京", LocalDateTime.now())));
        var headers = SimpMessageHeaderAccessor.create();
        headers.setUser(new UsernamePasswordAuthenticationToken(user, null));

        controller.enviar(3L, new MensajeRequest("Hola 😊"), headers);
        controller.enviar(4L, new MensajeRequest("Café 東京"), headers);

        ArgumentCaptor<ChatController.Mensaje> mensajesCaptor =
                ArgumentCaptor.forClass(ChatController.Mensaje.class);
        verify(messaging).convertAndSend(eq("/topic/conversacion/3"), mensajesCaptor.capture());
        verify(messaging).convertAndSend(eq("/topic/conversacion/4"), mensajesCaptor.capture());
        assertEquals("Hola 😊", mensajesCaptor.getAllValues().get(0).contenido());
        assertEquals(3L, mensajesCaptor.getAllValues().get(0).idConversacion());
        assertEquals("Café 東京", mensajesCaptor.getAllValues().get(1).contenido());
        assertEquals(4L, mensajesCaptor.getAllValues().get(1).idConversacion());
    }
}
