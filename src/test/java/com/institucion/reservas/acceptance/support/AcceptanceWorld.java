package com.institucion.reservas.acceptance.support;

import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

/**
 * Estado compartido entre las definiciones de pasos de un mismo escenario.
 * El alcance de Cucumber garantiza que cada escenario recibe un estado limpio.
 */
public class AcceptanceWorld {

    private MvcResult lastResponse;
    private long usuarioId;
    private long recursoId;
    private long reservaId;
    private LocalDateTime reservaInicio;
    private LocalDateTime reservaFin;

    public MvcResult getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(MvcResult lastResponse) {
        this.lastResponse = lastResponse;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public long getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(long recursoId) {
        this.recursoId = recursoId;
    }

    public long getReservaId() {
        return reservaId;
    }

    public void setReservaId(long reservaId) {
        this.reservaId = reservaId;
    }

    public LocalDateTime getReservaInicio() {
        return reservaInicio;
    }

    public void setReservaInicio(LocalDateTime reservaInicio) {
        this.reservaInicio = reservaInicio;
    }

    public LocalDateTime getReservaFin() {
        return reservaFin;
    }

    public void setReservaFin(LocalDateTime reservaFin) {
        this.reservaFin = reservaFin;
    }
}
