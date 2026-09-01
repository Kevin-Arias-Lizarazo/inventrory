package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadEntregaRopa;

public interface EntregaRopaConsultaJpa extends JpaRepository<EntidadEntregaRopa, Long>,
		JpaSpecificationExecutor<EntidadEntregaRopa> {

	boolean existsByEmpleadoId(Long empleadoId);
}