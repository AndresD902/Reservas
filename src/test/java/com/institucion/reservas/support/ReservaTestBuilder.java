package com.institucion.reservas.support;

import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.ReservaRepository;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ReservaTestBuilder {

    private final UsuarioEntity usuario;
    private final RecursoEntity recurso;
    private LocalDateTime fechaInicio = LocalDateTime.now().plusDays(1);
    private LocalDateTime fechaFin;
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    private ReservaTestBuilder(UsuarioEntity usuario, RecursoEntity recurso) {
        this.usuario = Objects.requireNonNull(usuario, "El usuario es obligatorio");
        this.recurso = Objects.requireNonNull(recurso, "El recurso es obligatorio");
    }

    public static ReservaTestBuilder aReserva(UsuarioEntity usuario, RecursoEntity recurso) {
        return new ReservaTestBuilder(usuario, recurso);
    }

    public ReservaTestBuilder withFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
        return this;
    }

    public ReservaTestBuilder withFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
        return this;
    }

    public ReservaTestBuilder withEstado(EstadoReserva estado) {
        this.estado = estado;
        return this;
    }

    public ReservaEntity build() {
        ReservaEntity reserva = new ReservaEntity();
        reserva.setUsuario(usuario);
        reserva.setRecurso(recurso);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin != null ? fechaFin : fechaInicio.plusHours(1));
        reserva.setEstado(estado);
        return reserva;
    }

    public ReservaEntity persist(ReservaRepository repository) {
        return repository.save(build());
    }
}
