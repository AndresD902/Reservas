package com.institucion.reservas.controller;

import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.GlobalExceptionHandler;
import com.institucion.reservas.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReservaController(reservaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarPorRangoRespondeConLasReservasDelServicio() throws Exception {
        ReservaResponse response = new ReservaResponse(
                1L, 10L, "Ana", 20L, "Sala 1",
                LocalDateTime.of(2026, 11, 6, 9, 0),
                LocalDateTime.of(2026, 11, 6, 12, 0),
                EstadoReserva.ACTIVA,
                LocalDateTime.of(2026, 11, 5, 10, 0));
        when(reservaService.listarPorRango(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/reservas/rango")
                        .param("fechaInicio", "2026-11-06T09:00:00")
                        .param("fechaFin", "2026-11-06T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    void listarPorRangoConFinAnteriorAlInicioResponde400() throws Exception {
        when(reservaService.listarPorRango(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new BusinessRuleException("FECHAS_INVALIDAS",
                        "La fecha de fin debe ser estrictamente posterior a la fecha de inicio"));

        mockMvc.perform(get("/reservas/rango")
                        .param("fechaInicio", "2026-11-06T12:00:00")
                        .param("fechaFin", "2026-11-06T09:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FECHAS_INVALIDAS"));
    }
}
