package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadEntregaEpp;

public interface EntregaEppConsultaJpa extends JpaRepository<EntidadEntregaEpp, Long> {

	boolean existsByEmpleadoId(Long empleadoId);

	boolean existsByEppId(Long eppId);
}