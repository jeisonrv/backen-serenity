package com.serenity.usuario.domain.model;

/** Identidad de Google validada por el adaptador de infraestructura. */
public record IdentidadGoogle(String subject, String email, boolean emailVerificado, String nombre) {}
