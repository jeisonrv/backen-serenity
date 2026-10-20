package com.serenity.chat.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ModeracionChatUseCase;
import com.serenity.chat.domain.model.*;
import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.usuario.domain.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/moderacion")
public class ModeracionController {
    public record ResolverRequest(@NotNull Boolean procedente, @NotBlank @Size(max=1000) String observacion) {}
    public record RestriccionRequest(@NotBlank @Size(max=1000) String motivo,
                                    LocalDateTime hasta) {}
    public record ConversacionResumen(Long idConversacion,Long idUsuario,Long idOyente,
                                      LocalDateTime fechaInicio,LocalDateTime fechaFin) {}
    public record MensajeResumen(Long idConversacion,String remitente,String contenido,LocalDateTime fechaEnvio) {}
    public record DetalleResponse(ReporteChat reporte,Long idUsuario,Long idOyente,
            LocalDateTime fechaInicio,LocalDateTime fechaFin,List<MensajeResumen> mensajes,
            long reportesTotalesDelReportado,long reportesProcedentesDelReportado) {}
    public record UsuarioRestringido(Long id,String username,String email,String motivo,
            LocalDateTime fechaRestriccion,LocalDateTime restringidoHasta,Long restringidoPor) {
        static UsuarioRestringido desde(Usuario u){return new UsuarioRestringido(u.getId(),u.getUsername(),u.getEmail(),u.getMotivoRestriccion(),u.getFechaRestriccion(),u.getRestringidoHasta(),u.getRestringidoPor());}
    }
    public record RestriccionResponse(Long id,boolean restringido,LocalDateTime restringidoHasta) {}
    private final ModeracionChatUseCase moderacion;
    public ModeracionController(ModeracionChatUseCase moderacion){this.moderacion=moderacion;}

    @GetMapping("/reportes")
    public List<ReporteChat> reportes(@RequestParam(required=false) EstadoReporte estado){return moderacion.listarReportes(estado);}

    @GetMapping("/reportes/{id}")
    public DetalleResponse detalle(@PathVariable Long id){
        DetalleReporte d=moderacion.detalleReporte(id);Conversacion c=d.conversacion();
        return new DetalleResponse(d.reporte(),c.idUsuario(),c.idOyente(),c.fechaInicio(),c.fechaFin(),
                d.mensajes().stream().map(m->new MensajeResumen(m.idConversacion(),m.remitente(),m.contenido(),m.fechaEnvio())).toList(),
                d.reportesTotalesDelReportado(),d.reportesProcedentesDelReportado());
    }

    @PostMapping("/reportes/{id}/resolver")
    public ReporteChat resolver(@PathVariable Long id,@Valid @RequestBody ResolverRequest request,
                                @AuthenticationPrincipal AuthenticatedUser user){
        return moderacion.resolverReporte(id,user.id(),request.procedente(),request.observacion());
    }

    @GetMapping("/usuarios/restringidos")
    public List<UsuarioRestringido> restringidos(){return moderacion.listarRestringidos().stream().map(UsuarioRestringido::desde).toList();}

    @PostMapping("/usuarios/{id}/restriccion")
    public RestriccionResponse restringir(@PathVariable Long id,@Valid @RequestBody RestriccionRequest request,
                                          @AuthenticationPrincipal AuthenticatedUser user){
        Usuario u=moderacion.restringirUsuario(id,user.id(),request.motivo(),request.hasta());
        return new RestriccionResponse(u.getId(),u.isRestringido(),u.getRestringidoHasta());
    }

    @DeleteMapping("/usuarios/{id}/restriccion")
    public RestriccionResponse levantar(@PathVariable Long id){
        Usuario u=moderacion.levantarRestriccion(id);return new RestriccionResponse(u.getId(),u.isRestringido(),u.getRestringidoHasta());
    }
}
