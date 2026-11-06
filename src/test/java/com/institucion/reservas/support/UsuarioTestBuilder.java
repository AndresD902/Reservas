package com.institucion.reservas.support;

import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.UsuarioRepository;

import java.util.concurrent.ThreadLocalRandom;

public final class UsuarioTestBuilder {

    private String nombre = "Usuario de prueba";
    private String documento = documentoAleatorio();
    private String correo;
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    private UsuarioTestBuilder() {
    }

    public static UsuarioTestBuilder aUsuario() {
        return new UsuarioTestBuilder();
    }

    public UsuarioTestBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public UsuarioTestBuilder withDocumento(String documento) {
        this.documento = documento;
        return this;
    }

    public UsuarioTestBuilder withCorreo(String correo) {
        this.correo = correo;
        return this;
    }

    public UsuarioTestBuilder withEstado(EstadoUsuario estado) {
        this.estado = estado;
        return this;
    }

    public UsuarioEntity build() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(nombre);
        usuario.setDocumento(documento);
        usuario.setCorreo(correo != null ? correo : "usuario-" + documento + "@example.com");
        usuario.setEstado(estado);
        return usuario;
    }

    public UsuarioEntity persist(UsuarioRepository repository) {
        return repository.save(build());
    }

    private static String documentoAleatorio() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000L, 100_000_000L));
    }
}
