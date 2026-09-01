package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadFactura;

public interface FacturaConsultaJpa extends JpaRepository<EntidadFactura, Long>,
		JpaSpecificationExecutor<EntidadFactura> {

	boolean existsByProveedorId(Long proveedorId);

	Optional<EntidadFactura> findByCompraId(Long compraId);
}