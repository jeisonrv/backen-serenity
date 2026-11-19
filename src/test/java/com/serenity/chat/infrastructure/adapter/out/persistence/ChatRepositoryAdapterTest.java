package com.serenity.chat.infrastructure.adapter.out.persistence;

import com.serenity.chat.domain.model.MensajeChat;
import com.serenity.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    @Test
    void aceptarConcurrentementePorMismoOyenteEsIdempotente() {
        when(conversaciones.asignarOyente(42L, 8L)).thenReturn(0);
        when(conversaciones.findById(42L)).thenReturn(Optional.of(
                new ConversacionJpaEntity(42L, 11L, 8L, LocalDateTime.now(), null)));
        ChatRepositoryAdapter adapter = new ChatRepositoryAdapter(conversaciones, mensajes);

        assertDoesNotThrow(() -> adapter.asignarOyente(42L, 8L));
    }

    @Test
    void aceptarConcurrentementePorOtroOyenteSigueGenerandoConflicto() {
        when(conversaciones.asignarOyente(42L, 8L)).thenReturn(0);
        when(conversaciones.findById(42L)).thenReturn(Optional.of(
                new ConversacionJpaEntity(42L, 11L, 9L, LocalDateTime.now(), null)));
        ChatRepositoryAdapter adapter = new ChatRepositoryAdapter(conversaciones, mensajes);

        assertThrows(ConflictException.class, () -> adapter.asignarOyente(42L, 8L));
    }

    @Test
    void listaTodasLasConversacionesActivasSoloDelOyenteSolicitado() {
        when(conversaciones.findByIdOyenteAndFechaFinIsNullOrderByFechaInicioAsc(8L))
                .thenReturn(List.of(
                        new ConversacionJpaEntity(42L, 11L, 8L, LocalDateTime.now(), null),
                        new ConversacionJpaEntity(43L, 12L, 8L, LocalDateTime.now(), null)));
        ChatRepositoryAdapter adapter = new ChatRepositoryAdapter(conversaciones, mensajes);

        var activas = adapter.listarActivasDeOyente(8L);

        assertEquals(List.of(42L, 43L), activas.stream().map(c -> c.idConversacion()).toList());
        assertEquals(List.of(8L, 8L), activas.stream().map(c -> c.idOyente()).toList());
        verify(conversaciones).findByIdOyenteAndFechaFinIsNullOrderByFechaInicioAsc(8L);
    }

    @Test
    void finalizarSoloActualizaLaConversacionSolicitada() {
        ConversacionJpaEntity elegida = new ConversacionJpaEntity(42L, 11L, 8L, LocalDateTime.now(), null);
        when(conversaciones.buscarParticipante(42L, 8L)).thenReturn(Optional.of(elegida));
        ChatRepositoryAdapter adapter = new ChatRepositoryAdapter(conversaciones, mensajes);

        adapter.finalizar(42L, 8L);

        verify(conversaciones).save(elegida);
        verify(conversaciones, never()).save(argThat(c -> !c.getIdConversacion().equals(42L)));
        assertEquals(8L, elegida.getIdOyente());
        org.junit.jupiter.api.Assertions.assertNotNull(elegida.getFechaFin());
    }
}
