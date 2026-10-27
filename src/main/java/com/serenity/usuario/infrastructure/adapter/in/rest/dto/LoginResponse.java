package com.serenity.usuario.infrastructure.adapter.in.rest.dto;

public record LoginResponse(String token, UsuarioResponse usuario) {}