package com.serenity.shared.consent;

import com.serenity.shared.exception.GlobalExceptionHandler;
import com.serenity.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SensitiveConsentInterceptorTest {

    static class SensitiveController {
        @RequireSensitiveConsent
        public void protectedEndpoint() { }
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deniesWithoutConsentAndUsesStableErrorCode() throws Exception {
        ConsentService service = Mockito.mock(ConsentService.class);
        when(service.hasCurrentSensitiveConsent(7L)).thenReturn(false);
        authenticate();
        SensitiveConsentInterceptor interceptor = new SensitiveConsentInterceptor(service);
        HandlerMethod handler = handler();

        SensitiveConsentRequiredException error = assertThrows(
                SensitiveConsentRequiredException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handler));
        var response = new GlobalExceptionHandler().consentimientoSensibleRequerido(
                error, new MockHttpServletRequest("GET", "/api/animo"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("SENSITIVE_CONSENT_REQUIRED", response.getBody().getProperties().get("codigo"));
    }

    @Test
    void allowsWhenConsentIsCurrent() throws Exception {
        ConsentService service = Mockito.mock(ConsentService.class);
        when(service.hasCurrentSensitiveConsent(7L)).thenReturn(true);
        authenticate();

        assertTrue(new SensitiveConsentInterceptor(service).preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), handler()));
    }

    private void authenticate() {
        var user = new AuthenticatedUser(7L, "user@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));
    }

    private HandlerMethod handler() throws NoSuchMethodException {
        Method method = SensitiveController.class.getMethod("protectedEndpoint");
        return new HandlerMethod(new SensitiveController(), method);
    }
}
