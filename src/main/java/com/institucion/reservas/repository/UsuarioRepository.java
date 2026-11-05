package com.institucion.reservas.repository;

import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UsuarioEntity u WHERE u.id = :id")
    Optional<UsuarioEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<UsuarioEntity> findByDocumento(String documento);

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByDocumento(String documento);

    boolean existsByCorreo(String correo);

    List<UsuarioEntity> findByEstado(EstadoUsuario estado);
}
