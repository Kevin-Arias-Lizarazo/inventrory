package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadMinuta;

public interface MinutaConsultaJpa extends JpaRepository<EntidadMinuta, Long>,
		JpaSpecificationExecutor<EntidadMinuta> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByProyectoId(Long proyectoId);
}