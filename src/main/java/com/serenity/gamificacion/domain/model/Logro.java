package com.serenity.gamificacion.domain.model;
import java.time.LocalDateTime;
public record Logro(Long id,String nombre,String descripcion,boolean desbloqueado,LocalDateTime fechaDesbloqueo){}
