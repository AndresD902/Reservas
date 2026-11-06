package com.institucion.reservas.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica las ramas de {@link GlobalExceptionHandler} contra el contrato
 * de error {@code status}, {@code error} y {@code message}.
 * Las rutas {@code /handler/*} solo existen en el contexto de prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerTest.FailingController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns404WithStandardBodyForResourceNotFound() throws Exception {
        mockMvc.perform(get("/handler/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithStandardBodyForBusinessRule() throws Exception {
        mockMvc.perform(get("/handler/business-rule"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("REGLA_DE_NEGOCIO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns409WithStandardBodyForReservationConflict() throws Exception {
        mockMvc.perform(get("/handler/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("RECURSO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithValidacionWhenBeanValidationFails() throws Exception {
        mockMvc.perform(post("/handler/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithSolicitudInvalidaWhenBodyIsUnreadable() throws Exception {
        mockMvc.perform(post("/handler/unreadable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithSolicitudInvalidaForIllegalArgument() throws Exception {
        mockMvc.perform(get("/handler/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithSolicitudInvalidaWhenQueryParameterHasWrongType() throws Exception {
        mockMvc.perform(get("/handler/wrong-type")
                        .param("fecha", "no-es-fecha"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns500WithErrorInternoForUnexpectedException() throws Exception {
        mockMvc.perform(get("/handler/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    record HandlerRequest(@NotBlank String nombre) {
    }

    @RestController
    static class FailingController {

        @GetMapping("/handler/not-found")
        void notFound() {
            throw new ResourceNotFoundException("NO_ENCONTRADO", "El recurso no existe");
        }

        @GetMapping("/handler/business-rule")
        void businessRule() {
            throw new BusinessRuleException("REGLA_DE_NEGOCIO", "Se incumplió una regla de negocio");
        }

        @GetMapping("/handler/conflict")
        void conflict() {
            throw new ReservationConflictException("RECURSO_NO_DISPONIBLE", "El recurso no está disponible");
        }

        @PostMapping("/handler/validation")
        void validation(@Valid @RequestBody HandlerRequest request) {
        }

        @PostMapping("/handler/unreadable")
        void unreadable(@RequestBody HandlerRequest request) {
        }

        @GetMapping("/handler/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("Argumento inválido");
        }

        @GetMapping("/handler/wrong-type")
        void wrongType(@RequestParam LocalDateTime fecha) {
        }

        @GetMapping("/handler/unexpected")
        void unexpected() {
            throw new IllegalStateException("Fallo inesperado");
        }
    }
}
