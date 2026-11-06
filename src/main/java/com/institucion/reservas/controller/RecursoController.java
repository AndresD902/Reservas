package com.institucion.reservas.controller;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoEstadoRequest;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.service.RecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService recursoService;

    @PostMapping
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody RecursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.crear(request));
    }

    @GetMapping
    public Page<RecursoResponse> listar(@RequestParam(required = false) EstadoRecurso estado,
                                        @RequestParam(required = false) String tipo,
                                        Pageable pageable) {
        return recursoService.listar(estado, tipo, pageable);
    }

    @GetMapping("/{id}")
    public RecursoResponse obtenerPorId(@PathVariable Long id) {
        return recursoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public RecursoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody RecursoRequest request) {
        return recursoService.actualizar(id, request);
    }

    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public RecursoResponse actualizarEstado(@PathVariable Long id,
                                            @Valid @RequestBody RecursoEstadoRequest request) {
        return recursoService.actualizarEstado(id, request.estado());
    }

    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return recursoService.consultarDisponibilidad(id, fechaInicio, fechaFin);
    }
}
