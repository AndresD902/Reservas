package com.institucion.reservas.service.impl;

import com.institucion.reservas.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaFinalizacionJob {

    private final ReservaService reservaService;

    @Scheduled(fixedDelayString = "${reservas.finalizacion.frecuencia:PT5M}")
    public void finalizarVencidas() {
        int finalizadas = reservaService.finalizarVencidas(LocalDateTime.now());
        if (finalizadas > 0) {
            log.info("Se finalizaron {} reservas vencidas", finalizadas);
        }
    }
}
