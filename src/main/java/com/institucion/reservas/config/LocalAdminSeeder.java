package com.institucion.reservas.config;

import com.institucion.reservas.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalAdminSeeder implements ApplicationRunner {

    private final AuthService authService;

    @Value("${reservas.admin.nombre:Administrador local}")
    private String nombre;

    @Value("${reservas.admin.email:admin@example.com}")
    private String email;

    @Value("${reservas.admin.password:admin123}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (authService.existeCorreo(email)) {
            return;
        }

        authService.crearAdministrador(nombre, email, password);
        log.info("Administrador local creado con el correo {}", email);
    }
}
