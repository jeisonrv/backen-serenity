package com.serenity.chat.infrastructure.adapter.out.persistence;

import com.serenity.chat.application.service.ChatService;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Rol;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-acceptance;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ChatRepositoryAdapter.class)
class ChatAcceptanceIntegrationTest {

    private static final Long OYENTE_A = 800L;
    private static final Long OYENTE_B = 801L;

    @Autowired
    private ConversacionJpaRepository conversaciones;

    @Autowired
    private ChatRepositoryAdapter chatRepository;

    @Test
    void oyentePuedeAceptarConversacionesOnceYDoceYSeConservaLaAsignacionExclusiva() {
        crearConversacionesPrevias(10);
        crearConversacion(101L);
        crearConversacion(102L);

        GestionarAccesoUseCase acceso = mock(GestionarAccesoUseCase.class);
        GestionarDisponibilidadUseCase disponibilidad = mock(GestionarDisponibilidadUseCase.class);
        when(acceso.verificarAcceso(OYENTE_A)).thenReturn(Rol.OYENTE);
        when(acceso.verificarAcceso(OYENTE_B)).thenReturn(Rol.OYENTE);
        when(disponibilidad.estaDisponible(OYENTE_A)).thenReturn(true);
        when(disponibilidad.estaDisponible(OYENTE_B)).thenReturn(true);

        ChatService chat = new ChatService(
                chatRepository,
                acceso,
                disponibilidad,
                mock(com.serenity.chat.application.port.out.ReporteChatRepositoryPort.class)
        );

        var primera = chat.aceptar(11L, OYENTE_A);
        var segunda = chat.aceptar(12L, OYENTE_A);

        assertEquals(11L, primera.idConversacion());
        assertEquals(12L, segunda.idConversacion());
        assertEquals(OYENTE_A, conversaciones.findById(11L).orElseThrow().getIdOyente());
        assertEquals(OYENTE_A, conversaciones.findById(12L).orElseThrow().getIdOyente());
        assertNull(conversaciones.findById(11L).orElseThrow().getFechaFin());
        assertNull(conversaciones.findById(12L).orElseThrow().getFechaFin());
        assertEquals(
                List.of(11L, 12L),
                chat.listarActivasDeOyente(OYENTE_A).stream()
                        .map(c -> c.idConversacion())
                        .toList()
        );

        assertEquals(11L, chat.aceptar(11L, OYENTE_A).idConversacion());
        assertThrows(ConflictException.class, () -> chat.aceptar(12L, OYENTE_B));

        assertEquals(OYENTE_A, conversaciones.findById(12L).orElseThrow().getIdOyente());
        assertNull(conversaciones.findById(12L).orElseThrow().getFechaFin());

        chat.finalizar(11L, OYENTE_A);
        assertThrows(ConflictException.class, () -> chat.aceptar(11L, OYENTE_A));
        assertEquals(
                List.of(12L),
                chat.listarActivasDeOyente(OYENTE_A).stream()
                        .map(c -> c.idConversacion())
                        .toList()
        );
    }

    private void crearConversacionesPrevias(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            crearConversacion(100L + i);
        }
    }

    private void crearConversacion(Long idUsuario) {
        conversaciones.saveAndFlush(
                new ConversacionJpaEntity(null, idUsuario, null, LocalDateTime.now(), null)
        );
    }
}
