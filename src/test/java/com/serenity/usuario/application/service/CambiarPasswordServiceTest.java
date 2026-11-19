package com.serenity.usuario.application.service;

import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CambiarPasswordServiceTest {

    @Test
    void cuentaSoloGooglePuedeCrearPasswordHasheadaConBcrypt() {
        UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        CambiarPasswordService servicio = new CambiarPasswordService(usuarios, encoder);
        Usuario cuentaGoogle = new Usuario(7L, "ana", "ana@example.test", null, 1, 0, 0, 0);
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(cuentaGoogle));
        when(usuarios.guardar(cuentaGoogle)).thenReturn(cuentaGoogle);

        servicio.crear(7L, "password-segura");

        assertNotEquals("password-segura", cuentaGoogle.getPasswordHash());
        assertTrue(encoder.matches("password-segura", cuentaGoogle.getPasswordHash()));
        verify(usuarios).guardar(cuentaGoogle);
    }

    @Test
    void noPermiteReemplazarUnaPasswordYaConfigurada() {
        UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
        CambiarPasswordService servicio = new CambiarPasswordService(
                usuarios, new BCryptPasswordEncoder());
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(
                new Usuario(7L, "ana", "ana@example.test", "existing-hash", 1, 0, 0, 0)));

        assertThrows(ConflictException.class, () -> servicio.crear(7L, "password-segura"));
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void validaLongitudYExistenciaDeCuentaAntesDeGuardar() {
        UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
        CambiarPasswordService servicio = new CambiarPasswordService(
                usuarios, new BCryptPasswordEncoder());

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(7L, "short"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(7L, "a".repeat(73)));
        assertThrows(ResourceNotFoundException.class, () -> servicio.crear(7L, "password-segura"));
        verify(usuarios, never()).guardar(any());
    }
}
