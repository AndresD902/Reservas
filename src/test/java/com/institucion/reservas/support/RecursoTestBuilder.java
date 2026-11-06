package com.institucion.reservas.support;

import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.repository.RecursoRepository;

public final class RecursoTestBuilder {

    private String nombre = "Recurso de prueba";
    private String descripcion = "Recurso generado para pruebas";
    private String tipo = "SALA";
    private Integer capacidad = 10;
    private EstadoRecurso estado = EstadoRecurso.DISPONIBLE;

    private RecursoTestBuilder() {
    }

    public static RecursoTestBuilder aRecurso() {
        return new RecursoTestBuilder();
    }

    public RecursoTestBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public RecursoTestBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public RecursoTestBuilder withTipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public RecursoTestBuilder withCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
        return this;
    }

    public RecursoTestBuilder withEstado(EstadoRecurso estado) {
        this.estado = estado;
        return this;
    }

    public RecursoEntity build() {
        RecursoEntity recurso = new RecursoEntity();
        recurso.setNombre(nombre);
        recurso.setDescripcion(descripcion);
        recurso.setTipo(tipo);
        recurso.setCapacidad(capacidad);
        recurso.setEstado(estado);
        return recurso;
    }

    public RecursoEntity persist(RecursoRepository repository) {
        return repository.save(build());
    }
}
