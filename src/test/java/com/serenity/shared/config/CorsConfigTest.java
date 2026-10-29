package com.serenity.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {
    @Test void configuraOrigenesMetodosCabecerasYCredenciales() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "https://app.serenity.test, , http://localhost:4200 ");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/usuarios"));

        assertNotNull(cors);
        assertEquals(java.util.List.of("https://app.serenity.test", "http://localhost:4200"), cors.getAllowedOrigins());
        assertEquals(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), cors.getAllowedMethods());
        assertEquals(java.util.List.of("*"), cors.getAllowedHeaders());
        assertEquals(java.util.List.of("Authorization"), cors.getExposedHeaders());
        assertTrue(cors.getAllowCredentials());
    }

    @Test void registraFiltroCorsAntesDeSeguridad() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "https://app.serenity.test");

        FilterRegistrationBean<CorsFilter> registro = config.corsFilterRegistration();

        assertInstanceOf(CorsFilter.class, registro.getFilter());
        assertEquals(Ordered.HIGHEST_PRECEDENCE, registro.getOrder());
    }
}
