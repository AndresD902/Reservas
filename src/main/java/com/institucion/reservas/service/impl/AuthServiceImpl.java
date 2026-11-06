package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.AdministradorResponse;
import com.institucion.reservas.dto.LoginRequest;
import com.institucion.reservas.entity.AdministradorEntity;
import com.institucion.reservas.exception.InvalidCredentialsException;
import com.institucion.reservas.mapper.AdministradorMapper;
import com.institucion.reservas.repository.AdministradorRepository;
import com.institucion.reservas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AdministradorRepository administradorRepository;
    private final AdministradorMapper administradorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdministradorResponse login(LoginRequest request) {
        AdministradorEntity entity = administradorRepository
                .findByCorreo(normalizarCorreo(request.correo()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), entity.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return administradorMapper.toResponse(entity);
    }

    @Override
    public AdministradorResponse obtenerPorCorreo(String correo) {
        return administradorRepository
                .findByCorreo(normalizarCorreo(correo))
                .map(administradorMapper::toResponse)
                .orElseThrow(InvalidCredentialsException::new);
    }

    @Override
    public boolean existeCorreo(String correo) {
        return administradorRepository.existsByCorreo(normalizarCorreo(correo));
    }

    @Override
    @Transactional
    public void crearAdministrador(String nombre, String correo, String password) {
        if (existeCorreo(correo)) {
            return;
        }

        AdministradorEntity entity = new AdministradorEntity();
        entity.setNombre(nombre);
        entity.setCorreo(normalizarCorreo(correo));
        entity.setPasswordHash(passwordEncoder.encode(password));
        administradorRepository.save(entity);
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
