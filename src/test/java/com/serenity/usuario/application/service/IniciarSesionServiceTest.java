package com.serenity.usuario.application.service;

import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.CredencialesInvalidasException;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.application.port.out.VerificarIdentidadGooglePort;
import com.serenity.usuario.domain.model.IdentidadGoogle;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IniciarSesionServiceTest {
    @Mock UsuarioRepositoryPort usuarios;
    @Mock JwtUtil jwt;
    @Mock PasswordEncoder encoder;
    @Mock VerificarIdentidadGooglePort google;
    @Mock EnviarCorreoTransaccionalUseCase correo;
    IniciarSesionService servicio;

    @BeforeEach void crearServicio() { servicio = new IniciarSesionService(usuarios, jwt, encoder, google, correo); }

    @Test void emailNuloBlancoYPasswordNulaOVaciaSonInvalidos() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(null, "x"));
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(" ", "x"));
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("a@b.co", null));
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("a@b.co", ""));
    }

    @Test void usuarioInexistenteEsInvalido() {
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("a@b.co", "pass"));
    }

    @Test void passwordIncorrectaEsInvalida() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "$2hash", 1, 0, 0, 0);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
        when(encoder.matches("mala", "$2hash")).thenReturn(false);
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("a@b.co", "mala"));
    }

    @Test void usuarioRestringidoNoPuedeIniciarSesion() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "$2hash", 1, 0, 0, 0);
        usuario.restringir("motivo", LocalDateTime.now().plusDays(1), LocalDateTime.now(), 2L);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
        when(encoder.matches("bien", "$2hash")).thenReturn(true);
        assertThrows(AccesoRestringidoException.class, () -> servicio.iniciarSesion("a@b.co", "bien"));
    }

    @Test void inicioExitosoDevuelveTokenJwt() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "$2hash", 1, 0, 0, 0);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
        when(encoder.matches("bien", "$2hash")).thenReturn(true);
        when(jwt.generarToken(1L, "a@b.co")).thenReturn("jwt-firmado");
        var resultado = servicio.iniciarSesion(" a@b.co ", "bien");
        assertSame(usuario, resultado.usuario());
        assertEquals("jwt-firmado", resultado.token());
    }

    @Test void googleCreaUsuarioYDevuelveToken() {
        var identidad = new IdentidadGoogle("sub-1", "ana@b.co", true, "Ana Pérez");
        when(google.verificar("credential")).thenReturn(Optional.of(identidad));
        when(usuarios.buscarPorGoogleSubject("sub-1")).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmailIgnorandoMayusculas("ana@b.co")).thenReturn(Optional.empty());
        when(usuarios.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generarToken(any(), eq("ana@b.co"))).thenReturn("jwt-google");
        var resultado = servicio.iniciarSesionConGoogle("credential");
        assertEquals("jwt-google", resultado.token());
        assertEquals("Ana_Pérez", resultado.usuario().getUsername());
        verify(correo).enviarBienvenida(resultado.usuario());
    }
}
