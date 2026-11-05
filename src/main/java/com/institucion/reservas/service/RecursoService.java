package com.institucion.reservas.service;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;

import java.time.LocalDateTime;
import java.util.List;

public interface RecursoService {

    RecursoResponse crear(RecursoRequest request);

    RecursoResponse obtenerPorId(Long id);

    List<RecursoResponse> listar();

    RecursoResponse actualizar(Long id, RecursoRequest request);

    RecursoResponse actualizarEstado(Long id, EstadoRecurso estado);

    DisponibilidadResponse consultarDisponibilidad(Long id, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
