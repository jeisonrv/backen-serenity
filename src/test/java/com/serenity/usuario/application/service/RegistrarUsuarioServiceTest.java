package com.serenity.usuario.application.service;

import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.consent.ConsentService;
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
    @Mock ConsentService consentimientos;

    @Test void emailDuplicadoLanzaConflictException() {
        when(usuarios.buscarPorEmail("ana@b.co")).thenReturn(Optional.of(new Usuario()));
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);
        assertThrows(ConflictException.class, () -> servicio.registrar("ana", "ana@b.co", "secreto", true, false));
        verify(usuarios, never()).guardar(any());
    }

    @Test void registroExitosoGuardaUsuarioHasheadoYEnviaBienvenida() {
        when(usuarios.buscarPorEmail("ana@b.co")).thenReturn(Optional.empty());
        when(encoder.encode("secreto")).thenReturn("$2hash");
        when(usuarios.guardar(any())).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setId(10L);
            return usuario;
        });
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);
        Usuario guardado = servicio.registrar("ana", "ana@b.co", "secreto", true, false);
        assertEquals("$2hash", guardado.getPasswordHash());
        verify(correo).enviarBienvenida(guardado);
        verify(consentimientos).prepareRegistration(any(Usuario.class), eq(false));
        verify(consentimientos).recordRegistration(10L, false);
    }

    @Test void camposObligatoriosNulosOVaciosSonRechazadosAntesDeConsultarRepositorio() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(null, "ana@b.co", "secreto", true, false));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(" ", "ana@b.co", "secreto", true, false));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", null, "secreto", true, false));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", " ", "secreto", true, false));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@b.co", null, true, false));
        verifyNoInteractions(usuarios, encoder, correo, consentimientos);
    }

    @Test void registroSinAceptarTerminosSeRechazaConMensajeClaro() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);
        var error = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar("ana", "ana@b.co", "secreto", false, true));
        assertTrue(error.getMessage().contains("Debes aceptar los términos"));
        verifyNoInteractions(usuarios, encoder, correo, consentimientos);
    }

    @Test void registroConAmbosConsentimientosRegistraLaAutorizacionFacultativa() {
        when(usuarios.buscarPorEmail("ana@b.co")).thenReturn(Optional.empty());
        when(encoder.encode("secreto")).thenReturn("$2hash");
        when(usuarios.guardar(any())).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setId(10L);
            return usuario;
        });
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);

        servicio.registrar("ana", "ana@b.co", "secreto", true, true);

        verify(consentimientos).prepareRegistration(any(Usuario.class), eq(true));
        verify(consentimientos).recordRegistration(10L, true);
    }

    @Test void correoSinArrobaODominioConPuntoEsRechazado() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "sin-arroba.test", "secreto", true, false));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@dominio", "secreto", true, false));
        verifyNoInteractions(usuarios, encoder, correo, consentimientos);
    }

    @Test void passwordMenorASeisCaracteresEsRechazada() {
        var servicio = new RegistrarUsuarioService(usuarios, encoder, correo, consentimientos);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("ana", "ana@b.co", "12345", true, false));
        verifyNoInteractions(usuarios, encoder, correo, consentimientos);
    }

}
