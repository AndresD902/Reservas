package com.institucion.reservas.mapper;

import com.institucion.reservas.dto.AdministradorResponse;
import com.institucion.reservas.entity.AdministradorEntity;
import org.springframework.stereotype.Component;

@Component
public class AdministradorMapper {

    public AdministradorResponse toResponse(AdministradorEntity entity) {
        return new AdministradorResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getCorreo()
        );
    }
}
