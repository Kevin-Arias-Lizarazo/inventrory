package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadLineaDevolucion;

public interface LineaDevolucionConsultaJpa extends JpaRepository<EntidadLineaDevolucion, Long> {

	List<EntidadLineaDevolucion> findByDevolucionId(Long devolucionId);

	boolean existsByTipoAndProductoId(String tipo, Long productoId);

	@Query("select coalesce(sum(l.cantidad), 0) from EntidadLineaDevolucion l where l.devolucion.compraId = :compraId and l.tipo = :tipo and l.productoId = :productoId")
	Integer sumCantidadByCompraAndTipoAndProducto(@Param("compraId") Long compraId, @Param("tipo") String tipo,
			@Param("productoId") Long productoId);
}
