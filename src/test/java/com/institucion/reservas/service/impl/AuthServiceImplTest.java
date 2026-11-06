package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.AdministradorResponse;
import com.institucion.reservas.dto.LoginRequest;
import com.institucion.reservas.entity.AdministradorEntity;
import com.institucion.reservas.exception.InvalidCredentialsException;
import com.institucion.reservas.mapper.AdministradorMapper;
import com.institucion.reservas.repository.AdministradorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private AdministradorMapper administradorMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginDevuelveElAdministradorCuandoLasCredencialesSonValidas() {
        AdministradorEntity entity = administrador("admin@example.com", "hash");
        AdministradorResponse response = new AdministradorResponse(1L, "Admin", "admin@example.com");
        when(administradorRepository.findByCorreo("admin@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("secreta", "hash")).thenReturn(true);
        when(administradorMapper.toResponse(entity)).thenReturn(response);

        AdministradorResponse resultado = authService.login(new LoginRequest("admin@example.com", "secreta"));

        assertThat(resultado).isEqualTo(response);
    }

    @Test
    void loginNormalizaElCorreoAntesDeBuscarlo() {
        AdministradorEntity entity = administrador("admin@example.com", "hash");
        when(administradorRepository.findByCorreo("admin@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("secreta", "hash")).thenReturn(true);
        when(administradorMapper.toResponse(entity))
                .thenReturn(new AdministradorResponse(1L, "Admin", "admin@example.com"));

        authService.login(new LoginRequest("  ADMIN@Example.COM  ", "secreta"));

        verify(administradorRepository).findByCorreo("admin@example.com");
    }

    @Test
    void loginFallaCuandoElCorreoNoExiste() {
        when(administradorRepository.findByCorreo("nadie@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie@example.com", "secreta")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches("secreta", "hash");
    }

    @Test
    void loginFallaCuandoLaContrasenaNoCoincide() {
        AdministradorEntity entity = administrador("admin@example.com", "hash");
        when(administradorRepository.findByCorreo("admin@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("incorrecta", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@example.com", "incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(administradorMapper, never()).toResponse(entity);
    }

    @Test
    void obtenerPorCorreoDevuelveElAdministradorNormalizado() {
        AdministradorEntity entity = administrador("admin@example.com", "hash");
        AdministradorResponse response = new AdministradorResponse(1L, "Admin", "admin@example.com");
        when(administradorRepository.findByCorreo("admin@example.com")).thenReturn(Optional.of(entity));
        when(administradorMapper.toResponse(entity)).thenReturn(response);

        assertThat(authService.obtenerPorCorreo(" Admin@Example.com ")).isEqualTo(response);
    }

    @Test
    void obtenerPorCorreoFallaCuandoNoExiste() {
        when(administradorRepository.findByCorreo("nadie@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.obtenerPorCorreo("nadie@example.com"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void existeCorreoConsultaNormalizado() {
        when(administradorRepository.existsByCorreo("admin@example.com")).thenReturn(true);

        assertThat(authService.existeCorreo(" ADMIN@example.com ")).isTrue();
    }

    @Test
    void crearAdministradorGuardaElHashYNormalizaElCorreo() {
        when(administradorRepository.existsByCorreo("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta")).thenReturn("hash-bcrypt");

        authService.crearAdministrador("Admin", " ADMIN@Example.com ", "secreta");

        ArgumentCaptor<AdministradorEntity> captor = ArgumentCaptor.forClass(AdministradorEntity.class);
        verify(administradorRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Admin");
        assertThat(captor.getValue().getCorreo()).isEqualTo("admin@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash-bcrypt");
    }

    @Test
    void crearAdministradorNoHaceNadaSiElCorreoYaExiste() {
        when(administradorRepository.existsByCorreo("admin@example.com")).thenReturn(true);

        authService.crearAdministrador("Admin", "admin@example.com", "secreta");

        verify(administradorRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    private AdministradorEntity administrador(String correo, String hash) {
        AdministradorEntity entity = new AdministradorEntity();
        entity.setId(1L);
        entity.setNombre("Admin");
        entity.setCorreo(correo);
        entity.setPasswordHash(hash);
        return entity;
    }
}
