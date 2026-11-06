package com.institucion.reservas.service.impl;

import com.institucion.reservas.dto.DisponibilidadResponse;
import com.institucion.reservas.dto.RecursoRequest;
import com.institucion.reservas.dto.RecursoResponse;
import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.RecursoEntity;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.exception.BusinessRuleException;
import com.institucion.reservas.exception.ResourceNotFoundException;
import com.institucion.reservas.mapper.RecursoMapper;
import com.institucion.reservas.mapper.ReservaMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private final RecursoMapper recursoMapper = new RecursoMapper();
    private final ReservaMapper reservaMapper = new ReservaMapper();

    private RecursoServiceImpl service;

    private RecursoEntity entity;
    private RecursoRequest request;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void setUp() {
        service = new RecursoServiceImpl(recursoRepository, reservaRepository, recursoMapper, reservaMapper);

        entity = new RecursoEntity();
        entity.setId(1L);
        entity.setNombre("Sala 1");
        entity.setDescripcion("Piso 2");
        entity.setTipo("Sala");
        entity.setCapacidad(10);
        entity.setEstado(EstadoRecurso.DISPONIBLE);

        request = new RecursoRequest("Sala 1", "Piso 2", "Sala", 10, EstadoRecurso.DISPONIBLE);

        inicio = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        fin = inicio.plusHours(2);
    }

    @Test
    void creaRecurso() {
        when(recursoRepository.save(any(RecursoEntity.class))).thenAnswer(invocation -> {
            RecursoEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RecursoResponse response = service.crear(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Sala 1");
        assertThat(response.capacidad()).isEqualTo(10);
        assertThat(response.estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void obtieneRecursoPorId() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(service.obtenerPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void rechazaObtenerRecursoInexistente() {
        when(recursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }

    @Test
    void listaRecursos() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(recursoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        assertThat(service.listar(null, null, pageable).getContent()).hasSize(1);
    }

    @Test
    void actualizaRecurso() {
        RecursoRequest update = new RecursoRequest("Sala 2", "Planta baja", "Auditorio", 20, EstadoRecurso.MANTENIMIENTO);
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(recursoRepository.save(entity)).thenReturn(entity);

        RecursoResponse response = service.actualizar(1L, update);

        assertThat(response.nombre()).isEqualTo("Sala 2");
        assertThat(response.descripcion()).isEqualTo("Planta baja");
        assertThat(response.tipo()).isEqualTo("Auditorio");
        assertThat(response.capacidad()).isEqualTo(20);
        assertThat(response.estado()).isEqualTo(EstadoRecurso.MANTENIMIENTO);
    }

    @Test
    void rechazaActualizarRecursoInexistente() {
        when(recursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }

    @Test
    void actualizaEstadoAMantenimiento() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(recursoRepository.save(entity)).thenReturn(entity);

        RecursoResponse response = service.actualizarEstado(1L, EstadoRecurso.MANTENIMIENTO);

        assertThat(response.estado()).isEqualTo(EstadoRecurso.MANTENIMIENTO);
        assertThat(entity.getEstado()).isEqualTo(EstadoRecurso.MANTENIMIENTO);
    }

    @Test
    void rechazaActualizacionDeEstadoNulo() {
        assertThatThrownBy(() -> service.actualizarEstado(1L, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorio");
        verify(recursoRepository, never()).findById(any());
    }

    @Test
    void rechazaActualizacionDeRecursoInexistente() {
        when(recursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarEstado(99L, EstadoRecurso.MANTENIMIENTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }

    @Test
    void disponibleCuandoEstaDisponibleYSinSolapamientos() {
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reservaRepository.findOverlappingReservas(1L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(List.of());

        DisponibilidadResponse response = service.consultarDisponibilidad(1L, inicio, fin);

        assertThat(response.disponible()).isTrue();
        assertThat(response.reservasSolapadas()).isEmpty();
        assertThat(response.recursoId()).isEqualTo(1L);
        assertThat(response.fechaInicio()).isEqualTo(inicio);
        assertThat(response.fechaFin()).isEqualTo(fin);
    }

    @Test
    void noDisponibleCuandoEstaDisponiblePeroHaySolapamientos() {
        ReservaEntity overlapping = new ReservaEntity();
        overlapping.setId(5L);
        overlapping.setUsuario(new UsuarioEntity());
        overlapping.setRecurso(entity);
        overlapping.setFechaInicio(inicio);
        overlapping.setFechaFin(fin);
        overlapping.setEstado(EstadoReserva.ACTIVA);
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reservaRepository.findOverlappingReservas(1L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(List.of(overlapping));

        DisponibilidadResponse response = service.consultarDisponibilidad(1L, inicio, fin);

        assertThat(response.disponible()).isFalse();
        assertThat(response.reservasSolapadas()).hasSize(1);
    }

    @Test
    void noDisponibleCuandoEstaEnMantenimientoYSinSolapamientos() {
        entity.setEstado(EstadoRecurso.MANTENIMIENTO);
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reservaRepository.findOverlappingReservas(1L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(List.of());

        DisponibilidadResponse response = service.consultarDisponibilidad(1L, inicio, fin);

        assertThat(response.disponible()).isFalse();
        assertThat(response.reservasSolapadas()).isEmpty();
    }

    @Test
    void noDisponibleCuandoEstaFueraDeServicioYHaySolapamientos() {
        entity.setEstado(EstadoRecurso.FUERA_DE_SERVICIO);
        ReservaEntity overlapping = new ReservaEntity();
        overlapping.setId(5L);
        overlapping.setUsuario(new UsuarioEntity());
        overlapping.setRecurso(entity);
        overlapping.setFechaInicio(inicio);
        overlapping.setFechaFin(fin);
        overlapping.setEstado(EstadoReserva.ACTIVA);
        when(recursoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reservaRepository.findOverlappingReservas(1L, inicio, fin, EstadoReserva.ACTIVA))
                .thenReturn(List.of(overlapping));

        assertThat(service.consultarDisponibilidad(1L, inicio, fin).disponible()).isFalse();
    }

    @Test
    void rechazaDisponibilidadConInicioNulo() {
        assertThatThrownBy(() -> service.consultarDisponibilidad(1L, null, fin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorias");
    }

    @Test
    void rechazaDisponibilidadConFinNulo() {
        assertThatThrownBy(() -> service.consultarDisponibilidad(1L, inicio, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatorias");
    }

    @Test
    void rechazaDisponibilidadConFinNoPosterior() {
        assertThatThrownBy(() -> service.consultarDisponibilidad(1L, inicio, inicio))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void rechazaDisponibilidadDeRecursoInexistente() {
        when(recursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarDisponibilidad(99L, inicio, fin))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("error")
                .isEqualTo("RECURSO_NO_ENCONTRADO");
    }
}
