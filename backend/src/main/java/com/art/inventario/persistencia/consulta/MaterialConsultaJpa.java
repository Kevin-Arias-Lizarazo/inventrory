package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadMaterial;

public interface MaterialConsultaJpa extends JpaRepository<EntidadMaterial, Long> {

	@Query("select count(m) from EntidadMaterial m where lower(m.nombre) = lower(:nombre) and m.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}