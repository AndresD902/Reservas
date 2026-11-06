package com.institucion.reservas.repository;

import com.institucion.reservas.entity.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Long> {

    Optional<AdministradorEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
