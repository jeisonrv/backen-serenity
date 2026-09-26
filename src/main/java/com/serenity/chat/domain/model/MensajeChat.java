package com.serenity.chat.domain.model;
import java.time.LocalDateTime;
public record MensajeChat(Long idConversacion,String remitente,String contenido,LocalDateTime fechaEnvio){}
