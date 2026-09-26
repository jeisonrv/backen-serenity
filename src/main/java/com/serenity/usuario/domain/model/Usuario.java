package com.serenity.usuario.domain.model;

import java.time.LocalDate;

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
}
