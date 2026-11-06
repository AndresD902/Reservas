package com.institucion.reservas.service;

import com.institucion.reservas.dto.ReservaRequest;
import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaService {

    ReservaResponse crear(ReservaRequest request);

    ReservaResponse obtenerPorId(Long id);

    Page<ReservaResponse> listar(EstadoReserva estado, Long usuarioId, Long recursoId, Pageable pageable);

    List<ReservaResponse> listarPorUsuario(Long usuarioId);

    List<ReservaResponse> listarPorRecurso(Long recursoId);

    List<ReservaResponse> listarPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    ReservaResponse cancelar(Long id);

    int finalizarVencidas(LocalDateTime ahora);
}
