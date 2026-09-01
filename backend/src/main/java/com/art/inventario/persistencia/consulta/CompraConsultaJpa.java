package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadCompra;

public interface CompraConsultaJpa extends JpaRepository<EntidadCompra, Long>,
		JpaSpecificationExecutor<EntidadCompra> {

	boolean existsByProveedorId(Long proveedorId);
}