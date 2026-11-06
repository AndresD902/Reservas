package com.institucion.reservas.controller;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoEstadoRequest;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.exception.ErrorResponseDto;
import com.institucion.reservas.service.RecursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Recursos", description = "Registro, consulta, estado y disponibilidad de recursos")
@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService recursoService;

    @Operation(summary = "Registrar recurso", description = "Crea un recurso.")
    @ApiResponse(responseCode = "201", description = "Recurso creado", content = @Content(schema = @Schema(implementation = RecursoResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDACION", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody RecursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.crear(request));
    }

    @Operation(summary = "Listar recursos", description = "Devuelve una página de recursos con filtros opcionales por estado y tipo.")
    @ApiResponse(responseCode = "200", description = "Página de recursos")
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping
    public Page<RecursoResponse> listar(@RequestParam(required = false) EstadoRecurso estado,
                                        @RequestParam(required = false) String tipo,
                                        Pageable pageable) {
        return recursoService.listar(estado, tipo, pageable);
    }

    @Operation(summary = "Consultar recurso", description = "Devuelve un recurso por su identificador.")
    @ApiResponse(responseCode = "200", description = "Recurso encontrado", content = @Content(schema = @Schema(implementation = RecursoResponse.class)))
    @ApiResponse(responseCode = "404", description = "RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{id}")
    public RecursoResponse obtenerPorId(@PathVariable Long id) {
        return recursoService.obtenerPorId(id);
    }

    @Operation(summary = "Actualizar recurso", description = "Actualiza todos los datos de un recurso.")
    @ApiResponse(responseCode = "200", description = "Recurso actualizado", content = @Content(schema = @Schema(implementation = RecursoResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDACION", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PutMapping("/{id}")
    public RecursoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody RecursoRequest request) {
        return recursoService.actualizar(id, request);
    }

    @Operation(summary = "Cambiar estado de recurso", description = "Cambia un recurso a DISPONIBLE, MANTENIMIENTO o FUERA_DE_SERVICIO.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado", content = @Content(schema = @Schema(implementation = RecursoResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDACION: el estado es obligatorio", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public RecursoResponse actualizarEstado(@PathVariable Long id,
                                            @Valid @RequestBody RecursoEstadoRequest request) {
        return recursoService.actualizarEstado(id, request.estado());
    }

    @Operation(summary = "Consultar disponibilidad", description = "Indica si un recurso está disponible en un rango y lista las reservas solapadas.")
    @ApiResponse(responseCode = "200", description = "Disponibilidad calculada", content = @Content(schema = @Schema(implementation = DisponibilidadResponse.class)))
    @ApiResponse(responseCode = "400", description = "Fechas obligatorias y rango válido", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return recursoService.consultarDisponibilidad(id, fechaInicio, fechaFin);
    }
}
