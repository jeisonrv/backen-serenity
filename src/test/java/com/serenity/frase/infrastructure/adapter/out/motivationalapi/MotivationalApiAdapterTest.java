package com.serenity.frase.infrastructure.adapter.out.motivationalapi;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MotivationalApiAdapterTest {
    private HttpServer servidor;

    private MotivationalApiAdapter iniciarServidor(int estado, String cuerpo, AtomicInteger peticiones) throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/es.json", intercambio -> {
            peticiones.incrementAndGet();
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        return new MotivationalApiAdapter(RestClient.builder(), "http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @AfterEach void detenerServidor() { if (servidor != null) servidor.stop(0); }

    @Test void mapeaYRecortaFrasesYMarcaReligion() throws Exception {
        var adapter = iniciarServidor(200, "[{\"phrase\":\"  Paz  \",\"author\":\"  Ana  \",\"religion\":1},{\"phrase\":\"Fe\",\"author\":\" \"}]", new AtomicInteger());
        var frases = adapter.obtenerFrases();
        assertEquals("Paz", frases.get(0).frase());
        assertEquals("Ana", frases.get(0).autor());
        assertTrue(frases.get(0).religiosa());
        assertEquals("Autor desconocido", frases.get(1).autor());
        assertFalse(frases.get(1).religiosa());
    }

    @Test void descartaFrasesNulasYEnBlancoYUsaCache() throws Exception {
        AtomicInteger peticiones = new AtomicInteger();
        var adapter = iniciarServidor(200, "[null,{\"phrase\":\" \"},{\"phrase\":\" Viva \",\"author\":\"B\"}]", peticiones);
        assertEquals(1, adapter.obtenerFrases().size());
        assertEquals(1, adapter.obtenerFrases().size());
        assertEquals(1, peticiones.get());
    }

    @Test void respuesta500LanzaExcepcionDeEjecucion() throws Exception {
        var adapter = iniciarServidor(500, "fallo", new AtomicInteger());
        assertThrows(RuntimeException.class, adapter::obtenerFrases);
    }
}
