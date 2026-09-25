package com.serenity.diario.infrastructure.adapter.in.rest;

import com.serenity.diario.application.port.in.GuardarEntradaDiarioUseCase;
import com.serenity.diario.application.port.in.ListarEntradasDiarioUseCase;
import com.serenity.diario.domain.model.EntradaDiario;
import com.serenity.diario.infrastructure.adapter.in.rest.dto.DiarioResumenResponse;
import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.serenity.diario.application.port.in.GestionarEntradaDiarioUseCase;

@RestController @RequestMapping("/api/diario")
public class DiarioController {
    public record GuardarRequest(String titulo,String contenido,String tipoPrompt){}
    private final GuardarEntradaDiarioUseCase guardar; private final ListarEntradasDiarioUseCase listar;
    private final GestionarEntradaDiarioUseCase gestionar;
    public DiarioController(GuardarEntradaDiarioUseCase guardar,ListarEntradasDiarioUseCase listar,GestionarEntradaDiarioUseCase gestionar){this.guardar=guardar;this.listar=listar;this.gestionar=gestionar;}
    @PostMapping public EntradaDiario guardar(@RequestBody GuardarRequest req,@AuthenticationPrincipal AuthenticatedUser user){return guardar.guardar(user.id(),req.titulo(),req.contenido(),req.tipoPrompt());}
    @GetMapping public List<DiarioResumenResponse> listar(@AuthenticationPrincipal AuthenticatedUser user){
        return listar.listar(user.id()).stream().map(DiarioResumenResponse::desde).toList();
    }
    @GetMapping("/{id}") public EntradaDiario obtener(@PathVariable Long id,@AuthenticationPrincipal AuthenticatedUser user){return gestionar.obtener(id,user.id());}
    @PutMapping("/{id}") public EntradaDiario editar(@PathVariable Long id,@RequestBody GuardarRequest req,@AuthenticationPrincipal AuthenticatedUser user){return gestionar.editar(id,user.id(),req.titulo(),req.contenido(),req.tipoPrompt());}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id,@AuthenticationPrincipal AuthenticatedUser user){gestionar.eliminar(id,user.id());}
}
