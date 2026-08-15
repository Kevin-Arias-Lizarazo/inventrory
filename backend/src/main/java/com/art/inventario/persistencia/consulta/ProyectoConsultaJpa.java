package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadProyecto;

public interface ProyectoConsultaJpa extends JpaRepository<EntidadProyecto, Long> {

	EntidadProyecto findByCodigo(String codigo);

	@Query("select count(p) from EntidadProyecto p where lower(p.nombre) = lower(:nombre) and p.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}