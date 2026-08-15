package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadDevolucion;

public interface DevolucionConsultaJpa extends JpaRepository<EntidadDevolucion, Long> {

	List<EntidadDevolucion> findByCompraId(Long compraId);

	boolean existsByCompraId(Long compraId);
}
