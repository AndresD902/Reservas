package com.institucion.reservas.mapper;

import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.RecursoEntity;
import org.springframework.stereotype.Component;

@Component
public class RecursoMapper {

    public RecursoEntity toEntity(RecursoRequest request) {
        RecursoEntity entity = new RecursoEntity();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(RecursoEntity entity, RecursoRequest request) {
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setTipo(request.tipo());
        entity.setCapacidad(request.capacidad());
        entity.setEstado(request.estado());
    }

    public RecursoResponse toResponse(RecursoEntity entity) {
        return new RecursoResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getTipo(),
                entity.getCapacidad(),
                entity.getEstado()
        );
    }
}
