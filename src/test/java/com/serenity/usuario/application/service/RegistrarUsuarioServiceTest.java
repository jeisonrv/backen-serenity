package com.serenity.usuario.application.service;

import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {
    @Mock UsuarioRepositoryPort usuarios;
    @Mock PasswordEncoder encoder;
    @Mock EnviarCorreoTransaccionalUseCase correo;

    @Test void emailDuplicadoLanzaConflictException() {
        when(usuarios.buscarPorEmail("ana@b.co")).thenReturn(Optional.of(new Usuario()));
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo);
        assertThrows(ConflictException.class, () -> servicio.registrar("ana", "ana@b.co", "secreto"));
        verify(usuarios, never()).guardar(any());
    }

    @Test void registroExitosoGuardaUsuarioHasheadoYEnviaBienvenida() {
        when(usuarios.buscarPorEmail("ana@b.co")).thenReturn(Optional.empty());
        when(encoder.encode("secreto")).thenReturn("$2hash");
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo);
        Usuario guardado = servicio.registrar("ana", "ana@b.co", "secreto");
        assertEquals("$2hash", guardado.getPasswordHash());
        verify(correo).enviarBienvenida(guardado);
    }

    @Test void camposObligatoriosNulosOVaciosSonRechazadosAntesDeConsultarRepositorio() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(null, "ana@b.co", "secreto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(" ", "ana@b.co", "secreto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", null, "secreto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", " ", "secreto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@b.co", null));
        verifyNoInteractions(usuarios, encoder, correo);
    }

    @Test void correoSinArrobaODominioConPuntoEsRechazado() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "sin-arroba.test", "secreto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@dominio", "secreto"));
        verifyNoInteractions(usuarios, encoder, correo);
    }

    @Test void passwordMenorASeisCaracteresEsRechazada() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@b.co", "12345"));
        verifyNoInteractions(usuarios, encoder, correo);
    }
}
