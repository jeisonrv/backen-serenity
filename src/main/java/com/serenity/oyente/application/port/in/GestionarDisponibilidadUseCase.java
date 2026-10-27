package com.serenity.oyente.application.port.in;
import com.serenity.oyente.domain.model.DisponibilidadOyente;
import java.util.List;
public interface GestionarDisponibilidadUseCase {
 DisponibilidadOyente obtener(Long id);
 DisponibilidadOyente cambiar(Long id, boolean disponible);
 boolean estaDisponible(Long id);
 List<Long> listarDisponibles();
}
