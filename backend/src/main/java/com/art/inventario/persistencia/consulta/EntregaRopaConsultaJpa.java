package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadEntregaRopa;

public interface EntregaRopaConsultaJpa extends JpaRepository<EntidadEntregaRopa, Long> {

	boolean existsByEmpleadoId(Long empleadoId);
}