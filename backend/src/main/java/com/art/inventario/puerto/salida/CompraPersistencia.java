package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Compra;

public interface CompraPersistencia {

	List<Compra> listar();

	Compra obtener(Long id);

	Compra guardar(Compra compra);

	void eliminar(Long id);

	boolean existeLineaProducto(String tipo, Long productoId);

	void vincularFactura(Long compraId, Long facturaId);

	void desvincularFactura(Long compraId);
}