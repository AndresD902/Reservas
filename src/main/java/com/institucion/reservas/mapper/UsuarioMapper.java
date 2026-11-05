package com.institucion.reservas.mapper;

import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioEntity toEntity(UsuarioRequest request) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNombre(request.nombre());
        entity.setDocumento(request.documento());
        entity.setCorreo(request.correo());
        entity.setEstado(request.estado());
        return entity;
    }

    public UsuarioResponse toResponse(UsuarioEntity entity) {
        return new UsuarioResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getDocumento(),
                entity.getCorreo(),
                entity.getEstado()
        );
    }
}
