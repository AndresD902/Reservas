package com.institucion.reservas.controller;

import com.institucion.reservas.dto.AdministradorResponse;
import com.institucion.reservas.dto.LoginRequest;
import com.institucion.reservas.exception.ErrorResponseDto;
import com.institucion.reservas.exception.InvalidCredentialsException;
import com.institucion.reservas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Autenticación", description = "Inicio y cierre de sesión del administrador")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    @Operation(summary = "Iniciar sesión", description = "Autentica al administrador y guarda la sesión en una cookie HttpOnly.")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada", content = @Content(schema = @Schema(implementation = AdministradorResponse.class)))
    @ApiResponse(responseCode = "401", description = "CREDENCIALES_INVALIDAS", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/login")
    public AdministradorResponse login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        AdministradorResponse administrador = authService.login(request);

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                administrador.correo(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return administrador;
    }

    @Operation(summary = "Consultar sesión", description = "Devuelve el administrador autenticado.")
    @ApiResponse(responseCode = "200", description = "Administrador autenticado", content = @Content(schema = @Schema(implementation = AdministradorResponse.class)))
    @ApiResponse(responseCode = "401", description = "CREDENCIALES_INVALIDAS", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping("/me")
    public AdministradorResponse me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new InvalidCredentialsException();
        }

        return authService.obtenerPorCorreo(authentication.getName());
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión actual.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener token CSRF", description = "Devuelve el token CSRF que el cliente debe enviar en las operaciones de escritura.")
    @ApiResponse(responseCode = "200", description = "Token CSRF")
    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        CsrfToken csrfToken = csrfTokenRepository.loadToken(httpRequest);
        if (csrfToken == null) {
            csrfToken = csrfTokenRepository.generateToken(httpRequest);
            csrfTokenRepository.saveToken(csrfToken, httpRequest, httpResponse);
        }

        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "token", csrfToken.getToken()
        );
    }
}
