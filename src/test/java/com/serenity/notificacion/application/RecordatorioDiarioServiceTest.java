package com.serenity.notificacion.application;

import com.serenity.ejercicio.infrastructure.adapter.out.persistence.SesionJpaRepository;
import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import com.serenity.notificacion.infrastructure.persistence.RecordatorioEnviadoJpaEntity;
import com.serenity.notificacion.infrastructure.persistence.RecordatorioEnviadoJpaRepository;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordatorioDiarioServiceTest {
    @Mock UsuarioJpaRepository usuarios;
    @Mock SesionJpaRepository sesiones;
    @Mock RecordatorioEnviadoJpaRepository enviados;
    @Mock ProveedorCorreoPort correo;

    @Test void noEnviaDosRecordatoriosAlMismoUsuarioElMismoDia() {
        var usuario = new UsuarioJpaEntity(4L, "Ana", "ana@b.co", "hash", 1, 0, 0, 0);
        when(usuarios.findByRecordatoriosEmailTrue()).thenReturn(List.of(usuario));
        AtomicBoolean registrado = new AtomicBoolean();
        when(enviados.existsByUsuarioIdAndFecha(eq(4L), any())).thenAnswer(inv -> registrado.get());
        when(sesiones.existsByUsuarioIdAndFechaCompletadoGreaterThanEqualAndFechaCompletadoLessThan(eq(4L), any(), any())).thenReturn(false);
        when(enviados.saveAndFlush(any(RecordatorioEnviadoJpaEntity.class))).thenAnswer(inv -> {
            registrado.set(true);
            return inv.getArgument(0);
        });
        var servicio = new RecordatorioDiarioService(usuarios, sesiones, enviados, correo);

        servicio.enviarRecordatoriosPendientes();
        servicio.enviarRecordatoriosPendientes();

        verify(correo, times(1)).enviar(eq("ana@b.co"), anyString(), anyString(), anyString());
        verify(enviados, times(1)).saveAndFlush(any(RecordatorioEnviadoJpaEntity.class));
    }
}
