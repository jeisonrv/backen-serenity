package com.serenity.shared.config;

import com.serenity.frase.application.port.in.ObtenerFraseDelDiaUseCase;
import com.serenity.shared.consent.ConsentService;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = com.serenity.frase.infrastructure.adapter.in.rest.FraseMotivacionalController.class,
        properties = {
                "jwt.secret=security-config-test-key-with-at-least-32-bytes",
                "jwt.expiration=60000"
        }
)
@Import({SecurityConfig.class, JwtUtil.class})
@ImportAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
class SecurityConfigTest {
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;
    @MockBean ConsentService consentService;
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

        mvc.perform(post("/api/auth/vincular"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/ruta-no-mapeada"))
                .andExpect(status().isNotFound());
    }

    @Test void loginGoogleIgnoraUnBearerInvalidoPorqueEsPublico() throws Exception {
        mvc.perform(post("/api/auth/google")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test void cambiarPasswordRequiereAutenticacionJwtReal() throws Exception {
        mvc.perform(post("/api/auth/cambiar-password"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/cambiar-password")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.USUARIO)))
                .andExpect(status().isNotFound());
        verify(acceso).verificarAcceso(5L);
    }

    @Test void crearPasswordRequiereAutenticacionJwtReal() throws Exception {
        mvc.perform(post("/api/auth/crear-password"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/crear-password")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.USUARIO)))
                .andExpect(status().isNotFound());
        verify(acceso).verificarAcceso(5L);
    }

    @Test void registroEInicioDeSesionContinuanPublicos() throws Exception {
        mvc.perform(post("/api/auth/registro"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/auth/login"))
                .andExpect(status().isNotFound());
    }

    @Test void rutasDeOyenteRechazanRolUsuarioConProblemDetail403() throws Exception {
        mvc.perform(get("/api/oyente/recurso")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.USUARIO)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Acceso denegado"));
    }

    @Test void rutasDeModeracionRechazanRolOyente() throws Exception {
        mvc.perform(get("/api/moderacion/reportes")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.OYENTE)))
                .andExpect(status().isForbidden());
    }

    @Test void rutaDeModeracionPermiteRolModerador() throws Exception {
        mvc.perform(get("/api/moderacion/reportes")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.MODERADOR)))
                .andExpect(status().isNotFound());
        verify(acceso).verificarAcceso(5L);
    }

    @Test void jwtIncluyeElRolSinReemplazarLaValidacionActualDeLaBaseDeDatos() {
        var claimsOyente = jwt.validarToken(
                jwt.generarToken(5L, "user@example.test", Rol.OYENTE));
        var claimsModerador = jwt.validarToken(
                jwt.generarToken(5L, "mod@example.test", Rol.MODERADOR));

        org.junit.jupiter.api.Assertions.assertEquals("OYENTE", claimsOyente.get("rol", String.class));
        org.junit.jupiter.api.Assertions.assertEquals("MODERADOR", claimsModerador.get("rol", String.class));
    }

    @Test void rolCorrectoPuedePasarLaReglaDeAutorizacion() throws Exception {
        mvc.perform(get("/api/oyente/recurso")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.OYENTE)))
                .andExpect(status().isNotFound());
        verify(acceso).verificarAcceso(5L);
    }

    @Test void moderadorNoPuedeUsarRutasExclusivasDeOyente() throws Exception {
        mvc.perform(get("/api/oyente/recurso")
                        .header("Authorization", "Bearer " + tokenJwt(Rol.MODERADOR)))
                .andExpect(status().isForbidden());
    }

    @Test void solicitudesPreflightOptionsSonPublicas() throws Exception {
        mvc.perform(options("/api/recurso-privado"))
                .andExpect(status().isNotFound());
    }

    private String tokenJwt(Rol rol) {
        when(acceso.verificarAcceso(5L)).thenReturn(rol);
        return jwt.generarToken(5L, "user@example.test", rol);
    }
}
