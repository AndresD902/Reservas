package com.institucion.reservas.controller;

import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import com.institucion.reservas.support.RecursoTestBuilder;
import com.institucion.reservas.support.ReservaTestBuilder;
import com.institucion.reservas.support.UsuarioTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación de HU-15 para los endpoints de recursos: éxito,
 * errores con el contrato homogéneo y validaciones de Bean Validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecursoControllerTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void returns201AndPersistsResourceWhenDataIsValid() throws Exception {
        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recursoJson("Sala Nueva", "Sala amplia", "SALA", 10, "DISPONIBLE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Sala Nueva"))
                .andExpect(jsonPath("$.descripcion").value("Sala amplia"))
                .andExpect(jsonPath("$.tipo").value("SALA"))
                .andExpect(jsonPath("$.capacidad").value(10))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));

        RecursoEntity creado = recursoRepository.findAll().stream()
                .filter(recurso -> "Sala Nueva".equals(recurso.getNombre()))
                .findFirst()
                .orElseThrow();
        assertThat(creado.getCapacidad()).isEqualTo(10);
    }

    @Test
    void returns200WithAllResourcesWhenListing() throws Exception {
        persistirRecurso(RecursoTestBuilder.aRecurso().withNombre("Sala A"));
        persistirRecurso(RecursoTestBuilder.aRecurso().withNombre("Sala B"));

        mockMvc.perform(get("/recursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nombre", hasItems("Sala A", "Sala B")));
    }

    @Test
    void returns200WhenResourceExists() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso()
                .withNombre("Auditorio")
                .withCapacidad(50));

        mockMvc.perform(get("/recursos/{id}", recurso.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recurso.getId()))
                .andExpect(jsonPath("$.nombre").value("Auditorio"))
                .andExpect(jsonPath("$.capacidad").value(50));
    }

    @Test
    void returns404WithStandardErrorBodyWhenResourceDoesNotExist() throws Exception {
        mockMvc.perform(get("/recursos/{id}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns200WithUpdatedResourceWhenDataIsValid() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso()
                .withNombre("Sala vieja")
                .withCapacidad(5));

        mockMvc.perform(put("/recursos/{id}", recurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recursoJson("Sala renovada", "Nueva descripción", "AULA", 20, "MANTENIMIENTO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sala renovada"))
                .andExpect(jsonPath("$.tipo").value("AULA"))
                .andExpect(jsonPath("$.capacidad").value(20))
                .andExpect(jsonPath("$.estado").value("MANTENIMIENTO"));
    }

    @Test
    void returns404WhenUpdatingNonexistentResource() throws Exception {
        mockMvc.perform(put("/recursos/{id}", 999_999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recursoJson("Sala", "Descripción", "SALA", 10, "DISPONIBLE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void returns200WhenStatusIsChangedWithPut() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso());

        mockMvc.perform(put("/recursos/{id}/estado", recurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"MANTENIMIENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("MANTENIMIENTO"));
    }

    @Test
    void returns200WhenStatusIsChangedWithPatch() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso());

        mockMvc.perform(patch("/recursos/{id}/estado", recurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"FUERA_DE_SERVICIO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FUERA_DE_SERVICIO"));
    }

    @Test
    void returns404WhenChangingStatusOfNonexistentResource() throws Exception {
        mockMvc.perform(patch("/recursos/{id}/estado", 999_999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"MANTENIMIENTO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void returns200WithAvailabilityWhenRangeIsFree() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso());
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);

        mockMvc.perform(get("/recursos/{id}/disponibilidad", recurso.getId())
                        .param("fechaInicio", inicio.format(ISO))
                        .param("fechaFin", fin.format(ISO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recursoId").value(recurso.getId()))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.reservasSolapadas").isEmpty());
    }

    @Test
    void returns200WithUnavailableWhenAnActiveReservationOverlaps() throws Exception {
        UsuarioEntity usuario = persistirUsuario();
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso());
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);
        persistirReserva(usuario, recurso, inicio.plusMinutes(30), fin.plusMinutes(30));

        mockMvc.perform(get("/recursos/{id}/disponibilidad", recurso.getId())
                        .param("fechaInicio", inicio.format(ISO))
                        .param("fechaFin", fin.format(ISO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.reservasSolapadas[0].estado").value("ACTIVA"));
    }

    @Test
    void returns400WhenAvailabilityRangeIsInvalid() throws Exception {
        RecursoEntity recurso = persistirRecurso(RecursoTestBuilder.aRecurso());
        LocalDateTime inicio = futureStart();

        mockMvc.perform(get("/recursos/{id}/disponibilidad", recurso.getId())
                        .param("fechaInicio", inicio.format(ISO))
                        .param("fechaFin", inicio.minusHours(1).format(ISO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("REGLA_DE_NEGOCIO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns404WhenAvailabilityResourceDoesNotExist() throws Exception {
        LocalDateTime inicio = futureStart();

        mockMvc.perform(get("/recursos/{id}/disponibilidad", 999_999_999L)
                        .param("fechaInicio", inicio.format(ISO))
                        .param("fechaFin", inicio.plusHours(1).format(ISO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void returns400WithValidacionWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithValidacionWhenCapacidadIsNotPositive() throws Exception {
        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recursoJson("Sala", "Descripción", "SALA", 0, "DISPONIBLE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithSolicitudInvalidaWhenEstadoIsUnknown() throws Exception {
        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recursoJson("Sala", "Descripción", "SALA", 10, "DESCONOCIDO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void returns400WithSolicitudInvalidaWhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"));
    }

    private RecursoEntity persistirRecurso(RecursoTestBuilder builder) {
        return builder.persist(recursoRepository);
    }

    private UsuarioEntity persistirUsuario() {
        return UsuarioTestBuilder.aUsuario().persist(usuarioRepository);
    }

    private void persistirReserva(UsuarioEntity usuario,
                                  RecursoEntity recurso,
                                  LocalDateTime inicio,
                                  LocalDateTime fin) {
        ReservaTestBuilder.aReserva(usuario, recurso)
                .withFechaInicio(inicio)
                .withFechaFin(fin)
                .withEstado(EstadoReserva.ACTIVA)
                .persist(reservaRepository);
    }

    private String recursoJson(String nombre,
                               String descripcion,
                               String tipo,
                               int capacidad,
                               String estado) {
        return """
                {"nombre":"%s","descripcion":"%s","tipo":"%s","capacidad":%d,"estado":"%s"}
                """.formatted(nombre, descripcion, tipo, capacidad, estado);
    }

    private LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
}
