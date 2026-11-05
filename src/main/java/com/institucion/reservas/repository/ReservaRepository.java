package com.institucion.reservas.repository;

import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.ReservaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReservaEntity r WHERE r.id = :id")
    Optional<ReservaEntity> findByIdForUpdate(@Param("id") Long id);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoReserva estado);

    List<ReservaEntity> findByUsuarioIdOrderByFechaInicioAsc(Long usuarioId);

    List<ReservaEntity> findByRecursoIdOrderByFechaInicioAsc(Long recursoId);

    List<ReservaEntity> findByRecursoIdAndEstadoOrderByFechaInicioAsc(Long recursoId, EstadoReserva estado);

    @Query("""
            SELECT COUNT(r) FROM ReservaEntity r
            WHERE r.recurso.id = :recursoId
              AND r.estado = :estado
              AND r.fechaInicio < :fechaFin
              AND r.fechaFin > :fechaInicio
            """)
    long countOverlappingReservas(@Param("recursoId") Long recursoId,
                                  @Param("fechaInicio") LocalDateTime fechaInicio,
                                  @Param("fechaFin") LocalDateTime fechaFin,
                                  @Param("estado") EstadoReserva estado);

    @Query("""
            SELECT r FROM ReservaEntity r
            WHERE r.recurso.id = :recursoId
              AND r.estado = :estado
              AND r.fechaInicio < :fechaFin
              AND r.fechaFin > :fechaInicio
            ORDER BY r.fechaInicio ASC
            """)
    List<ReservaEntity> findOverlappingReservas(@Param("recursoId") Long recursoId,
                                                @Param("fechaInicio") LocalDateTime fechaInicio,
                                                @Param("fechaFin") LocalDateTime fechaFin,
                                                @Param("estado") EstadoReserva estado);
}
