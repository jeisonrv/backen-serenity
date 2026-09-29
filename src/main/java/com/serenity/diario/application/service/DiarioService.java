package com.serenity.diario.application.service;

import com.serenity.diario.application.port.in.GuardarEntradaDiarioUseCase;
import com.serenity.diario.application.port.in.ListarEntradasDiarioUseCase;
import com.serenity.diario.application.port.out.EntradaDiarioRepositoryPort;
import com.serenity.diario.domain.model.EntradaDiario;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import com.serenity.diario.application.port.in.GestionarEntradaDiarioUseCase;
import com.serenity.shared.exception.ResourceNotFoundException;

@Service
public class DiarioService implements GuardarEntradaDiarioUseCase, ListarEntradasDiarioUseCase, GestionarEntradaDiarioUseCase {
    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");
    private final EntradaDiarioRepositoryPort repository;
    public DiarioService(EntradaDiarioRepositoryPort repository){this.repository=repository;}
    public EntradaDiario guardar(Long id,String contenido,String tipoPrompt){
        return guardar(id,null,contenido,tipoPrompt);
    }
    @Override public EntradaDiario guardar(Long id,String titulo,String contenido,String tipoPrompt){
        if(contenido==null||contenido.isBlank())throw new IllegalArgumentException("El contenido es obligatorio");
        if(titulo!=null&&titulo.length()>200)throw new IllegalArgumentException("El título no puede superar 200 caracteres");
        LocalDateTime ahora=LocalDateTime.now(ZONA_BOGOTA);
        return repository.guardar(new EntradaDiario(null,id,titulo,contenido,tipoPrompt==null?"libre":tipoPrompt,ahora,ahora));
    }
    @Override public List<EntradaDiario> listar(Long id){return repository.buscarPorUsuario(id);}
    @Override public Page<EntradaDiario> listar(Long id,int page,int size){
        if(page<0)throw new IllegalArgumentException("page no puede ser negativo");
        if(size<1||size>100)throw new IllegalArgumentException("size debe estar entre 1 y 100");
        return repository.buscarPaginaPorUsuario(id,page,size);
    }
    @Override public EntradaDiario obtener(Long id,Long usuario){return repository.buscarPropia(id,usuario).orElseThrow(()->new ResourceNotFoundException("Entrada de diario no encontrada"));}
    @Override public EntradaDiario editar(Long id,Long usuario,String titulo,String contenido,String tipoPrompt){
        if(contenido==null||contenido.isBlank())throw new IllegalArgumentException("El contenido es obligatorio");
        if(titulo!=null&&titulo.length()>200)throw new IllegalArgumentException("El título no puede superar 200 caracteres");
        EntradaDiario actual=obtener(id,usuario);
        return repository.guardar(new EntradaDiario(actual.idEntrada(),usuario,titulo==null?actual.titulo():titulo,contenido,
                tipoPrompt==null?actual.tipoPrompt():tipoPrompt,actual.fechaCreacion(),LocalDateTime.now(ZONA_BOGOTA)));
    }
    @Override public void eliminar(Long id,Long usuario){obtener(id,usuario);repository.eliminarPropia(id,usuario);}
}
