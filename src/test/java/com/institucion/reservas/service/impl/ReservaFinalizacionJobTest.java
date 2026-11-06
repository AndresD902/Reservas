package com.institucion.reservas.service.impl;

import com.institucion.reservas.service.ReservaService;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaFinalizacionJobTest {

    private final ReservaService reservaService = mock(ReservaService.class);
    private final ReservaFinalizacionJob job = new ReservaFinalizacionJob(reservaService);

    private ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> attachListAppender() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ReservaFinalizacionJob.class);
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    @Test
    void ejecutaLaFinalizacionConElInstanteActual() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        job.finalizarVencidas();

        LocalDateTime despues = LocalDateTime.now().plusSeconds(1);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reservaService).finalizarVencidas(captor.capture());
        assertFalse(captor.getValue().isBefore(antes));
        assertFalse(captor.getValue().isAfter(despues));
    }

    @Test
    void registraMensajeCuandoSeFinalizanReservas() {
        when(reservaService.finalizarVencidas(any(LocalDateTime.class))).thenReturn(3);
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = attachListAppender();

        job.finalizarVencidas();

        assertThat(appender.list).anyMatch(event -> event.getFormattedMessage().contains("3 reservas vencidas"));
    }

    @Test
    void noRegistraMensajeCuandoNoSeFinalizaNinguna() {
        when(reservaService.finalizarVencidas(any(LocalDateTime.class))).thenReturn(0);
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = attachListAppender();

        job.finalizarVencidas();

        assertThat(appender.list).noneMatch(event -> event.getFormattedMessage().contains("reservas vencidas"));
    }
}
