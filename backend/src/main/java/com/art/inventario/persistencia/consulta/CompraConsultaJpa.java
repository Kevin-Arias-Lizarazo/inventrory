package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadCompra;

public interface CompraConsultaJpa extends JpaRepository<EntidadCompra, Long> {

	boolean existsByProveedorId(Long proveedorId);
}