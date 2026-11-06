package com.institucion.reservas.service;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface RecursoService {

    RecursoResponse crear(RecursoRequest request);

    RecursoResponse obtenerPorId(Long id);

    Page<RecursoResponse> listar(EstadoRecurso estado, String tipo, Pageable pageable);

    RecursoResponse actualizar(Long id, RecursoRequest request);

    RecursoResponse actualizarEstado(Long id, EstadoRecurso estado);

    DisponibilidadResponse consultarDisponibilidad(Long id, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
