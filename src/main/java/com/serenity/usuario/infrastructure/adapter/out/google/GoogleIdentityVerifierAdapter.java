package com.serenity.usuario.infrastructure.adapter.out.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.serenity.usuario.application.port.out.VerificarIdentidadGooglePort;
import com.serenity.usuario.domain.model.IdentidadGoogle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Component
public class GoogleIdentityVerifierAdapter implements VerificarIdentidadGooglePort {
    private static final Logger log = LoggerFactory.getLogger(GoogleIdentityVerifierAdapter.class);

    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityVerifierAdapter(@Value("${google.oauth.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.strip();
        if (this.clientId.isBlank()) {
            this.verifier = null;
            return;
        }
        try {
            this.verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(this.clientId))
                    .build();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("No se pudo inicializar la verificación de Google", ex);
        }
    }

    @Override
    public Optional<IdentidadGoogle> verificar(String credential) {
        if (verifier == null) {
            log.warn("google.id_token_verification outcome=not_configured");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El inicio con Google no está configurado");
        }
        try {
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) {
                log.info("google.id_token_verification outcome=invalid");
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = token.getPayload();
            Object emailVerificadoClaim = payload.get("email_verified");
            boolean emailVerificado = Boolean.TRUE.equals(emailVerificadoClaim)
                    || "true".equalsIgnoreCase(String.valueOf(emailVerificadoClaim));
            Object nombreClaim = payload.get("name");
            String nombre = nombreClaim == null ? null : nombreClaim.toString();
            return Optional.of(new IdentidadGoogle(
                    payload.getSubject(), payload.getEmail(), emailVerificado, nombre));
        } catch (GeneralSecurityException ex) {
            log.warn("google.id_token_verification outcome=error exceptionType={}",
                    ex.getClass().getSimpleName());
            return Optional.empty();
        } catch (IOException ex) {
            log.warn("google.id_token_verification outcome=error exceptionType={}",
                    ex.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar la identidad con Google", ex);
        }
    }
}
