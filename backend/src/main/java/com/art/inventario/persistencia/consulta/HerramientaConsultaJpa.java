package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadHerramienta;

public interface HerramientaConsultaJpa extends JpaRepository<EntidadHerramienta, Long>,
		JpaSpecificationExecutor<EntidadHerramienta> {

	EntidadHerramienta findByCodigo(String codigo);

	@Query("select count(h) from EntidadHerramienta h where lower(h.nombre) = lower(:nombre) and h.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}