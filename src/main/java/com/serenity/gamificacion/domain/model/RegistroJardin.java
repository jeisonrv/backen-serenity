package com.serenity.gamificacion.domain.model;
import java.time.LocalDateTime;
public record RegistroJardin(Long idJardin,Long idUsuario,String tipoPlanta,LocalDateTime fechaRegistro){}
