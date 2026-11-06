package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.UsuarioMapper;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    private UsuarioServiceImpl service;

    private UsuarioEntity entity;
    private UsuarioRequest request;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(usuarioRepository, usuarioMapper);

        entity = new UsuarioEntity();
        entity.setId(1L);
        entity.setNombre("Ana");
        entity.setDocumento("123");
        entity.setCorreo("ana@example.com");
        entity.setEstado(EstadoUsuario.ACTIVO);

        request = new UsuarioRequest("Ana", "123", "ana@example.com", EstadoUsuario.ACTIVO);
    }

    @Test
    void creaUsuarioCuandoDocumentoYCorreoSonUnicos() {
        when(usuarioRepository.existsByDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> {
            UsuarioEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UsuarioResponse response = service.crear(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Ana");
        assertThat(response.documento()).isEqualTo("123");
        assertThat(response.correo()).isEqualTo("ana@example.com");
        assertThat(response.estado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void rechazaDocumentoDuplicado() {
        when(usuarioRepository.existsByDocumento("123")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("123")
                .extracting("error")
                .isEqualTo("DOCUMENTO_DUPLICADO");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("CORREO_DUPLICADO");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void validaDocumentoAntesQueCorreo() {
        when(usuarioRepository.existsByDocumento("123")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("DOCUMENTO_DUPLICADO");
    }

    @Test
    void obtieneUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void rechazaObtenerUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("USUARIO_NO_ENCONTRADO");
    }

    @Test
    void listaUsuarios() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(usuarioRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        assertThat(service.listar(null, pageable).getContent()).hasSize(1);
    }

    @Test
    void listaUsuariosVaciaCuandoNoHayRegistros() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(usuarioRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(service.listar(null, pageable).getContent()).isEmpty();
    }

    @Test
    void actualizaEstadoAInactivo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(entity)).thenReturn(entity);

        UsuarioResponse response = service.actualizarEstado(1L, EstadoUsuario.INACTIVO);

        assertThat(response.estado()).isEqualTo(EstadoUsuario.INACTIVO);
        assertThat(entity.getEstado()).isEqualTo(EstadoUsuario.INACTIVO);
    }

    @Test
    void actualizaEstadoAACTIVO() {
        entity.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(entity)).thenReturn(entity);

        assertThat(service.actualizarEstado(1L, EstadoUsuario.ACTIVO).estado())
                .isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void rechazaActualizacionDeEstadoNulo() {
        assertThatThrownBy(() -> service.actualizarEstado(1L, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorio");
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void rechazaActualizacionDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarEstado(99L, EstadoUsuario.INACTIVO))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("USUARIO_NO_ENCONTRADO");
    }
}
