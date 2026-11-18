package com.serenity.shared.config;

import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Rol;
import com.serenity.usuario.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Idempotent seeder that creates the institutional Serenity accounts
 * (OYENTE and MODERADOR) on application startup if they do not exist.
 *
 * <p>Initial passwords are supplied through deployment secrets. If a password
 * is not configured, a missing account is not created and a warning is logged.
 *
 * <p>The seeder NEVER overwrites an existing password. Existing institutional
 * accounts only have their role corrected when necessary.
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code SERENITY_SEED_OYENTE_PASSWORD} – initial listener password</li>
 *   <li>{@code SERENITY_SEED_MODERADOR_PASSWORD} – initial moderator password</li>
 * </ul>
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String OYENTE_EMAIL = "oyente.serenity@example.com";
    private static final String MODERADOR_EMAIL = "moderador.serenity@example.com";

    private final UsuarioRepositoryPort repositorio;
    private final PasswordEncoder passwordEncoder;
    private final String oyentePassword;
    private final String moderadorPassword;

    public DataSeeder(
            UsuarioRepositoryPort repositorio,
            PasswordEncoder passwordEncoder,
            @Value("${serenity.seed.oyente.password:}") String oyentePassword,
            @Value("${serenity.seed.moderador.password:}") String moderadorPassword) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.oyentePassword = oyentePassword;
        this.moderadorPassword = moderadorPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedCuenta(OYENTE_EMAIL, oyentePassword, Rol.OYENTE, "oyente");
        seedCuenta(MODERADOR_EMAIL, moderadorPassword, Rol.MODERADOR, "moderador");
    }

    private void seedCuenta(String email, String passwordPlano, Rol rol, String tipo) {
        Optional<Usuario> existente = repositorio.buscarPorEmailIgnorandoMayusculas(email);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (usuario.getRol() != rol) {
                corregirRolSiSeVerifica(usuario, passwordPlano, rol, email);
            } else {
                log.info("DataSeeder: cuenta institucional {} ya existe; no se modifica.",
                        mask(email));
            }
            return;
        }

        if (passwordPlano == null || passwordPlano.isBlank()) {
            log.warn("DataSeeder: {} no fue creada porque falta la variable SERENITY_SEED_{}_PASSWORD.",
                    tipo, tipo.toUpperCase());
            return;
        }

        Usuario nuevo = new Usuario(
                "serenity." + tipo,
                email,
                passwordEncoder.encode(passwordPlano)
        );
        nuevo.setRol(rol);

        if (repositorio.guardarSiNoExiste(nuevo)) {
            log.info("DataSeeder: cuenta institucional {} creada con rol {}.",
                    mask(email), rol);
            return;
        }

        Usuario creadaEnParalelo = repositorio.buscarPorEmailIgnorandoMayusculas(email)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo crear ni localizar la cuenta institucional " + mask(email)));
        if (creadaEnParalelo.getRol() != rol) {
            corregirRolSiSeVerifica(creadaEnParalelo, passwordPlano, rol, email);
        }
        log.info("DataSeeder: cuenta institucional {} ya existe; contraseña preservada.",
                mask(email));
    }

    private void corregirRolSiSeVerifica(
            Usuario usuario, String passwordPlano, Rol rol, String email) {
        String passwordHash = usuario.getPasswordHash();
        boolean credencialVerificada = false;
        if (passwordPlano != null && !passwordPlano.isBlank()
                && passwordHash != null && !passwordHash.isBlank()) {
            try {
                credencialVerificada = passwordEncoder.matches(passwordPlano, passwordHash);
            } catch (IllegalArgumentException ex) {
                log.warn("DataSeeder: el hash de la cuenta institucional {} no es válido.",
                        mask(email));
            }
        }

        if (!credencialVerificada) {
            log.error("DataSeeder: la cuenta institucional {} tiene un rol distinto de {}. " +
                            "No se cambia el rol sin verificar la contraseña configurada.",
                    mask(email), rol);
            return;
        }

        usuario.setRol(rol);
        repositorio.guardar(usuario);
        log.warn("DataSeeder: rol de la cuenta institucional {} corregido a {}.",
                mask(email), rol);
    }

    /** Mask email for safe logging: a***@domain.tld */
    private static String mask(String email) {
        if (email == null || email.isBlank()) return "???";
        int at = email.indexOf('@');
        if (at < 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
