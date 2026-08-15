package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadLineaAjuste;

public interface LineaAjusteConsultaJpa extends JpaRepository<EntidadLineaAjuste, Long> {

	boolean existsByTipoProductoAndProductoId(String tipoProducto, Long productoId);
}
