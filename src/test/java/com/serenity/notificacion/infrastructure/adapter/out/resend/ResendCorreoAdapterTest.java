package com.serenity.notificacion.infrastructure.adapter.out.resend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class ResendCorreoAdapterTest {
    private RestClient.Builder builder;
    private RestClient client;
    private RestClient.RequestBodyUriSpec request;
    private RestClient.RequestBodySpec bodyRequest;
    private RestClient.ResponseSpec response;
    private ResendCorreoAdapter adapter;

    @BeforeEach
    void setUp() {
        builder = mock(RestClient.Builder.class);
        client = mock(RestClient.class);
        request = mock(RestClient.RequestBodyUriSpec.class);
        bodyRequest = mock(RestClient.RequestBodySpec.class);
        response = mock(RestClient.ResponseSpec.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.requestFactory(any(ClientHttpRequestFactory.class))).thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.post()).thenReturn(request);
        when(request.uri(anyString())).thenReturn(bodyRequest);
        when(bodyRequest.contentType(any())).thenReturn(bodyRequest);
        when(bodyRequest.header(anyString(), any(String[].class))).thenReturn(bodyRequest);
        when(bodyRequest.body(any(Object.class))).thenReturn(bodyRequest);
        when(bodyRequest.retrieve()).thenReturn(response);
        adapter = new ResendCorreoAdapter(builder, "test-key", "Serenity <no-reply@example.test>");
    }

    @Test
    void usaRemitenteConfiguradoYCredencialEnLaSolicitud() {
        adapter.enviar("destino@example.test", "Asunto", "<p>Hola</p>", "Hola");

        verify(bodyRequest).header("Authorization", "Bearer test-key");
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(bodyRequest).body(payloadCaptor.capture());
        Map<?, ?> payload = (Map<?, ?>) payloadCaptor.getValue();
        assertEquals("Serenity <no-reply@example.test>", payload.get("from"));
        assertEquals(List.of("destino@example.test"), payload.get("to"));
        verify(response).toBodilessEntity();
    }

    @Test
    void fallaConMensajeSeguroCuandoResendDevuelveError() {
        when(response.toBodilessEntity()).thenThrow(new RestClientException("sensitive provider response"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> adapter.enviar("destino@example.test", "Asunto", "html", "texto"));

        assertEquals("No se pudo enviar correo mediante Resend", error.getMessage());
        assertNull(error.getCause());
        assertFalse(error.toString().contains("sensitive"));
    }

    @Test
    void rechazaConfiguracionIncompletaSinRealizarSolicitud() {
        ResendCorreoAdapter sinRemitente = new ResendCorreoAdapter(builder, "test-key", " ");

        assertThrows(IllegalStateException.class,
                () -> sinRemitente.enviar("destino@example.test", "Asunto", "html", "texto"));
        verifyNoInteractions(client);
    }
}
