package com.serenity.shared.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.serenity.shared.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> recursoNoEncontrado(ResourceNotFoundException ex) {
        log.error("Recurso no encontrado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> rutaNoEncontrada(NoResourceFoundException ex) {
        log.error("Ruta no encontrada: {}", ex.getResourcePath(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Ruta no encontrada"));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> solicitudInvalida(Exception ex) {
        log.error("Solicitud inválida", ex);
        return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la solicitud es inválido"));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> manejarErroresDeNegocio(RuntimeException ex) {
        log.error("Error de negocio: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(Map.of("error", mensaje(ex)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado atendiendo la solicitud", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", mensaje(ex)));
    }

    private String mensaje(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Error interno del servidor" : ex.getMessage();
    }
}
