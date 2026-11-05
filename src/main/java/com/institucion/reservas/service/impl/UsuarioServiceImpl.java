package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.UsuarioMapper;
import com.institucion.reservas.repository.UsuarioRepository;
import com.institucion.reservas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByDocumento(request.documento())) {
            throw new BusinessRuleException("DOCUMENTO_DUPLICADO",
                    "Ya existe un usuario con el documento: " + request.documento());
        }
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessRuleException("CORREO_DUPLICADO",
                    "Ya existe un usuario con el correo: " + request.correo());
        }

        UsuarioEntity entity = usuarioMapper.toEntity(request);
        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioMapper.toResponse(obtenerEntidad(id));
    }

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarEstado(Long id, EstadoUsuario estado) {
        if (estado == null) {
            throw new BusinessRuleException("El estado del usuario es obligatorio");
        }

        UsuarioEntity entity = obtenerEntidad(id);
        entity.setEstado(estado);
        return usuarioMapper.toResponse(usuarioRepository.save(entity));
    }

    private UsuarioEntity obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USUARIO_NO_ENCONTRADO",
                        "No existe un usuario con id: " + id));
    }
}
