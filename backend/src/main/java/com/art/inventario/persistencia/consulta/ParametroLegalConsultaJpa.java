package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadParametroLegal;

public interface ParametroLegalConsultaJpa extends JpaRepository<EntidadParametroLegal, Long> {

	Optional<EntidadParametroLegal> findByAnio(Integer anio);

	boolean existsByAnio(Integer anio);
}
