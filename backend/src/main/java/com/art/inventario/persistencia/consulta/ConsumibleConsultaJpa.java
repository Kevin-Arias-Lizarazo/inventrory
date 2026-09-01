package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadConsumible;

public interface ConsumibleConsultaJpa extends JpaRepository<EntidadConsumible, Long>,
		JpaSpecificationExecutor<EntidadConsumible> {

	EntidadConsumible findByCodigo(String codigo);

	@Query("select count(c) from EntidadConsumible c where lower(c.nombre) = lower(:nombre) and c.id <> :excluir")
	long contarPorNombre(@Param("nombre") String nombre, @Param("excluir") Long excluir);
}