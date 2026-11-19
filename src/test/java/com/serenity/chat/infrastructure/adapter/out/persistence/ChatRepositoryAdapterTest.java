package com.serenity.chat.infrastructure.adapter.out.persistence;

import com.serenity.chat.domain.model.MensajeChat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRepositoryAdapterTest {
    @Mock ConversacionJpaRepository conversaciones;
    @Mock MensajeChatJpaRepository mensajes;

    @Test
    void persisteYRecuperaUnicodeSinAlterarElContenidoNiLaConversacion() {
        ChatRepositoryAdapter adapter = new ChatRepositoryAdapter(conversaciones, mensajes);
        String contenido = "¡Hola! 😊 Café — 東京";

        adapter.guardarMensaje(new MensajeChat(42L, "oyente", contenido, LocalDateTime.now()));

        ArgumentCaptor<MensajeChatJpaEntity> captor = ArgumentCaptor.forClass(MensajeChatJpaEntity.class);
        verify(mensajes).save(captor.capture());
        assertEquals(42L, captor.getValue().getIdConversacion());
        assertEquals(contenido, captor.getValue().getContenido());

        MensajeChatJpaEntity persistido = new MensajeChatJpaEntity(42L, "oyente", contenido);
        when(mensajes.findByIdConversacionOrderByFechaEnvioAscIdAsc(42L))
                .thenReturn(List.of(persistido));

        List<MensajeChat> historial = adapter.listarMensajes(42L);

        assertEquals(1, historial.size());
        assertEquals(42L, historial.get(0).idConversacion());
        assertEquals(contenido, historial.get(0).contenido());
        verify(mensajes).findByIdConversacionOrderByFechaEnvioAscIdAsc(42L);
    }
}
