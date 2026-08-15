package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadMinuta;

public interface MinutaConsultaJpa extends JpaRepository<EntidadMinuta, Long> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByProyectoId(Long proyectoId);
}