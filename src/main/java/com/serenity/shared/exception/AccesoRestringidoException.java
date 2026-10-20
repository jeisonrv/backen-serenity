package com.serenity.shared.exception;

import java.time.LocalDateTime;

public class AccesoRestringidoException extends RuntimeException {
    private final LocalDateTime restringidoHasta;
    public AccesoRestringidoException(LocalDateTime hasta) {
        super("El acceso de esta cuenta está restringido");
        this.restringidoHasta = hasta;
    }
    public LocalDateTime getRestringidoHasta() { return restringidoHasta; }
}
