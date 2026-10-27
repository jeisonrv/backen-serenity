package com.serenity.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.domain.model.Rol;
import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.ResourceNotFoundException;

import java.io.IOException;
import java.util.List;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final GestionarAccesoUseCase acceso;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, GestionarAccesoUseCase acceso) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.acceso = acceso;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.validarToken(header.substring(7));
                Number id = claims.get("usuarioId", Number.class);
                String email = claims.getSubject();
                if (id == null || email == null || email.isBlank()) {
                    throw new IllegalArgumentException("Token sin identidad de usuario");
                }
                AuthenticatedUser user = new AuthenticatedUser(id.longValue(), email);
                Rol rol = acceso.verificarAcceso(user.id());
                var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AccesoRestringidoException ex) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "El acceso de esta cuenta está restringido");
                problem.setProperty("codigo", "ACCESO_RESTRINGIDO");
                problem.setProperty("restringidoHasta", ex.getRestringidoHasta());
                escribir(request, response, problem);
                return;
            } catch (JwtException | IllegalArgumentException | ResourceNotFoundException ex) {
                log.warn("Se rechazó un token JWT inválido");
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                escribir(request, response, problem);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void escribir(HttpServletRequest request, HttpServletResponse response, ProblemDetail problem) throws IOException {
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(problem.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
