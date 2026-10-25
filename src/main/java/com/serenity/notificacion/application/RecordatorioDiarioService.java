package com.serenity.notificacion.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import com.serenity.notificacion.infrastructure.persistence.RecordatorioEnviadoJpaEntity;
import com.serenity.notificacion.infrastructure.persistence.RecordatorioEnviadoJpaRepository;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import com.serenity.ejercicio.infrastructure.adapter.out.persistence.SesionJpaRepository;

@Service
public class RecordatorioDiarioService {

    private static final Logger log =
            LoggerFactory.getLogger(RecordatorioDiarioService.class);

    private static final ZoneId ZONA_COLOMBIA =
            ZoneId.of("America/Bogota");

    private final UsuarioJpaRepository usuarioRepository;
    private final SesionJpaRepository sesionRepository;
    private final RecordatorioEnviadoJpaRepository recordatorioRepository;
    private final ProveedorCorreoPort proveedorCorreo;

    public RecordatorioDiarioService(
            UsuarioJpaRepository usuarioRepository,
            SesionJpaRepository sesionRepository,
            RecordatorioEnviadoJpaRepository recordatorioRepository,
            ProveedorCorreoPort proveedorCorreo) {

        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.proveedorCorreo = proveedorCorreo;
    }

    public void enviarRecordatoriosPendientes() {

        LocalDate hoy = LocalDate.now(ZONA_COLOMBIA);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();

        List<UsuarioJpaEntity> usuarios =
                usuarioRepository.findByRecordatoriosEmailTrue();

        log.info("Usuarios con recordatorios habilitados: {}", usuarios.size());

        for (UsuarioJpaEntity usuario : usuarios) {
            try {
                procesarUsuario(usuario, hoy, inicio, fin);
            } catch (Exception e) {
                log.error(
                        "Error al enviar recordatorio al usuario {}",
                        usuario.getId(),
                        e
                );
            }
        }
    }

    private void procesarUsuario(
            UsuarioJpaEntity usuario,
            LocalDate hoy,
            LocalDateTime inicio,
            LocalDateTime fin) {

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return;
        }

        if (recordatorioRepository.existsByUsuarioIdAndFecha(
                usuario.getId(), hoy)) {
            log.debug(
                    "El usuario {} ya recibió su recordatorio de hoy",
                    usuario.getId()
            );
            return;
        }

        boolean hizoEjercicio =
                sesionRepository
                        .existsByUsuarioIdAndFechaCompletadoGreaterThanEqualAndFechaCompletadoLessThan(
                                usuario.getId(),
                                inicio,
                                fin
                        );

        if (hizoEjercicio) {
            log.debug(
                    "El usuario {} ya completó un ejercicio hoy",
                    usuario.getId()
            );
            return;
        }

        String nombre = usuario.getUsername() == null
                || usuario.getUsername().isBlank()
                ? "Serenity"
                : usuario.getUsername();

        String asunto = "Tu momento de bienestar te espera 💜";

        String contenidoHtml = """
                <div style="font-family:Arial,sans-serif;
                            max-width:600px;margin:auto;color:#333;
                            line-height:1.6">
                    <h1 style="color:#6c63ff">¡Hola, %s!</h1>
                    <p>
                        Tu momento de bienestar te espera en Serenity.
                    </p>
                    <p>
                        Dedica unos minutos a realizar uno de tus ejercicios
                        de hoy y continúa construyendo tu hábito de bienestar.
                    </p>
                    <p>Recuerda: cada pequeño paso cuenta.</p>
                    <p>¡Te esperamos en Serenity!</p>
                    <p style="font-size:12px;color:#777">
                        Recibes este mensaje porque habilitaste los
                        recordatorios por correo.
                    </p>
                </div>
                """.formatted(escaparHtml(nombre));

        String contenidoTexto =
                "¡Hola, " + nombre + "!\n\n"
                        + "Tu momento de bienestar te espera en Serenity.\n"
                        + "Dedica unos minutos a completar un ejercicio de hoy "
                        + "y continúa construyendo tu hábito de bienestar.\n\n"
                        + "Recuerda: cada pequeño paso cuenta.\n\n"
                        + "¡Te esperamos en Serenity!";

        // Primero se envía el correo. Solo se registra si el proveedor
        // acepta la solicitud correctamente.
        proveedorCorreo.enviar(
                usuario.getEmail(),
                asunto,
                contenidoHtml,
                contenidoTexto
        );

        try {
            recordatorioRepository.saveAndFlush(
                    new RecordatorioEnviadoJpaEntity(
                            usuario.getId(),
                            hoy,
                            LocalDateTime.now(ZONA_COLOMBIA)
                    )
            );

            log.info(
                    "Recordatorio enviado al usuario {}",
                    usuario.getId()
            );

        } catch (DataIntegrityViolationException e) {
            log.warn(
                    "Ya existe un registro para el usuario {} en {}",
                    usuario.getId(),
                    hoy
            );
        }
    }

    private String escaparHtml(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}