package com.serenity.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.serenity.shared.security.JwtAuthenticationFilter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serenity.shared.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, objectMapper);
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()   // registro y login sin token
                        .requestMatchers("/error").permitAll()          // para ver errores reales
                        .requestMatchers("/ws/**").permitAll()          // el JWT se valida en STOMP CONNECT
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> {
                    log.error("Autenticación requerida para {} {}", request.getMethod(), request.getRequestURI(), ex);
                    writeProblem(request, response, HttpStatus.UNAUTHORIZED, "Autenticación requerida");
                })
                .accessDeniedHandler((request, response, ex) -> {
                    log.error("Acceso denegado a {} {}", request.getMethod(), request.getRequestURI(), ex);
                    writeProblem(request, response, HttpStatus.FORBIDDEN, "Acceso denegado");
                }));
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
    private void writeProblem(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, HttpStatus status, String detail) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
