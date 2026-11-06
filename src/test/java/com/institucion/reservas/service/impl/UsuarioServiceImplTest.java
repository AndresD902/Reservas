package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.mapper.UsuarioMapper;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void listarSinFiltroDelegaEnFindAllYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        UsuarioEntity entity = new UsuarioEntity();
        UsuarioResponse response = new UsuarioResponse(
                1L, "Ana", "1001", "ana@example.com", EstadoUsuario.ACTIVO);
        when(usuarioRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(usuarioMapper.toResponse(entity)).thenReturn(response);

        Page<UsuarioResponse> resultado = usuarioService.listar(null, pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    void listarConEstadoDelegaEnFindByEstadoYDevuelveLaPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 20);
        UsuarioEntity entity = new UsuarioEntity();
        UsuarioResponse response = new UsuarioResponse(
                1L, "Ana", "1001", "ana@example.com", EstadoUsuario.ACTIVO);
        when(usuarioRepository.findByEstado(EstadoUsuario.ACTIVO, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(usuarioMapper.toResponse(entity)).thenReturn(response);

        Page<UsuarioResponse> resultado = usuarioService.listar(EstadoUsuario.ACTIVO, pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(usuarioRepository).findByEstado(EstadoUsuario.ACTIVO, pageable);
    }
}
