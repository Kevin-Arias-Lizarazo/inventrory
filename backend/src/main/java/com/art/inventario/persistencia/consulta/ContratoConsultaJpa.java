package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadContrato;

public interface ContratoConsultaJpa extends JpaRepository<EntidadContrato, Long>,
		JpaSpecificationExecutor<EntidadContrato> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByEmpleadoIdAndEstado(Long empleadoId, String estado);

	@Query("select c.empleado.id from EntidadContrato c where c.estado = :estado")
	List<Long> empleadosConEstado(@Param("estado") String estado);
}