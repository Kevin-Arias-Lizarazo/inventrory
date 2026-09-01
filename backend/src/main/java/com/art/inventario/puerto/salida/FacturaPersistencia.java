package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Factura;

public interface FacturaPersistencia {

	List<Factura> listar();

	PaginaResultado<Factura> listarPagina(ConsultaPaginada consultaPaginada);

	Factura obtener(Long id);

	Factura guardar(Factura factura);

	void eliminar(Long id);

	boolean existeLineaProducto(String tipo, Long productoId);

	Double ultimoCosto(String tipo, Long productoId);

	void vincularCompra(Long facturaId, Long compraId);

	void desvincularCompra(Long compraId);
}