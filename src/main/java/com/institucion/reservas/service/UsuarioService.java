package com.institucion.reservas.service;

import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse obtenerPorId(Long id);

    List<UsuarioResponse> listar();

    UsuarioResponse actualizarEstado(Long id, EstadoUsuario estado);
}
