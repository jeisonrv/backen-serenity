package com.serenity.usuario.application.service;

import com.serenity.shared.security.JwtUtil;
import com.serenity.shared.exception.CredencialesInvalidasException;
import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.application.port.out.VerificarIdentidadGooglePort;
import com.serenity.usuario.domain.model.IdentidadGoogle;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.shared.exception.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

    private final UsuarioRepositoryPort repositorio;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final VerificarIdentidadGooglePort verificarIdentidadGoogle;
    private final EnviarCorreoTransaccionalUseCase correoTransaccional;

    public IniciarSesionService(UsuarioRepositoryPort repositorio, JwtUtil jwtUtil,
                                PasswordEncoder passwordEncoder,
                                VerificarIdentidadGooglePort verificarIdentidadGoogle,
                                EnviarCorreoTransaccionalUseCase correoTransaccional) {
        this.repositorio = repositorio;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.verificarIdentidadGoogle = verificarIdentidadGoogle;
        this.correoTransaccional = correoTransaccional;
    }

    @Override
    public ResultadoLogin iniciarSesion(String email, String passwordPlano) {
        Usuario usuario = repositorio.buscarPorEmail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        String passwordAlmacenada = usuario.getPasswordHash();
        boolean credencialesValidas;
        if (passwordAlmacenada != null && passwordAlmacenada.startsWith("$2")) {
            credencialesValidas = passwordEncoder.matches(passwordPlano, passwordAlmacenada);
        } else {
            // Migra al primer inicio de sesión los registros heredados sin hash.
            credencialesValidas = passwordPlano.equals(passwordAlmacenada);
            if (credencialesValidas) {
                usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
                repositorio.guardar(usuario);
            }
        }
        if (!credencialesValidas) {
            throw new CredencialesInvalidasException();
        }
        return crearResultadoLogin(usuario);
    }

    @Override
    public ResultadoLogin iniciarSesionConGoogle(String credential) {
        IdentidadGoogle identidad = identidadGoogle(credential);

        Usuario usuario = repositorio.buscarPorGoogleSubject(identidad.subject())
                .orElseGet(() -> buscarOCrearUsuario(identidad));
        return crearResultadoLogin(usuario);
    }

    @Override
    public void vincularGoogle(Long usuarioId, String credential) {
        IdentidadGoogle identidad = identidadGoogle(credential);
        Usuario usuario = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Usuario vinculado = repositorio.buscarPorGoogleSubject(identidad.subject()).orElse(null);
        if (vinculado != null && !vinculado.getId().equals(usuarioId)) {
            throw new ConflictException("Esta cuenta de Google ya está vinculada a otro usuario");
        }
        if (usuario.getGoogleSubject() != null && !usuario.getGoogleSubject().equals(identidad.subject())) {
            throw new ConflictException("La cuenta ya está vinculada a otra cuenta de Google");
        }
        usuario.setGoogleSubject(identidad.subject());
        repositorio.guardar(usuario);
    }

    private Usuario buscarOCrearUsuario(IdentidadGoogle identidad) {
        if (repositorio.buscarPorEmailIgnorandoMayusculas(identidad.email()).isPresent()) {
            throw new ConflictException(
                    "Ya existe una cuenta con este correo. Inicia sesión y vincula Google desde tu cuenta");
        }
        Usuario nuevo = new Usuario(nombreUsuario(identidad), identidad.email(), null);
        nuevo.setGoogleSubject(identidad.subject());
        Usuario guardado = repositorio.guardar(nuevo);
        correoTransaccional.enviarBienvenida(guardado);
        return guardado;
    }

    private IdentidadGoogle identidadGoogle(String credential) {
        IdentidadGoogle identidad = verificarIdentidadGoogle.verificar(credential)
                .orElseThrow(CredencialesInvalidasException::new);
        if (!identidad.emailVerificado() || identidad.subject() == null || identidad.subject().isBlank()
                || identidad.email() == null || identidad.email().isBlank()) {
            throw new CredencialesInvalidasException();
        }
        return identidad;
    }

    private String nombreUsuario(IdentidadGoogle identidad) {
        String nombre = identidad.nombre() == null ? "" : identidad.nombre().strip();
        if (nombre.isBlank()) {
            int separador = identidad.email().indexOf('@');
            nombre = separador > 0 ? identidad.email().substring(0, separador) : "google_user";
        }
        nombre = nombre.replaceAll("\\s+", "_");
        if (nombre.codePointCount(0, nombre.length()) < 3) nombre = "g_" + nombre;
        if (nombre.codePointCount(0, nombre.length()) > 50) {
            nombre = nombre.substring(0, nombre.offsetByCodePoints(0, 50));
        }
        return nombre;
    }

    private ResultadoLogin crearResultadoLogin(Usuario usuario) {
        if (usuario.estaRestringido(LocalDateTime.now(ZONA_HORARIA))) {
            throw new AccesoRestringidoException(usuario.getRestringidoHasta());
        }
        String token = jwtUtil.generarToken(usuario.getId(), usuario.getEmail());
        return new ResultadoLogin(usuario, token);
    }
}
