package com.serenity.notificacion.application.service;

import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;
import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import com.serenity.usuario.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnviarCorreoTransaccionalService implements EnviarCorreoTransaccionalUseCase {
    private static final Logger log = LoggerFactory.getLogger(EnviarCorreoTransaccionalService.class);

    private final ProveedorCorreoPort proveedor;
    private final boolean habilitado;

    public EnviarCorreoTransaccionalService(
            ProveedorCorreoPort proveedor,
            @Value("${app.correo.habilitado:false}") boolean habilitado) {
        this.proveedor = proveedor;
        this.habilitado = habilitado;
    }

    @Override
    public void enviarBienvenida(Usuario usuario) {
        if (!habilitado) return;

        String nombre = usuario.getUsername() == null ? "" : usuario.getUsername();
        String nombreHtml = escaparHtml(nombre);
        String html = "<h1>Bienvenido a Serenity</h1>"
                + "<p>Hola " + nombreHtml + ",</p>"
                + "<p>Tu cuenta está lista. Nos alegra acompañarte en tu camino de bienestar.</p>";
        String texto = "Bienvenido a Serenity, " + nombre
                + ". Tu cuenta está lista. Nos alegra acompañarte en tu camino de bienestar.";

        try {
            proveedor.enviar(usuario.getEmail(), "Bienvenido a Serenity", html, texto);
        } catch (RuntimeException ex) {
            // El proveedor de correo no debe interrumpir el registro de una cuenta.
            log.warn("No se pudo enviar el correo transaccional de bienvenida; el registro continúa");
        }
    }

    private String escaparHtml(String valor) {
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
