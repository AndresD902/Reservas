package com.institucion.reservas.controller;

import com.institucion.reservas.dto.ReservaRequest;
import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.exception.ErrorResponseDto;
import com.institucion.reservas.service.ReservaService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Reservas", description = "Creación, cancelación y consulta de reservas")
@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @Operation(summary = "Crear reserva", description = "Crea una reserva tras validar usuario activo, recurso disponible, fechas, solapamiento y límite de 3 activas.")
    @ApiResponse(responseCode = "201", description = "Reserva creada", content = @Content(schema = @Schema(implementation = ReservaResponse.class)))
    @ApiResponse(responseCode = "400", description = "USUARIO_INACTIVO, FECHAS_INVALIDAS, FECHA_PASADA, LIMITE_RESERVAS_ACTIVAS, VALIDACION", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "USUARIO_NO_ENCONTRADO, RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "RECURSO_NO_DISPONIBLE", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(request));
    }

    @Operation(summary = "Listar reservas", description = "Devuelve una página de reservas con filtros opcionales por estado, usuario y recurso.")
    @ApiResponse(responseCode = "200", description = "Página de reservas")
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping
    public Page<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado,
                                        @RequestParam(required = false) Long usuarioId,
                                        @RequestParam(required = false) Long recursoId,
                                        Pageable pageable) {
        return reservaService.listar(estado, usuarioId, recursoId, pageable);
    }

    @Operation(summary = "Consultar reservas por rango", description = "Devuelve las reservas cuyo intervalo se solapa con el rango solicitado (semántica semiabierta) y exige fin posterior a inicio.")
    @ApiResponse(responseCode = "200", description = "Reservas solapadas")
    @ApiResponse(responseCode = "400", description = "FECHAS_INVALIDAS", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/rango")
    public List<ReservaResponse> listarPorRango(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return reservaService.listarPorRango(fechaInicio, fechaFin);
    }

    @Operation(summary = "Consultar reserva", description = "Devuelve una reserva por su identificador.")
    @ApiResponse(responseCode = "200", description = "Reserva encontrada", content = @Content(schema = @Schema(implementation = ReservaResponse.class)))
    @ApiResponse(responseCode = "404", description = "RESERVA_NO_ENCONTRADA", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{id}")
    public ReservaResponse obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id);
    }

    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva ACTIVA. Rechaza las FINALIZADA y las ya CANCELADA.")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada", content = @Content(schema = @Schema(implementation = ReservaResponse.class)))
    @ApiResponse(responseCode = "400", description = "RESERVA_FINALIZADA, RESERVA_CANCELADA", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "RESERVA_NO_ENCONTRADA", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @DeleteMapping("/{id}")
    public ReservaResponse cancelar(@PathVariable Long id) {
        return reservaService.cancelar(id);
    }

    @Operation(summary = "Consultar reservas de un usuario", description = "Devuelve las reservas de un usuario ordenadas por fecha de inicio.")
    @ApiResponse(responseCode = "200", description = "Reservas del usuario")
    @ApiResponse(responseCode = "404", description = "USUARIO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return reservaService.listarPorUsuario(usuarioId);
    }

    @Operation(summary = "Consultar reservas de un recurso", description = "Devuelve las reservas de un recurso ordenadas por fecha de inicio.")
    @ApiResponse(responseCode = "200", description = "Reservas del recurso")
    @ApiResponse(responseCode = "404", description = "RECURSO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/recurso/{recursoId}")
    public List<ReservaResponse> listarPorRecurso(@PathVariable Long recursoId) {
        return reservaService.listarPorRecurso(recursoId);
    }
}
