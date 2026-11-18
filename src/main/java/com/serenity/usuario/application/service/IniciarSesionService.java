package com.serenity.usuario.application.service;

import com.serenity.shared.security.JwtUtil;
import com.serenity.shared.exception.CredencialesInvalidasException;
import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.shared.exception.ConflictException;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.application.port.out.VerificarIdentidadGooglePort;
import com.serenity.usuario.domain.model.IdentidadGoogle;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.shared.consent.ConsentService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    private static final Logger log = LoggerFactory.getLogger(IniciarSesionService.class);

    private static final ZoneId ZONA_HORARIA =
            ZoneId.of("America/Bogota");

    private final UsuarioRepositoryPort repositorio;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final VerificarIdentidadGooglePort verificarIdentidadGoogle;
    private final EnviarCorreoTransaccionalUseCase correoTransaccional;
    private final ConsentService consentimientos;

    public IniciarSesionService(
            UsuarioRepositoryPort repositorio,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            VerificarIdentidadGooglePort verificarIdentidadGoogle,
            EnviarCorreoTransaccionalUseCase correoTransaccional,
            ConsentService consentimientos) {

        this.repositorio = repositorio;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.verificarIdentidadGoogle = verificarIdentidadGoogle;
        this.correoTransaccional = correoTransaccional;
        this.consentimientos = consentimientos;
    }

    @Override
    public ResultadoLogin iniciarSesion(String email, String passwordPlano) {
        log.info("login.stage=start");
        try {
            ResultadoLogin resultado = autenticar(email, passwordPlano);
            log.info("login.stage=complete outcome=success");
            return resultado;
        } catch (RuntimeException ex) {
            log.warn("login.stage=complete outcome=error exceptionType={}",
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private ResultadoLogin autenticar(String email, String passwordPlano) {

        if (email == null || email.isBlank()
                || passwordPlano == null || passwordPlano.isEmpty()) {
            throw new CredencialesInvalidasException();
        }

        Optional<Usuario> usuarioEncontrado;
        try {
            usuarioEncontrado = repositorio.buscarPorEmailIgnorandoMayusculas(email.strip());
        } catch (RuntimeException ex) {
            log.warn("login.stage=user_lookup outcome=error exceptionType={}",
                    ex.getClass().getSimpleName());
            throw ex;
        }
        boolean encontrado = usuarioEncontrado.isPresent();
        log.info("login.stage=user_lookup userFound={}", encontrado);
        Usuario usuario = usuarioEncontrado.orElseThrow(CredencialesInvalidasException::new);

        String passwordAlmacenada = usuario.getPasswordHash();
        boolean hashPresente = passwordAlmacenada != null && !passwordAlmacenada.isBlank();
        log.info("login.stage=password_hash hashPresent={}", hashPresente);

        if (!hashPresente) {
            throw new CredencialesInvalidasException();
        }

        boolean credencialesValidas;
        try {
            credencialesValidas = passwordEncoder.matches(
                    passwordPlano,
                    passwordAlmacenada
            );
        } catch (IllegalArgumentException ex) {
            log.warn("login.stage=bcrypt outcome=exception exceptionType={}",
                    ex.getClass().getSimpleName());
            credencialesValidas = false;
        } catch (RuntimeException ex) {
            log.warn("login.stage=bcrypt outcome=exception exceptionType={}",
                    ex.getClass().getSimpleName());
            throw ex;
        }
        log.info("login.stage=bcrypt bcryptMatched={}", credencialesValidas);

        if (!credencialesValidas) {
            throw new CredencialesInvalidasException();
        }

        log.info("login.stage=credentials outcome=accepted");
        return crearResultadoLogin(usuario, true);
    }

    @Override
    @Transactional
    public ResultadoLogin iniciarSesionConGoogle(String credential) {
        return iniciarSesionConGoogle(credential, null);
    }

    @Override
    @Transactional
    public ResultadoLogin iniciarSesionConGoogle(String credential, String username) {
        return iniciarSesionConGoogle(credential, username, null, null);
    }

    @Override
    @Transactional
    public ResultadoLogin iniciarSesionConGoogle(String credential, String username,
                                                  Boolean acceptTerms, Boolean acceptSensitiveData) {
        try {
            ResultadoLogin resultado = autenticarConGoogle(
                    credential, username, acceptTerms, Boolean.TRUE.equals(acceptSensitiveData));
            log.info("Inicio de sesión con Google exitoso para {}",
                    enmascararCorreo(resultado.usuario().getEmail()));
            return resultado;
        } catch (RuntimeException ex) {
            log.warn("Fallo de inicio de sesión con Google");
            throw ex;
        }
    }

    private ResultadoLogin autenticarConGoogle(String credential, String username,
                                               Boolean acceptTerms, boolean acceptSensitiveData) {

        IdentidadGoogle identidad = identidadGoogle(credential);

        Usuario usuario = repositorio
                .buscarPorGoogleSubject(identidad.subject())
                .orElseGet(() -> buscarOCrearUsuario(identidad, username, acceptTerms, acceptSensitiveData));

        return crearResultadoLogin(usuario);
    }

    @Override
    public void vincularGoogle(Long usuarioId, String credential) {

        IdentidadGoogle identidad = identidadGoogle(credential);

        Usuario usuario = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        Usuario vinculado = repositorio
                .buscarPorGoogleSubject(identidad.subject())
                .orElse(null);

        if (vinculado != null
                && !vinculado.getId().equals(usuarioId)) {
            throw new ConflictException(
                    "Esta cuenta de Google ya está vinculada a otro usuario"
            );
        }

        if (usuario.getGoogleSubject() != null
                && !usuario.getGoogleSubject().equals(identidad.subject())) {
            throw new ConflictException(
                    "La cuenta ya está vinculada a otra cuenta de Google"
            );
        }

        usuario.setGoogleSubject(identidad.subject());
        repositorio.guardar(usuario);
    }

    private Usuario buscarOCrearUsuario(IdentidadGoogle identidad, String username,
                                        Boolean acceptTerms, boolean acceptSensitiveData) {

        if (repositorio
                .buscarPorEmailIgnorandoMayusculas(identidad.email())
                .isPresent()) {
            throw new ConflictException(
                    "Ya existe una cuenta con este correo. "
                            + "Inicia sesión y vincula Google desde tu cuenta"
            );
        }

        if (!Boolean.TRUE.equals(acceptTerms)) {
            throw new IllegalArgumentException(
                    "Debes aceptar los términos y confirmar que fuiste informado de la Política de Tratamiento de Datos"
            );
        }

        Usuario nuevo = new Usuario(
                usernameElegido(username),
                identidad.email(),
                null
        );

        consentimientos.prepareRegistration(nuevo, acceptSensitiveData);

        nuevo.setGoogleSubject(identidad.subject());

        Usuario guardado = repositorio.guardar(nuevo);

        consentimientos.recordRegistration(guardado.getId(), acceptSensitiveData);

        correoTransaccional.enviarBienvenida(guardado);

        return guardado;
    }

    private String usernameElegido(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Elige un nombre de usuario para crear tu cuenta con Google desde Registro"
            );
        }

        String elegido = username.strip();
        int longitud = elegido.codePointCount(0, elegido.length());
        if (longitud < 3 || longitud > 50) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 3 y 50 caracteres"
            );
        }
        return elegido;
    }

    private IdentidadGoogle identidadGoogle(String credential) {

        if (credential == null || credential.isBlank()) {
            throw new CredencialesInvalidasException();
        }

        IdentidadGoogle identidad = verificarIdentidadGoogle
                .verificar(credential)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!identidad.emailVerificado()
                || identidad.subject() == null
                || identidad.subject().isBlank()
                || identidad.email() == null
                || identidad.email().isBlank()) {
            throw new CredencialesInvalidasException();
        }

        return identidad;
    }

    private ResultadoLogin crearResultadoLogin(Usuario usuario) {
        return crearResultadoLogin(usuario, false);
    }

    private ResultadoLogin crearResultadoLogin(Usuario usuario, boolean registrarDiagnostico) {

        if (usuario.estaRestringido(LocalDateTime.now(ZONA_HORARIA))) {
            if (registrarDiagnostico) {
                log.warn("login.stage=access_check outcome=restricted exceptionType={}",
                        AccesoRestringidoException.class.getSimpleName());
            }
            throw new AccesoRestringidoException(
                    usuario.getRestringidoHasta()
            );
        }

        if (registrarDiagnostico) {
            log.info("login.stage=jwt_generation outcome=started");
        }
        String token;
        try {
            token = jwtUtil.generarToken(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getRol()
            );
        } catch (RuntimeException ex) {
            if (registrarDiagnostico) {
                log.error("login.stage=jwt_generation outcome=error exceptionType={}",
                        ex.getClass().getSimpleName());
            }
            throw ex;
        }
        if (registrarDiagnostico) {
            log.info("login.stage=jwt_generation outcome=success");
        }

        return new ResultadoLogin(usuario, token);
    }

    private String enmascararCorreo(String email) {
        if (email == null || email.isBlank()) {
            return "desconocido";
        }
        String normalizado = email.strip();
        int separador = normalizado.indexOf('@');
        if (separador < 1) {
            return "***";
        }
        return normalizado.charAt(0) + "***@***";
    }
}
