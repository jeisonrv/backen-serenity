package com.serenity.oyente.domain.model;
import java.time.LocalDateTime;
public record DisponibilidadOyente(Long idOyente, boolean disponible, LocalDateTime fechaActualizacion) {}
