package com.serenity.usuario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.serenity.usuario.domain.model.Rol;
import jakarta.persistence.Column;

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USUARIO'")
    private Rol rol = Rol.USUARIO;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean restringido;
    @Column(length = 1000)
    private String motivoRestriccion;
    private LocalDateTime restringidoHasta;
    private LocalDateTime fechaRestriccion;
    private Long restringidoPor;
    @Column(name = "google_subject", unique = true, length = 255)
    private String googleSubject;
    @Column(name = "recordatorios_email", nullable = false)
    private boolean recordatoriosEmail = false;

    public boolean isRecordatoriosEmail() {
        return recordatoriosEmail;
    }

    public void setRecordatoriosEmail(boolean recordatoriosEmail) {
        this.recordatoriosEmail = recordatoriosEmail;
    }

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
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isRestringido() { return restringido; }
    public void setRestringido(boolean restringido) { this.restringido = restringido; }
    public String getMotivoRestriccion() { return motivoRestriccion; }
    public void setMotivoRestriccion(String motivoRestriccion) { this.motivoRestriccion = motivoRestriccion; }
    public LocalDateTime getRestringidoHasta() { return restringidoHasta; }
    public void setRestringidoHasta(LocalDateTime restringidoHasta) { this.restringidoHasta = restringidoHasta; }
    public LocalDateTime getFechaRestriccion() { return fechaRestriccion; }
    public void setFechaRestriccion(LocalDateTime fechaRestriccion) { this.fechaRestriccion = fechaRestriccion; }
    public Long getRestringidoPor() { return restringidoPor; }
    public void setRestringidoPor(Long restringidoPor) { this.restringidoPor = restringidoPor; }
    public String getGoogleSubject() { return googleSubject; }
    public void setGoogleSubject(String googleSubject) { this.googleSubject = googleSubject; }
}
