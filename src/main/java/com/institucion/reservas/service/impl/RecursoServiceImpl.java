package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.RecursoMapper;
import com.institucion.reservas.mapper.ReservaMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.service.RecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecursoServiceImpl implements RecursoService {

    private final RecursoRepository recursoRepository;
    private final ReservaRepository reservaRepository;
    private final RecursoMapper recursoMapper;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public RecursoResponse crear(RecursoRequest request) {
        RecursoEntity entity = recursoMapper.toEntity(request);
        return recursoMapper.toResponse(recursoRepository.save(entity));
    }

    @Override
    public RecursoResponse obtenerPorId(Long id) {
        return recursoMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public List<RecursoResponse> listar() {
        return recursoRepository.findAll().stream()
                .map(recursoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecursoResponse actualizar(Long id, RecursoRequest request) {
        RecursoEntity entity = obtenerEntidad(id);
        recursoMapper.updateEntity(entity, request);
        return recursoMapper.toResponse(recursoRepository.save(entity));
    }

    @Override
    @Transactional
    public RecursoResponse actualizarEstado(Long id, EstadoRecurso estado) {
        if (estado == null) {
            throw new BusinessRuleException("El estado del recurso es obligatorio");
        }

        RecursoEntity entity = obtenerEntidad(id);
        entity.setEstado(estado);
        return recursoMapper.toResponse(recursoRepository.save(entity));
    }

    @Override
    public DisponibilidadResponse consultarDisponibilidad(Long id,
                                                          LocalDateTime fechaInicio,
                                                          LocalDateTime fechaFin) {
        validarRangoFechas(fechaInicio, fechaFin);
        RecursoEntity recurso = obtenerEntidad(id);

        List<ReservaEntity> solapadas = reservaRepository.findOverlappingReservas(
                id, fechaInicio, fechaFin, EstadoReserva.ACTIVA);

        boolean disponible = recurso.getEstado() == EstadoRecurso.DISPONIBLE && solapadas.isEmpty();

        return new DisponibilidadResponse(
                recurso.getId(),
                fechaInicio,
                fechaFin,
                disponible,
                solapadas.stream().map(reservaMapper::toResponse).toList()
        );
    }

    private RecursoEntity obtenerEntidad(Long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RECURSO_NO_ENCONTRADO",
                        "No existe un recurso con id: " + id));
    }

    private void validarRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new BusinessRuleException("Las fechas de inicio y fin son obligatorias");
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new BusinessRuleException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}
