package com.serenity.usuario.application.service;

import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.exception.CredencialesInvalidasException;
import com.serenity.shared.exception.ResourceNotFoundException;
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

    @Test void correoSinFormatoEmailNoEncuentraCuentaYSeRechaza() {
        when(usuarios.buscarPorEmail("sin-formato")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("sin-formato", "pass"));
    }

    @Test void passwordIncorrectaEsInvalida() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "$2hash", 1, 0, 0, 0);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
        when(encoder.matches("mala", "$2hash")).thenReturn(false);
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("a@b.co", "mala"));
    }

    @Test void passwordAlmacenadaNulaOVaciaEsInvalida() {
        for (String almacenada : new String[]{null, "  "}) {
            Usuario usuario = new Usuario(1L, "ana", "a@b.co", almacenada, 1, 0, 0, 0);
            when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
            assertThrows(CredencialesInvalidasException.class,
                    () -> servicio.iniciarSesion("a@b.co", "cualquier-clave"));
        }
        verifyNoInteractions(encoder, jwt);
    }

    @Test void passwordEnTextoPlanoSeMigraABcryptCuandoCoincide() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "clave-antigua", 1, 0, 0, 0);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));
        when(encoder.encode("clave-antigua")).thenReturn("$2nuevo-hash");
        when(jwt.generarToken(1L, "a@b.co")).thenReturn("jwt-firmado");

        var resultado = servicio.iniciarSesion("a@b.co", "clave-antigua");

        assertEquals("$2nuevo-hash", usuario.getPasswordHash());
        assertEquals("jwt-firmado", resultado.token());
        verify(usuarios).guardar(usuario);
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test void passwordEnTextoPlanoIncorrectaNoGuardaUsuario() {
        Usuario usuario = new Usuario(1L, "ana", "a@b.co", "clave-antigua", 1, 0, 0, 0);
        when(usuarios.buscarPorEmail("a@b.co")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("a@b.co", "clave-incorrecta"));
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(encoder, jwt);
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

    @Test void googleIniciaSesionConUsuarioYaVinculadoSinCrearCuentaNueva() {
        var identidad = new IdentidadGoogle("sub-existente", "ana@b.co", true, "Ana");
        Usuario existente = new Usuario(8L, "ana", "ana@b.co", null, 1, 0, 0, 0);
        when(google.verificar("credential")).thenReturn(Optional.of(identidad));
        when(usuarios.buscarPorGoogleSubject("sub-existente")).thenReturn(Optional.of(existente));
        when(jwt.generarToken(8L, "ana@b.co")).thenReturn("jwt-google");

        var resultado = servicio.iniciarSesionConGoogle("credential");

        assertSame(existente, resultado.usuario());
        assertEquals("jwt-google", resultado.token());
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(correo);
    }

    @Test void googleRechazaCredencialVaciaOIdentidadNoVerificada() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesionConGoogle(" "));
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", false, "Ana")));
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesionConGoogle("credential"));
        verifyNoInteractions(usuarios, jwt, correo);
    }

    @Test void googleRechazaIdentidadSinSubjectOCorreo() {
        when(google.verificar("subject-nulo")).thenReturn(Optional.of(
                new IdentidadGoogle(null, "ana@b.co", true, "Ana")));
        when(google.verificar("sin-subject")).thenReturn(Optional.of(
                new IdentidadGoogle(" ", "ana@b.co", true, "Ana")));
        when(google.verificar("sin-email")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", null, true, "Ana")));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesionConGoogle("subject-nulo"));
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesionConGoogle("sin-subject"));
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesionConGoogle("sin-email"));
        verifyNoInteractions(usuarios, jwt, correo);
    }

    @Test void googleRechazaRespuestaDelVerificadorSinIdentidad() {
        when(google.verificar("credential")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesionConGoogle("credential"));
        verifyNoInteractions(usuarios, jwt, correo);
    }

    @Test void googleRechazaUnaCuentaConEmailYaRegistrado() {
        var identidad = new IdentidadGoogle("sub-nuevo", "ana@b.co", true, "Ana");
        when(google.verificar("credential")).thenReturn(Optional.of(identidad));
        when(usuarios.buscarPorGoogleSubject("sub-nuevo")).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmailIgnorandoMayusculas("ana@b.co"))
                .thenReturn(Optional.of(new Usuario()));

        assertThrows(ConflictException.class,
                () -> servicio.iniciarSesionConGoogle("credential"));
        verify(usuarios, never()).guardar(any());
    }

    @Test void vincularGoogleGuardaSubjectEnCuentaSinVinculo() {
        Usuario usuario = new Usuario(1L, "ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", true, "Ana")));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.buscarPorGoogleSubject("sub")).thenReturn(Optional.empty());

        servicio.vincularGoogle(1L, "credential");

        assertEquals("sub", usuario.getGoogleSubject());
        verify(usuarios).guardar(usuario);
    }

    @Test void vincularGoogleRechazaUsuarioInexistenteYSubjectDeOtraCuenta() {
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", true, "Ana")));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> servicio.vincularGoogle(1L, "credential"));

        Usuario usuario = new Usuario(1L, "ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        Usuario vinculado = new Usuario(2L, "otra", "otra@b.co", "$2hash", 1, 0, 0, 0);
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.buscarPorGoogleSubject("sub")).thenReturn(Optional.of(vinculado));
        assertThrows(ConflictException.class,
                () -> servicio.vincularGoogle(1L, "credential"));
        verify(usuarios, never()).guardar(any());
    }

    @Test void vincularGoogleRechazaCambioDeVinculoActualPeroPermiteMismoSubject() {
        Usuario usuario = new Usuario(1L, "ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        usuario.setGoogleSubject("sub-anterior");
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", true, "Ana")));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.buscarPorGoogleSubject("sub")).thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> servicio.vincularGoogle(1L, "credential"));
        usuario.setGoogleSubject("sub");
        servicio.vincularGoogle(1L, "credential");
        verify(usuarios).guardar(usuario);
    }

    @Test void vincularGoogleRechazaSubjectVinculadoAOtroUsuario() {
        Usuario usuario = new Usuario(1L, "ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        Usuario otro = new Usuario(2L, "otra", "otra@b.co", "$2hash", 1, 0, 0, 0);
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", true, "Ana")));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.buscarPorGoogleSubject("sub")).thenReturn(Optional.of(otro));

        assertThrows(ConflictException.class,
                () -> servicio.vincularGoogle(1L, "credential"));
        verify(usuarios, never()).guardar(any());
    }

    @Test void nombreGoogleAusenteUsaPrefijoDeCorreoYLimitaNombresLargos() {
        var identidadNombreAusente = new IdentidadGoogle("sub-corto", "a@b.co", true, null);
        var identidadNombreLargo = new IdentidadGoogle("sub-largo", "larga@b.co", true, "A".repeat(55));
        var identidadCorreoSinSeparador = new IdentidadGoogle("sub-sin-email", "sin-separador", true, null);
        when(google.verificar("corto")).thenReturn(Optional.of(identidadNombreAusente));
        when(google.verificar("largo")).thenReturn(Optional.of(identidadNombreLargo));
        when(google.verificar("sin-separador")).thenReturn(Optional.of(identidadCorreoSinSeparador));
        when(usuarios.buscarPorGoogleSubject("sub-corto")).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmailIgnorandoMayusculas("a@b.co")).thenReturn(Optional.empty());
        when(usuarios.buscarPorGoogleSubject("sub-largo")).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmailIgnorandoMayusculas("larga@b.co")).thenReturn(Optional.empty());
        when(usuarios.buscarPorGoogleSubject("sub-sin-email")).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmailIgnorandoMayusculas("sin-separador")).thenReturn(Optional.empty());
        when(usuarios.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generarToken(any(), anyString())).thenReturn("jwt-firmado");

        var corto = servicio.iniciarSesionConGoogle("corto");
        var largo = servicio.iniciarSesionConGoogle("largo");
        var sinSeparador = servicio.iniciarSesionConGoogle("sin-separador");

        assertEquals("g_a", corto.usuario().getUsername());
        assertEquals(50, largo.usuario().getUsername().codePointCount(0, largo.usuario().getUsername().length()));
        assertEquals("google_user", sinSeparador.usuario().getUsername());
        verify(correo).enviarBienvenida(corto.usuario());
        verify(correo).enviarBienvenida(largo.usuario());
        verify(correo).enviarBienvenida(sinSeparador.usuario());
    }

    @Test void vincularGooglePermiteElSubjectYaVinculadoAlMismoUsuario() {
        Usuario usuario = new Usuario(1L, "ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        usuario.setGoogleSubject("sub");
        when(google.verificar("credential")).thenReturn(Optional.of(
                new IdentidadGoogle("sub", "ana@b.co", true, "Ana")));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.buscarPorGoogleSubject("sub")).thenReturn(Optional.of(usuario));

        servicio.vincularGoogle(1L, "credential");

        verify(usuarios).guardar(usuario);
    }
}
