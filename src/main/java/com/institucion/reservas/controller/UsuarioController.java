package com.institucion.reservas.controller;

import com.institucion.reservas.dto.UsuarioEstadoRequest;
import com.institucion.reservas.dto.UsuarioRequest;
import com.institucion.reservas.dto.UsuarioResponse;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.exception.ErrorResponseDto;
import com.institucion.reservas.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Registro, consulta y gestión de estado de usuarios")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Registrar usuario", description = "Crea un usuario. Falla con 400 si el documento o el correo ya existen.")
    @ApiResponse(responseCode = "201", description = "Usuario creado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados: VALIDACION, DOCUMENTO_DUPLICADO, CORREO_DUPLICADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve una página de usuarios con filtro opcional por estado.")
    @ApiResponse(responseCode = "200", description = "Página de usuarios")
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping
    public Page<UsuarioResponse> listar(@RequestParam(required = false) EstadoUsuario estado,
                                        Pageable pageable) {
        return usuarioService.listar(estado, pageable);
    }

    @Operation(summary = "Consultar usuario", description = "Devuelve un usuario por su identificador.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "404", description = "USUARIO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @Operation(summary = "Cambiar estado de usuario", description = "Activa o inactiva un usuario.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDACION: el estado es obligatorio", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "USUARIO_NO_ENCONTRADO", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public UsuarioResponse actualizarEstado(@PathVariable Long id,
                                            @Valid @RequestBody UsuarioEstadoRequest request) {
        return usuarioService.actualizarEstado(id, request.estado());
    }
}
