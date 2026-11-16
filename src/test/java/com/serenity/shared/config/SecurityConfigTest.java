package com.serenity.shared.config;

import com.serenity.frase.application.port.in.ObtenerFraseDelDiaUseCase;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Rol;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.serenity.frase.infrastructure.adapter.in.rest.FraseMotivacionalController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {
    @Autowired MockMvc mvc;
    @MockBean JwtUtil jwt;
    @MockBean GestionarAccesoUseCase acceso;
    @MockBean ObtenerFraseDelDiaUseCase frases;

    @Test void endpointPublicoDeFrasesRespondeSinAutenticacion() throws Exception {
        when(frases.obtener()).thenReturn(new com.serenity.frase.domain.model.FraseMotivacional("Calma", "Serenity", false));

        mvc.perform(get("/api/frases/motivacional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frase").value("Calma"));
        verify(frases).obtener();
    }

    @Test void endpointPrivadoRequiereAutenticacionYDevuelveProblemDetail() throws Exception {
        mvc.perform(get("/api/recurso-privado"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Autenticación requerida"));
    }

    @Test void vinculacionDeGoogleRequiereAutenticacionAunqueAuthSeaPublico() throws Exception {
        mvc.perform(post("/api/auth/google/vincular"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/ruta-no-mapeada"))
                .andExpect(status().isNotFound());
    }

    @Test void rutasDeOyenteRechazanRolUsuarioConProblemDetail403() throws Exception {
        prepararJwt(Rol.USUARIO);

        mvc.perform(get("/api/oyente/recurso").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Acceso denegado"));
    }

    @Test void rutasDeModeracionRechazanRolOyente() throws Exception {
        prepararJwt(Rol.OYENTE);

        mvc.perform(get("/api/moderacion/reportes").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isForbidden());
    }

    @Test void rolCorrectoPuedePasarLaReglaDeAutorizacion() throws Exception {
        prepararJwt(Rol.OYENTE);

        mvc.perform(get("/api/oyente/recurso").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isNotFound());
        verify(acceso).verificarAcceso(5L);
    }

    @Test void solicitudesPreflightOptionsSonPublicas() throws Exception {
        mvc.perform(options("/api/recurso-privado"))
                .andExpect(status().isNotFound());
    }

    private void prepararJwt(Rol rol) {
        Claims claims = mock(Claims.class);
        when(jwt.validarToken("token-valido")).thenReturn(claims);
        when(claims.get("usuarioId", Number.class)).thenReturn(5L);
        when(claims.getSubject()).thenReturn("masked-user");
        when(acceso.verificarAcceso(5L)).thenReturn(rol);
    }
}
