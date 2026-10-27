package com.serenity.gamificacion.application.service;
import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.gamificacion.application.port.in.ConsultarGamificacionUseCase;
import com.serenity.gamificacion.domain.model.*;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ConsultarGamificacionService implements ConsultarGamificacionUseCase {
    private final SesionRepositoryPort sesiones; private final UsuarioRepositoryPort usuarios;
    public ConsultarGamificacionService(SesionRepositoryPort sesiones,UsuarioRepositoryPort usuarios){this.sesiones=sesiones;this.usuarios=usuarios;}
    @Override public List<RegistroJardin> jardin(Long id){return sesiones.buscarPorUsuario(id).stream().map(s->new RegistroJardin(s.getId(),id,s.getTipoEjercicio(),s.getFechaCompletado())).toList();}
    @Override public List<Logro> logros(Long id){int n=sesiones.buscarPorUsuario(id).size();return List.of(new Logro(1L,"Primer paso","Completa tu primera sesión",n>=1,null),new Logro(2L,"Constancia","Completa cinco sesiones",n>=5,null),new Logro(3L,"En crecimiento","Completa diez sesiones",n>=10,null));}
    @Override public ResumenEstadisticas resumen(Long id){var a=sesiones.buscarPorUsuario(id);int seconds=a.stream().mapToInt(s->s.getDuracionSegundos()==null?0:s.getDuracionSegundos()).sum();int streak=usuarios.buscarPorId(id).map(u->u.getRacha()).orElse(0);return new ResumenEstadisticas(a.size(),seconds/60,streak);}
}
