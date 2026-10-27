package com.serenity.chat.domain.model;
import java.time.LocalDateTime;
public record Conversacion(Long idConversacion,Long idUsuario,Long idOyente,LocalDateTime fechaInicio,LocalDateTime fechaFin){}
