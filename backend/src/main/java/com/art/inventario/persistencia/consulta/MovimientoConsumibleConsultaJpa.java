package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadMovimientoConsumible;

public interface MovimientoConsumibleConsultaJpa extends JpaRepository<EntidadMovimientoConsumible, Long> {

	List<EntidadMovimientoConsumible> findByConsumibleIdOrderByFechaDesc(Long consumibleId);

	boolean existsByConsumibleId(Long consumibleId);
}