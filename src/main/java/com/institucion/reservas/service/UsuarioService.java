package com.institucion.reservas.service;

import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse obtenerPorId(Long id);

    Page<UsuarioResponse> listar(EstadoUsuario estado, Pageable pageable);

    UsuarioResponse actualizarEstado(Long id, EstadoUsuario estado);
}
