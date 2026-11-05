package com.institucion.reservas.repository;

import com.institucion.reservas.entity.EstadoRecurso;
import com.institucion.reservas.entity.RecursoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecursoRepository extends JpaRepository<RecursoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecursoEntity r WHERE r.id = :id")
    Optional<RecursoEntity> findByIdForUpdate(@Param("id") Long id);

    List<RecursoEntity> findByEstado(EstadoRecurso estado);

    List<RecursoEntity> findByTipoIgnoreCase(String tipo);
}
