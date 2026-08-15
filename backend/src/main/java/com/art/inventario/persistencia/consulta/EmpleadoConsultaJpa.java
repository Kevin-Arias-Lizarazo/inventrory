package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadEmpleado;

public interface EmpleadoConsultaJpa extends JpaRepository<EntidadEmpleado, Long> {

	EntidadEmpleado findByCodigo(String codigo);

	@Query("select count(e) from EntidadEmpleado e where lower(e.nombre) = lower(:nombre) and e.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}