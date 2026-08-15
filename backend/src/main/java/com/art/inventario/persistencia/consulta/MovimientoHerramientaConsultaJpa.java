package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadMovimientoHerramienta;

public interface MovimientoHerramientaConsultaJpa extends JpaRepository<EntidadMovimientoHerramienta, Long> {

	List<EntidadMovimientoHerramienta> findByHerramientaIdOrderByFechaDesc(Long herramientaId);

	void deleteByHerramientaId(Long herramientaId);
}