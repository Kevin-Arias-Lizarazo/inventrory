package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadAsignacionHerramienta;

public interface AsignacionHerramientaConsultaJpa extends JpaRepository<EntidadAsignacionHerramienta, Long>,
		JpaSpecificationExecutor<EntidadAsignacionHerramienta> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByHerramientaIdAndDevueltaFalse(Long herramientaId);

	@Query("select coalesce(sum(a.cantidad), 0) from EntidadAsignacionHerramienta a "
			+ "where a.herramienta.id = :herramientaId and a.devuelta = false and a.id <> :excluirId")
	long contarAsignacionesActivas(@Param("herramientaId") Long herramientaId, @Param("excluirId") Long excluirId);

	@Query("select a from EntidadAsignacionHerramienta a "
			+ "where a.empleado.id = :empleadoId and a.herramienta.id = :herramientaId and a.devuelta = false "
			+ "order by a.id asc")
	List<EntidadAsignacionHerramienta> activasParaDevolucion(@Param("empleadoId") Long empleadoId,
			@Param("herramientaId") Long herramientaId, Pageable pageable);

	@Query("select distinct a.herramienta.id from EntidadAsignacionHerramienta a "
			+ "where a.herramienta is not null and a.devuelta = false")
	List<Long> herramientasEnUso();

	@Query("select a.herramienta.id, coalesce(sum(a.cantidad), 0) from EntidadAsignacionHerramienta a "
			+ "where a.herramienta is not null and a.devuelta = false group by a.herramienta.id")
	List<Object[]> contarAsignacionesActivasPorHerramienta();

	@Modifying
	@Query("update EntidadAsignacionHerramienta a set a.herramienta = null "
			+ "where a.herramienta.id = :herramientaId")
	void desvincularHerramienta(@Param("herramientaId") Long herramientaId);
}