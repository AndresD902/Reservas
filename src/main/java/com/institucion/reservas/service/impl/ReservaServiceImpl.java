package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.ReservaRequest;
import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ReservationConflictException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.ReservaMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import com.institucion.reservas.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaServiceImpl implements ReservaService {

    private static final int MAX_RESERVAS_ACTIVAS = 3;

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoRepository recursoRepository;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        validarSolicitud(request);

        UsuarioEntity usuario = obtenerUsuarioBloqueado(request.usuarioId());
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessRuleException("USUARIO_INACTIVO",
                    "El usuario no está activo y no puede realizar reservas");
        }

        RecursoEntity recurso = obtenerRecursoBloqueado(request.recursoId());
        if (recurso.getEstado() != EstadoRecurso.DISPONIBLE) {
            throw new ReservationConflictException("RECURSO_NO_DISPONIBLE",
                    "El recurso no se encuentra disponible para reservas");
        }

        validarFechas(request.fechaInicio(), request.fechaFin());

        long reservasActivas = reservaRepository.countByUsuarioIdAndEstado(
                usuario.getId(), EstadoReserva.ACTIVA);
        if (reservasActivas >= MAX_RESERVAS_ACTIVAS) {
            throw new BusinessRuleException("LIMITE_RESERVAS_ACTIVAS",
                    "El usuario ya tiene el máximo de 3 reservas activas simultáneas");
        }

        long reservasSolapadas = reservaRepository.countOverlappingReservas(
                recurso.getId(),
                request.fechaInicio(),
                request.fechaFin(),
                EstadoReserva.ACTIVA);
        if (reservasSolapadas > 0) {
            throw new ReservationConflictException("RECURSO_NO_DISPONIBLE",
                    "El recurso ya se encuentra reservado para este horario");
        }

        ReservaEntity entity = reservaMapper.toEntity(
                request.fechaInicio(), request.fechaFin(), usuario, recurso);
        return reservaMapper.toResponse(reservaRepository.save(entity));
    }

    @Override
    public ReservaResponse obtenerPorId(Long id) {
        ReservaEntity entity = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVA_NO_ENCONTRADA",
                        "No existe una reserva con id: " + id));
        return reservaMapper.toResponse(entity);
    }

    @Override
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("USUARIO_NO_ENCONTRADO",
                    "No existe un usuario con id: " + usuarioId);
        }
        return reservaRepository.findByUsuarioIdOrderByFechaInicioAsc(usuarioId).stream()
                .map(reservaMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReservaResponse> listarPorRecurso(Long recursoId) {
        if (!recursoRepository.existsById(recursoId)) {
            throw new ResourceNotFoundException("RECURSO_NO_ENCONTRADO",
                    "No existe un recurso con id: " + recursoId);
        }
        return reservaRepository.findByRecursoIdOrderByFechaInicioAsc(recursoId).stream()
                .map(reservaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse cancelar(Long id) {
        ReservaEntity entity = reservaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVA_NO_ENCONTRADA",
                        "No existe una reserva con id: " + id));

        if (entity.getEstado() == EstadoReserva.FINALIZADA) {
            throw new BusinessRuleException("RESERVA_FINALIZADA",
                    "No se puede cancelar una reserva que se encuentra finalizada");
        }
        if (entity.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessRuleException("RESERVA_CANCELADA",
                    "La reserva ya se encuentra cancelada");
        }

        entity.setEstado(EstadoReserva.CANCELADA);
        return reservaMapper.toResponse(reservaRepository.save(entity));
    }

    private void validarSolicitud(ReservaRequest request) {
        if (request == null || request.usuarioId() == null || request.recursoId() == null
                || request.fechaInicio() == null || request.fechaFin() == null) {
            throw new BusinessRuleException("Los datos de la reserva son obligatorios");
        }
    }

    private UsuarioEntity obtenerUsuarioBloqueado(Long usuarioId) {
        return usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("USUARIO_NO_ENCONTRADO",
                        "No existe un usuario con id: " + usuarioId));
    }

    private RecursoEntity obtenerRecursoBloqueado(Long recursoId) {
        return recursoRepository.findByIdForUpdate(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("RECURSO_NO_ENCONTRADO",
                        "No existe un recurso con id: " + recursoId));
    }

    private void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new BusinessRuleException("FECHAS_INVALIDAS",
                    "La fecha de fin debe ser estrictamente posterior a la fecha de inicio");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (fechaInicio.isBefore(ahora)) {
            throw new BusinessRuleException("FECHA_PASADA",
                    "La fecha de inicio no puede ser anterior al momento actual");
        }
        if (fechaFin.isBefore(ahora)) {
            throw new BusinessRuleException("FECHA_PASADA",
                    "La fecha de fin no puede ser anterior al momento actual");
        }
    }
}
