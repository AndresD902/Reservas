package com.institucion.reservas.repository;

import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.RecursoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecursoRepository extends JpaRepository<RecursoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecursoEntity r WHERE r.id = :id")
    Optional<RecursoEntity> findByIdForUpdate(@Param("id") Long id);

    Page<RecursoEntity> findByEstado(EstadoRecurso estado, Pageable pageable);

    Page<RecursoEntity> findByTipoIgnoreCase(String tipo, Pageable pageable);

    Page<RecursoEntity> findByEstadoAndTipoIgnoreCase(EstadoRecurso estado, String tipo, Pageable pageable);
}
