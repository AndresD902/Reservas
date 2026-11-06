package com.institucion.reservas.service.impl;

import com.institucion.reservas.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservaFinalizacionJobTest {

    private final ReservaService reservaService = mock(ReservaService.class);
    private final ReservaFinalizacionJob job = new ReservaFinalizacionJob(reservaService);

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
}
