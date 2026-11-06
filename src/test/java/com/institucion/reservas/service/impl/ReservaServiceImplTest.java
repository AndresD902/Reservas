package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.mapper.ReservaMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    @Test
    void listarPorRangoDevuelveLasReservasQueSolapanConElRango() {
        LocalDateTime fechaInicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        LocalDateTime fechaFin = LocalDateTime.of(2026, 11, 6, 12, 0);
        ReservaEntity entity = new ReservaEntity();
        ReservaResponse response = new ReservaResponse(
                1L, 10L, "Ana", 20L, "Sala 1",
                fechaInicio, fechaFin, EstadoReserva.ACTIVA,
                LocalDateTime.of(2026, 11, 5, 10, 0));

        when(reservaRepository.findOverlappingInRange(fechaInicio, fechaFin))
                .thenReturn(List.of(entity));
        when(reservaMapper.toResponse(entity)).thenReturn(response);

        List<ReservaResponse> resultado = reservaService.listarPorRango(fechaInicio, fechaFin);

        assertThat(resultado).containsExactly(response);
        verify(reservaRepository).findOverlappingInRange(fechaInicio, fechaFin);
    }

    @Test
    void listarPorRangoDevuelveListaVaciaCuandoNoHayReservas() {
        LocalDateTime fechaInicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        LocalDateTime fechaFin = LocalDateTime.of(2026, 11, 6, 12, 0);

        when(reservaRepository.findOverlappingInRange(fechaInicio, fechaFin))
                .thenReturn(List.of());

        List<ReservaResponse> resultado = reservaService.listarPorRango(fechaInicio, fechaFin);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarPorRangoRechazaUnRangoConFinAnteriorAlInicio() {
        LocalDateTime fechaInicio = LocalDateTime.of(2026, 11, 6, 12, 0);
        LocalDateTime fechaFin = LocalDateTime.of(2026, 11, 6, 9, 0);

        assertThatThrownBy(() -> reservaService.listarPorRango(fechaInicio, fechaFin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior")
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarPorRangoRechazaUnRangoConFinIgualAlInicio() {
        LocalDateTime fechaInicio = LocalDateTime.of(2026, 11, 6, 9, 0);
        LocalDateTime fechaFin = LocalDateTime.of(2026, 11, 6, 9, 0);

        assertThatThrownBy(() -> reservaService.listarPorRango(fechaInicio, fechaFin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior")
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");

        verifyNoInteractions(reservaRepository);
    }
}
