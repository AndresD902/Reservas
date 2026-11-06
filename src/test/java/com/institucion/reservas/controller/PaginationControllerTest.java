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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación de HU-12 contra la API y una base PostgreSQL real.
 * Cada prueba revierte sus datos al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaginationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private UsuarioEntity activeUser;
    private RecursoEntity availableRoom;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        recursoRepository.deleteAll();
        usuarioRepository.deleteAll();

        activeUser = usuario("Ana", "1001", "ana@example.com", EstadoUsuario.ACTIVO);
        UsuarioEntity inactiveUser = usuario("Luis", "1002", "luis@example.com", EstadoUsuario.INACTIVO);
        usuarioRepository.saveAll(List.of(activeUser, inactiveUser));

        availableRoom = recurso("Sala 1", "SALA", EstadoRecurso.DISPONIBLE);
        RecursoEntity maintenanceRoom = recurso("Sala 2", "SALA", EstadoRecurso.MANTENIMIENTO);
        RecursoEntity availableEquipment = recurso("Equipo 1", "EQUIPO", EstadoRecurso.DISPONIBLE);
        recursoRepository.saveAll(List.of(availableRoom, maintenanceRoom, availableEquipment));

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        ReservaEntity activeReservation = reserva(activeUser, availableRoom, start, start.plusHours(1),
                EstadoReserva.ACTIVA);
        ReservaEntity cancelledReservation = reserva(inactiveUser, maintenanceRoom,
                start.plusHours(2), start.plusHours(3), EstadoReserva.CANCELADA);
        reservaRepository.saveAll(List.of(activeReservation, cancelledReservation));
        reservaRepository.flush();
    }

    @Test
    void usuariosAplicanElTamanoMaximoDePagina() throws Exception {
        mockMvc.perform(get("/usuarios").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void usuariosUsanElTamanoDePaginaPorDefecto() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void usuariosFiltranPorEstado() throws Exception {
        mockMvc.perform(get("/usuarios").param("estado", "ACTIVO").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$.content[0].nombre").value("Ana"));
    }

    @Test
    void recursosFiltranPorEstadoYTipo() throws Exception {
        mockMvc.perform(get("/recursos")
                        .param("estado", "DISPONIBLE")
                        .param("tipo", "SALA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Sala 1"));
    }

    @Test
    void recursosFiltranPorTipoSinDistinguirMayusculas() throws Exception {
        mockMvc.perform(get("/recursos").param("tipo", "equipo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Equipo 1"));
    }

    @Test
    void reservasFiltranPorEstadoYUsuario() throws Exception {
        mockMvc.perform(get("/reservas")
                        .param("estado", "ACTIVA")
                        .param("usuarioId", String.valueOf(activeUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$.content[0].usuarioId").value(activeUser.getId().intValue()));
    }

    @Test
    void reservasFiltranPorRecurso() throws Exception {
        mockMvc.perform(get("/reservas")
                        .param("recursoId", String.valueOf(availableRoom.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].recursoId").value(availableRoom.getId().intValue()));
    }

    @Test
    void reservasPaginanYDevuelvenElTotal() throws Exception {
        mockMvc.perform(get("/reservas").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    private UsuarioEntity usuario(String nombre, String documento, String correo, EstadoUsuario estado) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(nombre);
        usuario.setDocumento(documento);
        usuario.setCorreo(correo);
        usuario.setEstado(estado);
        return usuario;
    }

    private RecursoEntity recurso(String nombre, String tipo, EstadoRecurso estado) {
        RecursoEntity recurso = new RecursoEntity();
        recurso.setNombre(nombre);
        recurso.setTipo(tipo);
        recurso.setCapacidad(10);
        recurso.setEstado(estado);
        return recurso;
    }

    private ReservaEntity reserva(UsuarioEntity usuario, RecursoEntity recurso,
                                  LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                  EstadoReserva estado) {
        ReservaEntity reserva = new ReservaEntity();
        reserva.setUsuario(usuario);
        reserva.setRecurso(recurso);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setEstado(estado);
        return reserva;
    }
}
