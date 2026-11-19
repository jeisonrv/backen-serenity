package com.serenity.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {
    @Test void configuraOrigenesMetodosCabecerasYCredenciales() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins",
                "https://brave-ocean-059a05810.3.azurestaticapps.net , http://localhost:4200 ");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/auth/google"));

        assertNotNull(cors);
        assertEquals(java.util.List.of(
                "https://brave-ocean-059a05810.3.azurestaticapps.net",
                "http://localhost:4200"), cors.getAllowedOrigins());
        assertEquals(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), cors.getAllowedMethods());
        assertEquals(java.util.List.of("*"), cors.getAllowedHeaders());
        assertTrue(cors.checkOrigin("https://brave-ocean-059a05810.3.azurestaticapps.net") != null);
        assertTrue(cors.checkHeaders(java.util.List.of("Authorization", "Content-Type"))
                .containsAll(java.util.List.of("Authorization", "Content-Type")));
        assertEquals(java.util.List.of("Authorization"), cors.getExposedHeaders());
        assertTrue(cors.getAllowCredentials());
    }
}
