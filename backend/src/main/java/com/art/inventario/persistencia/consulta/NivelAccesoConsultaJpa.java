package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadNivelAcceso;

public interface NivelAccesoConsultaJpa extends JpaRepository<EntidadNivelAcceso, Long> {

	Optional<EntidadNivelAcceso> findByCodigo(String codigo);

	Optional<EntidadNivelAcceso> findByUsuarioRaizIdIsNotNull();
}