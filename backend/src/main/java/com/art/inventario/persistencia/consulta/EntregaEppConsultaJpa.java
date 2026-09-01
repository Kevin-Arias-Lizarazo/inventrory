package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadEntregaEpp;

public interface EntregaEppConsultaJpa extends JpaRepository<EntidadEntregaEpp, Long>,
		JpaSpecificationExecutor<EntidadEntregaEpp> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByEppId(Long eppId);
}