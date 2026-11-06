package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.mapper.RecursoMapper;
import com.institucion.reservas.repository.RecursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursoServiceImplTest {

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private RecursoMapper recursoMapper;

    @InjectMocks
    private RecursoServiceImpl recursoService;

    @Test
    void listarSinFiltrosDelegaEnFindAllYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        RecursoEntity entity = new RecursoEntity();
        RecursoResponse response = new RecursoResponse(
                1L, "Sala 1", "Sala principal", "SALA", 10, EstadoRecurso.DISPONIBLE);
        when(recursoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(recursoMapper.toResponse(entity)).thenReturn(response);

        Page<RecursoResponse> resultado = recursoService.listar(null, null, pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(recursoRepository).findAll(pageable);
    }

    @Test
    void listarConEstadoDelegaEnFindByEstadoYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        RecursoEntity entity = new RecursoEntity();
        RecursoResponse response = new RecursoResponse(
                1L, "Sala 1", "Sala principal", "SALA", 10, EstadoRecurso.DISPONIBLE);
        when(recursoRepository.findByEstado(EstadoRecurso.DISPONIBLE, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(recursoMapper.toResponse(entity)).thenReturn(response);

        Page<RecursoResponse> resultado = recursoService.listar(EstadoRecurso.DISPONIBLE, null, pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(recursoRepository).findByEstado(EstadoRecurso.DISPONIBLE, pageable);
    }

    @Test
    void listarConTipoDelegaEnFindByTipoIgnoreCaseYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        RecursoEntity entity = new RecursoEntity();
        RecursoResponse response = new RecursoResponse(
                1L, "Sala 1", "Sala principal", "SALA", 10, EstadoRecurso.DISPONIBLE);
        when(recursoRepository.findByTipoIgnoreCase("SALA", pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(recursoMapper.toResponse(entity)).thenReturn(response);

        Page<RecursoResponse> resultado = recursoService.listar(null, "SALA", pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(recursoRepository).findByTipoIgnoreCase("SALA", pageable);
    }

    @Test
    void listarConEstadoYTipoDelegaEnLaConsultaCombinadaYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        RecursoEntity entity = new RecursoEntity();
        RecursoResponse response = new RecursoResponse(
                1L, "Sala 1", "Sala principal", "SALA", 10, EstadoRecurso.DISPONIBLE);
        when(recursoRepository.findByEstadoAndTipoIgnoreCase(EstadoRecurso.DISPONIBLE, "SALA", pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(recursoMapper.toResponse(entity)).thenReturn(response);

        Page<RecursoResponse> resultado = recursoService.listar(EstadoRecurso.DISPONIBLE, "SALA", pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(recursoRepository).findByEstadoAndTipoIgnoreCase(EstadoRecurso.DISPONIBLE, "SALA", pageable);
    }

    @Test
    void listarConTipoEnBlancoNoAplicaElFiltroDeTipo() {
        Pageable pageable = PageRequest.of(0, 20);
        RecursoEntity entity = new RecursoEntity();
        RecursoResponse response = new RecursoResponse(
                1L, "Sala 1", "Sala principal", "SALA", 10, EstadoRecurso.DISPONIBLE);
        when(recursoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(recursoMapper.toResponse(entity)).thenReturn(response);

        Page<RecursoResponse> resultado = recursoService.listar(null, "  ", pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(recursoRepository).findAll(pageable);
    }
}
