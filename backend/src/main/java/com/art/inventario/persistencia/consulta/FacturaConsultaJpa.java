package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadFactura;

public interface FacturaConsultaJpa extends JpaRepository<EntidadFactura, Long> {

	boolean existsByProveedorId(Long proveedorId);

	Optional<EntidadFactura> findByCompraId(Long compraId);
}