package com.serenity.usuario.infrastructure.adapter.in.rest;
import com.serenity.usuario.application.port.in.RestablecerPasswordUseCase;
import com.serenity.usuario.application.port.in.SolicitarRestablecimientoPasswordUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class RecuperacionPasswordController {
 public record SolicitarRequest(@NotBlank @Email @Size(max=254) String correo){}
 public record RestablecerRequest(@NotBlank String token,@NotBlank @Size(min=8,max=72) String passwordNueva){}
 private final SolicitarRestablecimientoPasswordUseCase solicitar;
 private final RestablecerPasswordUseCase restablecer;
 public RecuperacionPasswordController(SolicitarRestablecimientoPasswordUseCase solicitar,RestablecerPasswordUseCase restablecer){
  this.solicitar=solicitar;this.restablecer=restablecer;
 }
 @PostMapping("/olvide-password") public ResponseEntity<Void> solicitar(@Valid @RequestBody SolicitarRequest request){
  solicitar.solicitar(request.correo());return ResponseEntity.accepted().build();
 }
 @PostMapping("/restablecer-password") public ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecerRequest request){
  restablecer.restablecer(request.token(),request.passwordNueva());return ResponseEntity.noContent().build();
 }
}