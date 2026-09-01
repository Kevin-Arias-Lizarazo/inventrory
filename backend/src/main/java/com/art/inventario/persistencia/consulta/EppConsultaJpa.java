package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadEpp;

public interface EppConsultaJpa extends JpaRepository<EntidadEpp, Long>,
		JpaSpecificationExecutor<EntidadEpp> {

	@Query("select count(e) from EntidadEpp e where lower(e.nombre) = lower(:nombre) and e.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}