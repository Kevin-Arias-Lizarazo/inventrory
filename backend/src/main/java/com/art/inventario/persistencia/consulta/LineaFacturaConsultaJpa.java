package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadLineaFactura;

public interface LineaFacturaConsultaJpa extends JpaRepository<EntidadLineaFactura, Long> {

	List<EntidadLineaFactura> findByFacturaId(Long facturaId);

	boolean existsByTipoAndProductoId(String tipo, Long productoId);

	@Query("select l.costoUnitario from EntidadLineaFactura l where l.tipo = :tipo and l.productoId = :productoId "
			+ "and l.costoUnitario is not null order by l.factura.fecha desc, l.factura.id desc, l.id desc")
	List<Double> costosMasRecientes(@Param("tipo") String tipo, @Param("productoId") Long productoId,
			org.springframework.data.domain.Pageable pageable);
}