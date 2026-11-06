package com.institucion.reservas.concurrency;

import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import com.institucion.reservas.support.DatabaseCleaner;
import com.institucion.reservas.support.RecursoTestBuilder;
import com.institucion.reservas.support.UsuarioTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Criterio de éxito "Cero Duplicados" de HU-17. Envía peticiones HTTP
 * simultáneas reales contra un servidor embebido y un PostgreSQL real, porque
 * son los bloqueos pesimistas de PostgreSQL los que se quieren verificar.
 * Corre con la tarea {@code ./gradlew postgresTest}; queda fuera de la suite
 * H2 con la etiqueta {@code postgres}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("postgres")
@Tag("postgres")
class ReservaConcurrencyTest {

    /** Número de hilos simultáneos por escenario. */
    private static final int HILOS = 10;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Pattern ERROR_JSON = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    @Test
    void onlyOneRequestIsConfirmedWhenManyRaceForTheSameResourceAndInterval() throws Exception {
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);
        RecursoEntity recurso = persistirRecurso();
        List<UsuarioEntity> usuarios = IntStream.range(0, HILOS)
                .mapToObj(i -> persistirUsuario())
                .toList();

        List<HttpResponse<String>> respuestas = ejecutarSimultaneamente(usuarios.stream()
                .map(usuario -> (Callable<HttpResponse<String>>) () ->
                        crearReserva(usuario.getId(), recurso.getId(), inicio, fin))
                .toList());

        long creadas = respuestas.stream().filter(r -> r.statusCode() == 201).count();
        long conflictos = respuestas.stream().filter(r -> r.statusCode() == 409).count();

        assertThat(respuestas).allSatisfy(r -> assertThat(r.statusCode()).isIn(201, 409));
        assertThat(creadas).isEqualTo(1);
        assertThat(conflictos).isEqualTo(HILOS - 1);
        respuestas.stream()
                .filter(r -> r.statusCode() == 409)
                .forEach(r -> assertThat(errorDe(r)).isEqualTo("RECURSO_NO_DISPONIBLE"));
    }

    @Test
    void leavesExactlyOneActiveReservationForTheResourceAndInterval() throws Exception {
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(2);
        RecursoEntity recurso = persistirRecurso();
        List<UsuarioEntity> usuarios = IntStream.range(0, HILOS)
                .mapToObj(i -> persistirUsuario())
                .toList();

        ejecutarSimultaneamente(usuarios.stream()
                .map(usuario -> (Callable<HttpResponse<String>>) () ->
                        crearReserva(usuario.getId(), recurso.getId(), inicio, fin))
                .toList());

        long activasSolapadas = reservaRepository.countOverlappingReservas(
                recurso.getId(), inicio, fin, EstadoReserva.ACTIVA);
        long activasDelRecurso = reservaRepository
                .findByRecursoIdAndEstadoOrderByFechaInicioAsc(recurso.getId(), EstadoReserva.ACTIVA)
                .size();

        assertThat(activasSolapadas).isEqualTo(1);
        assertThat(activasDelRecurso).isEqualTo(1);
    }

    @Test
    void neverExceedsThreeActiveReservationsWhenTheSameUserRacesForDifferentResources() throws Exception {
        LocalDateTime inicio = futureStart();
        LocalDateTime fin = inicio.plusHours(1);
        UsuarioEntity usuario = persistirUsuario();
        List<RecursoEntity> recursos = IntStream.range(0, 6)
                .mapToObj(i -> persistirRecurso())
                .toList();

        List<HttpResponse<String>> respuestas = ejecutarSimultaneamente(recursos.stream()
                .map(recurso -> (Callable<HttpResponse<String>>) () ->
                        crearReserva(usuario.getId(), recurso.getId(), inicio, fin))
                .toList());

        long creadas = respuestas.stream().filter(r -> r.statusCode() == 201).count();
        long rechazadas = respuestas.stream().filter(r -> r.statusCode() == 400).count();

        assertThat(creadas).isEqualTo(3);
        assertThat(rechazadas).isEqualTo(3);
        respuestas.stream()
                .filter(r -> r.statusCode() == 400)
                .forEach(r -> assertThat(errorDe(r)).isEqualTo("LIMITE_RESERVAS_ACTIVAS"));

        long activas = reservaRepository.countByUsuarioIdAndEstado(usuario.getId(), EstadoReserva.ACTIVA);
        assertThat(activas).isEqualTo(3);
    }

    private List<HttpResponse<String>> ejecutarSimultaneamente(List<Callable<HttpResponse<String>>> tareas)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tareas.size());
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<HttpResponse<String>>> futuros = new ArrayList<>();
            for (Callable<HttpResponse<String>> tarea : tareas) {
                futuros.add(pool.submit(() -> {
                    startGate.await();
                    return tarea.call();
                }));
            }
            startGate.countDown();

            List<HttpResponse<String>> respuestas = new ArrayList<>();
            for (Future<HttpResponse<String>> futuro : futuros) {
                respuestas.add(futuro.get(60, TimeUnit.SECONDS));
            }
            return respuestas;
        } finally {
            pool.shutdownNow();
        }
    }

    private HttpResponse<String> crearReserva(Long usuarioId, Long recursoId,
                                              LocalDateTime inicio, LocalDateTime fin) throws Exception {
        String cuerpo = """
                {"usuarioId":%d,"recursoId":%d,"fechaInicio":"%s","fechaFin":"%s"}
                """.formatted(usuarioId, recursoId, inicio.format(ISO), fin.format(ISO));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/reservas"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String errorDe(HttpResponse<String> respuesta) {
        Matcher matcher = ERROR_JSON.matcher(respuesta.body());
        if (!matcher.find()) {
            throw new IllegalStateException("Respuesta sin JSON de error: " + respuesta.body());
        }
        return matcher.group(1);
    }

    private UsuarioEntity persistirUsuario() {
        return UsuarioTestBuilder.aUsuario().persist(usuarioRepository);
    }

    private RecursoEntity persistirRecurso() {
        return RecursoTestBuilder.aRecurso().withEstado(EstadoRecurso.DISPONIBLE).persist(recursoRepository);
    }

    private LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
}
