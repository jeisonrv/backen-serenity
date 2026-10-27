package com.serenity.notificacion.application.port.out;

public interface ProveedorCorreoPort {
    void enviar(String destinatario, String asunto, String contenidoHtml, String contenidoTexto);
}
