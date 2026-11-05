package com.institucion.reservas.mapper;

import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservaMapper {

    public ReservaEntity toEntity(LocalDateTime fechaInicio,
                                  LocalDateTime fechaFin,
                                  UsuarioEntity usuario,
                                  RecursoEntity recurso) {
        ReservaEntity entity = new ReservaEntity();
        entity.setUsuario(usuario);
        entity.setRecurso(recurso);
        entity.setFechaInicio(fechaInicio);
        entity.setFechaFin(fechaFin);
        entity.setEstado(EstadoReserva.ACTIVA);
        return entity;
    }

    public ReservaResponse toResponse(ReservaEntity entity) {
        return new ReservaResponse(
                entity.getId(),
                entity.getUsuario().getId(),
                entity.getUsuario().getNombre(),
                entity.getRecurso().getId(),
                entity.getRecurso().getNombre(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getEstado(),
                entity.getFechaCreacion()
        );
    }
}
