package com.serenity.notificacion.infrastructure.adapter.out.resend;

import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class ResendCorreoAdapter implements ProveedorCorreoPort {
    private final RestClient cliente;
    private final String apiKey;
    private final String remitente;

    public ResendCorreoAdapter(
            RestClient.Builder builder,
            @Value("${app.correo.resend.api-key:}") String apiKey,
            @Value("${app.correo.resend.remitente:}") String remitente) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.cliente = builder
                .baseUrl("https://api.resend.com")
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String contenidoHtml, String contenidoTexto) {
        if (apiKey == null || apiKey.isBlank() || remitente == null || remitente.isBlank()) {
            throw new IllegalStateException("Falta configurar RESEND_API_KEY o RESEND_FROM");
        }

        cliente.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "from", remitente,
                        "to", List.of(destinatario),
                        "subject", asunto,
                        "html", contenidoHtml,
                        "text", contenidoTexto))
                .retrieve()
                .toBodilessEntity();
    }
}
