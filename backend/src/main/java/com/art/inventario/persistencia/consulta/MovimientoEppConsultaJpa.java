package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadMovimientoEpp;

public interface MovimientoEppConsultaJpa extends JpaRepository<EntidadMovimientoEpp, Long> {

	List<EntidadMovimientoEpp> findByEppIdOrderByFechaDesc(Long eppId);

	boolean existsByEppId(Long eppId);
}