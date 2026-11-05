package com.institucion.reservas.service;

import com.institucion.reservas.dto.ReservaRequest;
import com.institucion.reservas.dto.ReservaResponse;

import java.util.List;

public interface ReservaService {

    ReservaResponse crear(ReservaRequest request);

    ReservaResponse obtenerPorId(Long id);

    List<ReservaResponse> listarPorUsuario(Long usuarioId);

    List<ReservaResponse> listarPorRecurso(Long recursoId);

    ReservaResponse cancelar(Long id);
}
