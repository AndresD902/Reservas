package com.institucion.reservas.controller;

import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación de HU-15 para los endpoints de reservas: éxito,
 * rechazos de reglas de negocio, errores con el contrato homogéneo y
 * validaciones de Bean Validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservaControllerTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void returns201AndPersistsReservationWhenDataIsValid() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(), inicio, fin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.recursoId").value(recurso.getId()))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void returns400WithUsuarioInactivoWhenUserIsInactive() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.INACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(), inicio, inicio.plusHours(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("USUARIO_INACTIVO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns409WithRecursoNoDisponibleWhenResourceIsNotAvailable() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.MANTENIMIENTO);
        LocalDateTime inicio = futureStart();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(), inicio, inicio.plusHours(1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("RECURSO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns409WithRecursoNoDisponibleWhenReservationOverlaps() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);
        persistirReserva(usuario, recurso, inicio, fin, EstadoReserva.ACTIVA);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(),
                                inicio.plusMinutes(30), fin.plusMinutes(30))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("RECURSO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithFechasInvalidasWhenEndIsNotAfterStart() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(),
                                inicio, inicio.minusHours(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FECHAS_INVALIDAS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithFechaPasadaWhenStartIsInThePast() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(),
                                inicio, inicio.plusHours(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FECHA_PASADA"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithLimiteReservasActivasWhenUserAlreadyHasThree() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);
        persistirReserva(usuario, recurso, inicio.plusHours(2), inicio.plusHours(3), EstadoReserva.ACTIVA);
        persistirReserva(usuario, recurso, inicio.plusHours(4), inicio.plusHours(5), EstadoReserva.ACTIVA);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), recurso.getId(),
                                inicio.plusHours(6), inicio.plusHours(7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("LIMITE_RESERVAS_ACTIVAS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns404WhenCreatingReservationForNonexistentUser() throws Exception {
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(999_999_999L, recurso.getId(), inicio, inicio.plusHours(1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USUARIO_NO_ENCONTRADO"));
    }

    @Test
    void returns404WhenCreatingReservationForNonexistentResource() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        LocalDateTime inicio = futureStart();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(usuario.getId(), 999_999_999L, inicio, inicio.plusHours(1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void returns400WithValidacionWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithSolicitudInvalidaWhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void returns200WhenReservationExists() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);

        mockMvc.perform(get("/reservas/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reserva.getId()))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void returns200WithReservationsWhenListingWithFilters() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);

        mockMvc.perform(get("/reservas")
                        .param("usuarioId", String.valueOf(usuario.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[*].id", hasItem(reserva.getId().intValue())));
    }

    @Test
    void returns404WithStandardErrorBodyWhenReservationDoesNotExist() throws Exception {
        mockMvc.perform(get("/reservas/{id}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESERVA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns200WithReservationsWhenListingByUser() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);

        mockMvc.perform(get("/reservas/usuario/{usuarioId}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(reserva.getId().intValue())));
    }

    @Test
    void returns404WhenListingByNonexistentUser() throws Exception {
        mockMvc.perform(get("/reservas/usuario/{usuarioId}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USUARIO_NO_ENCONTRADO"));
    }

    @Test
    void returns200WithReservationsWhenListingByResource() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);

        mockMvc.perform(get("/reservas/recurso/{recursoId}", recurso.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(reserva.getId().intValue())));
    }

    @Test
    void returns404WhenListingByNonexistentResource() throws Exception {
        mockMvc.perform(get("/reservas/recurso/{recursoId}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void returns200WithOverlappingReservationsWhenQueryingByRange() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(2), EstadoReserva.ACTIVA);

        mockMvc.perform(get("/reservas/rango")
                        .param("fechaInicio", inicio.minusHours(1).format(ISO))
                        .param("fechaFin", inicio.plusHours(1).format(ISO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(reserva.getId().intValue())));
    }

    @Test
    void returns400WhenRangeEndIsNotAfterStart() throws Exception {
        LocalDateTime inicio = futureStart();

        mockMvc.perform(get("/reservas/rango")
                        .param("fechaInicio", inicio.format(ISO))
                        .param("fechaFin", inicio.minusHours(1).format(ISO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("FECHAS_INVALIDAS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns200WithCanceladaWhenCancellingAnActiveReservation() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);

        mockMvc.perform(delete("/reservas/{id}", reserva.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void returns400WithReservaCanceladaWhenCancellingTwice() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.CANCELADA);

        mockMvc.perform(delete("/reservas/{id}", reserva.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("RESERVA_CANCELADA"));
    }

    @Test
    void returns400WithReservaFinalizadaWhenCancellingAFinalizedReservation() throws Exception {
        UsuarioEntity usuario = persistirUsuario(EstadoUsuario.ACTIVO);
        RecursoEntity recurso = persistirRecurso(EstadoRecurso.DISPONIBLE);
        LocalDateTime inicio = futureStart();
        ReservaEntity reserva = persistirReserva(usuario, recurso, inicio, inicio.plusHours(1), EstadoReserva.FINALIZADA);

        mockMvc.perform(delete("/reservas/{id}", reserva.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("RESERVA_FINALIZADA"));
    }

    @Test
    void returns404WhenCancellingNonexistentReservation() throws Exception {
        mockMvc.perform(delete("/reservas/{id}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESERVA_NO_ENCONTRADA"));
    }

    private UsuarioEntity persistirUsuario(EstadoUsuario estado) {
        return UsuarioTestBuilder.aUsuario().withEstado(estado).persist(usuarioRepository);
    }

    private RecursoEntity persistirRecurso(EstadoRecurso estado) {
        return RecursoTestBuilder.aRecurso().withEstado(estado).persist(recursoRepository);
    }

    private ReservaEntity persistirReserva(UsuarioEntity usuario,
                                           RecursoEntity recurso,
                                           LocalDateTime inicio,
                                           LocalDateTime fin,
                                           EstadoReserva estado) {
        return ReservaTestBuilder.aReserva(usuario, recurso)
                .withFechaInicio(inicio)
                .withFechaFin(fin)
                .withEstado(estado)
                .persist(reservaRepository);
    }

    private String reservaJson(Long usuarioId,
                               Long recursoId,
                               LocalDateTime inicio,
                               LocalDateTime fin) {
        return """
                {"usuarioId":%d,"recursoId":%d,"fechaInicio":"%s","fechaFin":"%s"}
                """.formatted(usuarioId, recursoId, inicio.format(ISO), fin.format(ISO));
    }

    private LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
}
