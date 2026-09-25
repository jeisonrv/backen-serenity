package com.serenity.animo.infrastructure.adapter.in.rest;

import com.serenity.animo.application.port.in.ListarAnimoUseCase;
import com.serenity.animo.application.port.in.RegistrarAnimoUseCase;
import com.serenity.animo.domain.model.EstadoAnimo;
import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/animo")
public class EstadoAnimoController {
    public record RegistrarRequest(int valor) {}
    private final RegistrarAnimoUseCase registrar; private final ListarAnimoUseCase listar;
    public EstadoAnimoController(RegistrarAnimoUseCase registrar, ListarAnimoUseCase listar){this.registrar=registrar;this.listar=listar;}
    @PostMapping public EstadoAnimo registrar(@RequestBody RegistrarRequest request, @AuthenticationPrincipal AuthenticatedUser user){return registrar.registrar(user.id(),request.valor());}
    @GetMapping public List<EstadoAnimo> listar(@AuthenticationPrincipal AuthenticatedUser user){return listar.listar(user.id());}
    @GetMapping("/hoy") public ResponseEntity<EstadoAnimo> hoy(@AuthenticationPrincipal AuthenticatedUser user){
        return listar.obtenerHoy(user.id()).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.noContent().build());
    }
    @GetMapping("/historial") public List<EstadoAnimo> historial(@RequestParam(defaultValue="30") int dias,
            @AuthenticationPrincipal AuthenticatedUser user){return listar.historial(user.id(),dias);}
}
