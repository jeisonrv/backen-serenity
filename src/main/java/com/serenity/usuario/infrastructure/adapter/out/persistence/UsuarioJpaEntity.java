package com.serenity.usuario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;
    private int nivel;
    private int xp;
    private int racha;
    private int mejorRacha;
    private LocalDate ultimaActividad;

    public UsuarioJpaEntity() {}

    public UsuarioJpaEntity(Long id, String username, String email, String passwordHash,
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getRacha() { return racha; }
    public void setRacha(int racha) { this.racha = racha; }
    public int getMejorRacha() { return mejorRacha; }
    public void setMejorRacha(int mejorRacha) { this.mejorRacha = mejorRacha; }
    public LocalDate getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDate ultimaActividad) { this.ultimaActividad = ultimaActividad; }
}
