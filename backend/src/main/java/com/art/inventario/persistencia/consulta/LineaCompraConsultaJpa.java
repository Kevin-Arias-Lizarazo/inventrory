package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadLineaCompra;

public interface LineaCompraConsultaJpa extends JpaRepository<EntidadLineaCompra, Long> {

	List<EntidadLineaCompra> findByCompraId(Long compraId);

	boolean existsByTipoAndProductoId(String tipo, Long productoId);
}