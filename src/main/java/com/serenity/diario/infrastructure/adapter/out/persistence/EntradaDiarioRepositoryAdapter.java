package com.serenity.diario.infrastructure.adapter.out.persistence;

import com.serenity.diario.application.port.out.EntradaDiarioRepositoryPort;
import com.serenity.diario.domain.model.EntradaDiario;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Component
public class EntradaDiarioRepositoryAdapter implements EntradaDiarioRepositoryPort {
    private final EntradaDiarioJpaRepository repository;
    public EntradaDiarioRepositoryAdapter(EntradaDiarioJpaRepository repository){this.repository=repository;}
    @Override public EntradaDiario guardar(EntradaDiario e){
        EntradaDiarioJpaEntity entity=e.idEntrada()==null?new EntradaDiarioJpaEntity(null,e.idUsuario(),e.titulo(),e.contenido(),e.tipoPrompt(),e.fechaCreacion(),e.fechaActualizacion()):
                repository.findByIdEntradaAndIdUsuario(e.idEntrada(),e.idUsuario()).orElseThrow();
        if(e.idEntrada()!=null){entity.setTitulo(e.titulo());entity.setContenido(e.contenido());entity.setTipoPrompt(e.tipoPrompt());entity.setFechaActualizacion(e.fechaActualizacion());}
        return dominio(repository.save(entity));
    }
    @Override public List<EntradaDiario> buscarPorUsuario(Long id){return repository.findByIdUsuarioOrderByFechaCreacionDesc(id).stream().map(this::dominio).toList();}
    @Override public Page<EntradaDiario> buscarPaginaPorUsuario(Long id,int page,int size){return repository.findByIdUsuarioOrderByFechaCreacionDesc(id,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"fechaCreacion"))).map(this::dominio);}
    @Override public Optional<EntradaDiario> buscarPropia(Long id,Long usuario){return repository.findByIdEntradaAndIdUsuario(id,usuario).map(this::dominio);}
    @Override public void eliminarPropia(Long id,Long usuario){repository.findByIdEntradaAndIdUsuario(id,usuario).ifPresent(repository::delete);}
    private EntradaDiario dominio(EntradaDiarioJpaEntity e){return new EntradaDiario(e.getIdEntrada(),e.getIdUsuario(),e.getTitulo(),e.getContenido(),e.getTipoPrompt(),e.getFechaCreacion(),e.getFechaActualizacion()==null?e.getFechaCreacion():e.getFechaActualizacion());}
}
