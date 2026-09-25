package com.serenity.animo.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EstadoAnimo(Long idAnimo, Long idUsuario, int valor, LocalDate fechaDia,
                          LocalDateTime fechaRegistro) {}
