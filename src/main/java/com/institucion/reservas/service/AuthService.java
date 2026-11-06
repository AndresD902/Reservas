package com.institucion.reservas.service;

import com.institucion.reservas.dto.AdministradorResponse;
import com.institucion.reservas.dto.LoginRequest;

public interface AuthService {

    AdministradorResponse login(LoginRequest request);

    AdministradorResponse obtenerPorCorreo(String correo);

    boolean existeCorreo(String correo);

    void crearAdministrador(String nombre, String correo, String password);
}
