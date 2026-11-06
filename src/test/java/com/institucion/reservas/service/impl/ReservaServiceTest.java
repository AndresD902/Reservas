package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.ReservaRequest;
import com.institucion.reservas.dto.ReservaResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ReservationConflictException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.ReservaMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    private final ReservaMapper reservaMapper = new ReservaMapper();

    private ReservaServiceImpl service;

    private UsuarioEntity usuario;
    private RecursoEntity recurso;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void setUp() {
        service = new ReservaServiceImpl(reservaRepository, usuarioRepository, recursoRepository, reservaMapper);

        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNombre("Ana");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        recurso = new RecursoEntity();
        recurso.setId(2L);
        recurso.setNombre("Sala 1");
        recurso.setEstado(EstadoRecurso.DISPONIBLE);

        inicio = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        fin = inicio.plusHours(2);
    }

    private void mockCrearHappyPath() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(0L);
        when(reservaRepository.countOverlappingReservas(2L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(0L);
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });
    }

    private ReservaRequest request() {
        return new ReservaRequest(1L, 2L, inicio, fin);
    }

    @Test
    void creaReservaConEstadoActivaCuandoTodoEsValido() {
        mockCrearHappyPath();

        ReservaResponse response = service.crear(request());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.usuarioId()).isEqualTo(1L);
        assertThat(response.recursoId()).isEqualTo(2L);
        assertThat(response.fechaInicio()).isEqualTo(inicio);
        assertThat(response.fechaFin()).isEqualTo(fin);
        assertThat(response.estado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void verificaArgumentosExactosDeSolapamiento() {
        mockCrearHappyPath();

        service.crear(request());

        ArgumentCaptor<Long> recursoId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDateTime> fechaInicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fechaFin = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<EstadoReserva> estado = ArgumentCaptor.forClass(EstadoReserva.class);
        verify(reservaRepository).countOverlappingReservas(
                recursoId.capture(), fechaInicio.capture(), fechaFin.capture(), estado.capture());
        assertThat(recursoId.getValue()).isEqualTo(2L);
        assertThat(fechaInicio.getValue()).isEqualTo(inicio);
        assertThat(fechaFin.getValue()).isEqualTo(fin);
        assertThat(estado.getValue()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void rechazaUsuarioInactivo() {
        usuario.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("USUARIO_INACTIVO");
        verifyNoInteractions(recursoRepository, reservaRepository);
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarioRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ReservaRequest request = new ReservaRequest(99L, 2L, inicio, fin);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("USUARIO_NO_ENCONTRADO");
        verifyNoInteractions(recursoRepository, reservaRepository);
    }

    @Test
    void rechazaRecursoEnMantenimiento() {
        recurso.setEstado(EstadoRecurso.MANTENIMIENTO);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ReservationConflictException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_DISPONIBLE");
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void rechazaRecursoFueraDeServicio() {
        recurso.setEstado(EstadoRecurso.FUERA_DE_SERVICIO);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ReservationConflictException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_DISPONIBLE");
    }

    @Test
    void rechazaRecursoInexistente() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ReservaRequest request = new ReservaRequest(1L, 99L, inicio, fin);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }

    @Test
    void rechazaFinIgualAlInicio() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        ReservaRequest request = new ReservaRequest(1L, 2L, inicio, inicio);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");
    }

    @Test
    void rechazaFinAnteriorAlInicio() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        ReservaRequest request = new ReservaRequest(1L, 2L, inicio, inicio.minusHours(1));

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");
    }

    @Test
    void aceptaFinUnNanosegundoDespuesDelInicio() {
        LocalDateTime inicioCorto = inicio;
        LocalDateTime finCorto = inicioCorto.plusNanos(1);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(0L);
        when(reservaRepository.countOverlappingReservas(2L, inicioCorto, finCorto, EstadoReserva.ACTIVA))
                .thenReturn(0L);
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        ReservaResponse response = service.crear(new ReservaRequest(1L, 2L, inicioCorto, finCorto));

        assertThat(response.id()).isEqualTo(11L);
    }

    @Test
    void rechazaInicioPasado() {
        LocalDateTime inicioPasado = LocalDateTime.now().minusHours(1);
        LocalDateTime finFuturo = LocalDateTime.now().plusHours(1);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        ReservaRequest request = new ReservaRequest(1L, 2L, inicioPasado, finFuturo);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHA_PASADA");
    }

    @Test
    void rechazaFinPasado() {
        LocalDateTime inicioPasado = LocalDateTime.now().minusHours(2);
        LocalDateTime finPasado = LocalDateTime.now().minusHours(1);
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));

        ReservaRequest request = new ReservaRequest(1L, 2L, inicioPasado, finPasado);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHA_PASADA");
    }

    @Test
    void permiteReservaConDosActivas() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(2L);
        when(reservaRepository.countOverlappingReservas(2L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(0L);
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity entity = invocation.getArgument(0);
            entity.setId(12L);
            return entity;
        });

        assertThat(service.crear(request()).id()).isEqualTo(12L);
    }

    @Test
    void rechazaCuartaReservaActiva() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(3L);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("LIMITE_RESERVAS_ACTIVAS");
        verify(reservaRepository, never()).countOverlappingReservas(any(), any(), any(), any());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void rechazaReservaSolapada() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));
        when(recursoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recurso));
        when(reservaRepository.countByUsuarioIdAndEstado(1L, EstadoReserva.ACTIVA)).thenReturn(0L);
        when(reservaRepository.countOverlappingReservas(2L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ReservationConflictException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_DISPONIBLE");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelaReservaActiva() {
        ReservaEntity entity = new ReservaEntity();
        entity.setId(10L);
        entity.setUsuario(usuario);
        entity.setRecurso(recurso);
        entity.setFechaInicio(inicio);
        entity.setFechaFin(fin);
        entity.setEstado(EstadoReserva.ACTIVA);
        when(reservaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(entity));
        when(reservaRepository.save(entity)).thenReturn(entity);

        ReservaResponse response = service.cancelar(10L);

        assertThat(response.estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(entity.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void rechazaCancelarReservaFinalizada() {
        ReservaEntity entity = new ReservaEntity();
        entity.setEstado(EstadoReserva.FINALIZADA);
        when(reservaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.cancelar(10L))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("RESERVA_FINALIZADA");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void rechazaCancelarReservaCancelada() {
        ReservaEntity entity = new ReservaEntity();
        entity.setEstado(EstadoReserva.CANCELADA);
        when(reservaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.cancelar(10L))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("RESERVA_CANCELADA");
    }

    @Test
    void rechazaCancelarReservaInexistente() {
        when(reservaRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RESERVA_NO_ENCONTRADA");
    }

    @Test
    void rechazaRequestNulo() {
        assertThatThrownBy(() -> service.crear(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorios");
        verifyNoInteractions(usuarioRepository, recursoRepository, reservaRepository);
    }

    @Test
    void rechazaUsuarioIdNulo() {
        assertThatThrownBy(() -> service.crear(new ReservaRequest(null, 2L, inicio, fin)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaRecursoIdNulo() {
        assertThatThrownBy(() -> service.crear(new ReservaRequest(1L, null, inicio, fin)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaFechaInicioNula() {
        assertThatThrownBy(() -> service.crear(new ReservaRequest(1L, 2L, null, fin)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaFechaFinNula() {
        assertThatThrownBy(() -> service.crear(new ReservaRequest(1L, 2L, inicio, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void obtieneReservaPorId() {
        ReservaEntity entity = new ReservaEntity();
        entity.setId(10L);
        entity.setUsuario(usuario);
        entity.setRecurso(recurso);
        entity.setFechaInicio(inicio);
        entity.setFechaFin(fin);
        entity.setEstado(EstadoReserva.ACTIVA);
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThat(service.obtenerPorId(10L).id()).isEqualTo(10L);
    }

    @Test
    void rechazaObtenerReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RESERVA_NO_ENCONTRADA");
    }

    @Test
    void rechazaListarPorRangoConInicioNulo() {
        assertThatThrownBy(() -> service.listarPorRango(null, fin))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");
    }

    @Test
    void rechazaListarPorRangoConFinNulo() {
        assertThatThrownBy(() -> service.listarPorRango(inicio, null))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("error")
                .isEqualTo("FECHAS_INVALIDAS");
    }

    @Test
    void listaReservasPorUsuarioExistente() {
        ReservaEntity entity = new ReservaEntity();
        entity.setId(10L);
        entity.setUsuario(usuario);
        entity.setRecurso(recurso);
        entity.setFechaInicio(inicio);
        entity.setFechaFin(fin);
        entity.setEstado(EstadoReserva.ACTIVA);
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.findByUsuarioIdOrderByFechaInicioAsc(1L)).thenReturn(List.of(entity));

        assertThat(service.listarPorUsuario(1L)).hasSize(1);
    }

    @Test
    void rechazaListarPorUsuarioInexistente() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.listarPorUsuario(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("USUARIO_NO_ENCONTRADO");
    }

    @Test
    void listaReservasPorRecursoExistente() {
        ReservaEntity entity = new ReservaEntity();
        entity.setId(10L);
        entity.setUsuario(usuario);
        entity.setRecurso(recurso);
        entity.setFechaInicio(inicio);
        entity.setFechaFin(fin);
        entity.setEstado(EstadoReserva.ACTIVA);
        when(recursoRepository.existsById(2L)).thenReturn(true);
        when(reservaRepository.findByRecursoIdOrderByFechaInicioAsc(2L)).thenReturn(List.of(entity));

        assertThat(service.listarPorRecurso(2L)).hasSize(1);
    }

    @Test
    void rechazaListarPorRecursoInexistente() {
        when(recursoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.listarPorRecurso(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }
}
