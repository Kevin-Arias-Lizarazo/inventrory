package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Devolucion;

public interface DevolucionPersistencia {

	List<Devolucion> listar();

	List<Devolucion> listarPorCompra(Long compraId);

	PaginaResultado<Devolucion> listarPagina(ConsultaPaginada consulta);

	Devolucion obtener(Long id);

	Devolucion guardar(Devolucion devolucion);

	void eliminar(Long id);

	boolean tienePorCompra(Long compraId);

	boolean tieneProducto(String tipo, Long productoId);

	int cantidadDevuelta(Long compraId, String tipo, Long productoId);
}
