package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Factura;

public interface FacturaPersistencia {

	List<Factura> listar();

	Factura obtener(Long id);

	Factura guardar(Factura factura);

	void eliminar(Long id);

	boolean existeLineaProducto(String tipo, Long productoId);

	Double ultimoCosto(String tipo, Long productoId);

	void vincularCompra(Long facturaId, Long compraId);

	void desvincularCompra(Long compraId);
}