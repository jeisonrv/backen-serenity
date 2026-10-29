package com.serenity.frase.infrastructure.adapter.out.motivationalapi;

import com.serenity.frase.application.port.out.ProveedorFrasesMotivacionalesPort;
import com.serenity.frase.domain.model.FraseMotivacional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class MotivationalApiAdapter
        implements ProveedorFrasesMotivacionalesPort {

    public record FraseApi(
            Long id,
            String phrase,
            String author,
            Integer religion
    ) {}

    private final RestClient cliente;

    private volatile List<FraseMotivacional> cache = List.of();

    public MotivationalApiAdapter(
            RestClient.Builder builder,
            @Value("${app.frases.api-url:https://cdn.jsdelivr.net/gh/GomezMig03/MotivationalAPI}")
            String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(5));

        this.cliente = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<FraseMotivacional> obtenerFrases() {

        if (!cache.isEmpty()) {
            return cache;
        }

        List<FraseApi> respuesta = cliente.get()
                .uri("/es.json")
                .retrieve()
                .body(new ParameterizedTypeReference<List<FraseApi>>() {});

        if (respuesta == null) {
            return List.of();
        }

        List<FraseMotivacional> frases = respuesta.stream()
                .filter(f -> f.phrase() != null
                        && !f.phrase().isBlank())
                .map(f -> new FraseMotivacional(
                        f.phrase().trim(),
                        f.author() == null || f.author().isBlank()
                                ? "Autor desconocido"
                                : f.author().trim(),
                        Integer.valueOf(1).equals(f.religion())
                ))
                .toList();

        if (!frases.isEmpty()) {
            cache = List.copyOf(frases);
        }

        return cache;
    }
}