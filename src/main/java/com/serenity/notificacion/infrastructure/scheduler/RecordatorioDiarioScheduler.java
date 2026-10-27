package com.serenity.notificacion.infrastructure.scheduler;

import com.serenity.notificacion.application.RecordatorioDiarioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecordatorioDiarioScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(RecordatorioDiarioScheduler.class);

    private final RecordatorioDiarioService recordatorioService;

    public RecordatorioDiarioScheduler(
            RecordatorioDiarioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @Scheduled(
            cron = "${app.correo.recordatorios.cron:0 0 18 * * *}",
            zone = "${app.correo.recordatorios.zona:America/Bogota}"
    )
    public void ejecutar() {
        log.info("Iniciando revisión de recordatorios de Serenity");

        recordatorioService.enviarRecordatoriosPendientes();

        log.info("Finalizó la revisión de recordatorios de Serenity");
    }
}