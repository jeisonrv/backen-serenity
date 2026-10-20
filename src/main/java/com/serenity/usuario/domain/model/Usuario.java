package com.serenity.usuario.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private int nivel;
    private int xp;
    private int racha;
    private int mejorRacha;
    private LocalDate ultimaActividad;
    private Rol rol = Rol.USUARIO;
    private boolean restringido;
    private String motivoRestriccion;
    private LocalDateTime restringidoHasta;
    private LocalDateTime fechaRestriccion;
    private Long restringidoPor;
    private String googleSubject;

    public Usuario() {}

    public Usuario(String username, String email, String passwordHash) {
        this(null, username, email, passwordHash, 1, 0, 0, 0);
    }

    public Usuario(Long id, String username, String email, String passwordHash,
                   int nivel, int xp, int racha, int mejorRacha) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nivel = nivel;
        this.xp = xp;
        this.racha = racha;
        this.mejorRacha = mejorRacha;
    }

    public void registrarActividad(LocalDate hoy) {
        if (ultimaActividad == null || ultimaActividad.isBefore(hoy.minusDays(1))) {
            racha = 1;
        } else if (ultimaActividad.isBefore(hoy)) {
            racha++;
        }
        mejorRacha = Math.max(mejorRacha, racha);
        ultimaActividad = hoy;
    }

    public void agregarXP(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La XP no puede ser negativa");
        }
        xp += cantidad;
        nivel = xp / 100 + 1;
    }

    public boolean estaRestringido(LocalDateTime ahora) {
        return restringido && (restringidoHasta == null || restringidoHasta.isAfter(ahora));
    }

    public void restringir(String motivo, LocalDateTime hasta, LocalDateTime ahora, Long moderadorId) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo de restricción es obligatorio");
        if (hasta != null && !hasta.isAfter(ahora)) throw new IllegalArgumentException("La fecha de restricción debe ser futura");
        restringido = true;
        motivoRestriccion = motivo.trim();
        restringidoHasta = hasta;
        fechaRestriccion = ahora;
        restringidoPor = moderadorId;
    }

    public void levantarRestriccion() {
        restringido = false;
        motivoRestriccion = null;
        restringidoHasta = null;
        fechaRestriccion = null;
        restringidoPor = null;
    }

    public void restaurarRestriccion(boolean restringido, String motivo, LocalDateTime hasta, LocalDateTime fecha, Long moderadorId) {
        this.restringido = restringido;
        this.motivoRestriccion = motivo;
        this.restringidoHasta = hasta;
        this.fechaRestriccion = fecha;
        this.restringidoPor = moderadorId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getNivel() { return nivel; }
    public int getXp() { return xp; }
    public int getRacha() { return racha; }
    public int getMejorRacha() { return mejorRacha; }
    public LocalDate getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDate ultimaActividad) { this.ultimaActividad = ultimaActividad; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol == null ? Rol.USUARIO : rol; }
    public boolean isRestringido() { return restringido; }
    public String getMotivoRestriccion() { return motivoRestriccion; }
    public LocalDateTime getRestringidoHasta() { return restringidoHasta; }
    public LocalDateTime getFechaRestriccion() { return fechaRestriccion; }
    public Long getRestringidoPor() { return restringidoPor; }
    public String getGoogleSubject() { return googleSubject; }
    public void setGoogleSubject(String googleSubject) { this.googleSubject = googleSubject; }
}
