package com.serenity.usuario.infrastructure.adapter.out.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.serenity.usuario.domain.model.IdentidadGoogle;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleIdentityVerifierAdapterTest {
    private static final String CLIENT_ID = "serenity-web-client.apps.googleusercontent.com";
    private static final String EMAIL = "google-user@example.com";

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
    }

    @Test
    void verificaTokenGoogleFirmadoConLaAudienciaConfigurada() throws Exception {
        GoogleIdTokenVerifier configuredVerifier =
                GoogleIdentityVerifierAdapter.crearVerificador(CLIENT_ID);

        assertEquals(1, configuredVerifier.getAudience().size());
        assertTrue(configuredVerifier.getAudience().contains(CLIENT_ID));
        assertTrue(configuredVerifier.getIssuers().contains("https://accounts.google.com"));

        GoogleIdentityVerifierAdapter adapter = new GoogleIdentityVerifierAdapter(verifier(CLIENT_ID));
        Optional<IdentidadGoogle> result = adapter.verificar(token(
                CLIENT_ID, "https://accounts.google.com", new Date(System.currentTimeMillis() + 600_000),
                keyPair.getPrivate()));

        assertTrue(result.isPresent());
        assertEquals("google-subject", result.get().subject());
        assertEquals(EMAIL, result.get().email());
        assertTrue(result.get().emailVerificado());
    }

    @Test
    void rechazaTokenExpirado() throws Exception {
        GoogleIdentityVerifierAdapter adapter = new GoogleIdentityVerifierAdapter(verifier(CLIENT_ID));

        Optional<IdentidadGoogle> result = adapter.verificar(token(
                CLIENT_ID, "https://accounts.google.com", new Date(System.currentTimeMillis() - 600_000),
                keyPair.getPrivate()));

        assertFalse(result.isPresent());
    }

    @Test
    void rechazaTokenCuandoElClientIdNoCoincideConLaAudiencia() throws Exception {
        GoogleIdentityVerifierAdapter adapter = new GoogleIdentityVerifierAdapter(verifier("different-client"));

        Optional<IdentidadGoogle> result = adapter.verificar(token(
                CLIENT_ID, "https://accounts.google.com", new Date(System.currentTimeMillis() + 600_000),
                keyPair.getPrivate()));

        assertFalse(result.isPresent());
    }

    @Test
    void rechazaTokenConIssuerNoConfiable() throws Exception {
        GoogleIdentityVerifierAdapter adapter = new GoogleIdentityVerifierAdapter(verifier(CLIENT_ID));

        Optional<IdentidadGoogle> result = adapter.verificar(token(
                CLIENT_ID, "https://issuer.example.com", new Date(System.currentTimeMillis() + 600_000),
                keyPair.getPrivate()));

        assertFalse(result.isPresent());
    }

    @Test
    void rechazaTokenConFirmaInvalida() throws Exception {
        KeyPair anotherKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        GoogleIdentityVerifierAdapter adapter = new GoogleIdentityVerifierAdapter(verifier(CLIENT_ID));

        Optional<IdentidadGoogle> result = adapter.verificar(token(
                CLIENT_ID, "https://accounts.google.com", new Date(System.currentTimeMillis() + 600_000),
                anotherKeyPair.getPrivate()));

        assertFalse(result.isPresent());
    }

    private GoogleIdTokenVerifier verifier(String audience) throws Exception {
        GooglePublicKeysManager keys = new GooglePublicKeysManager(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance());
        ReflectionTestUtils.setField(keys, "publicKeys", List.<PublicKey>of(keyPair.getPublic()));
        ReflectionTestUtils.setField(keys, "expirationTimeMilliseconds", System.currentTimeMillis() + 3_600_000);
        return new GoogleIdTokenVerifier.Builder(keys)
                .setAudience(List.of(audience))
                .build();
    }

    private String token(String audience, String issuer, Date expiration, java.security.PrivateKey signer) {
        long issuedAt = System.currentTimeMillis() - 1_000;
        return Jwts.builder()
                .setHeaderParam("kid", "local-test-key")
                .setIssuer(issuer)
                .setSubject("google-subject")
                .setAudience(audience)
                .setIssuedAt(new Date(issuedAt))
                .setExpiration(expiration)
                .claim("email", EMAIL)
                .claim("email_verified", true)
                .signWith(signer, SignatureAlgorithm.RS256)
                .compact();
    }
}
