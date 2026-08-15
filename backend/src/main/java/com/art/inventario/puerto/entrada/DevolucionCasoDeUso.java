package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.dominio.Devolucion;

public interface DevolucionCasoDeUso {

	List<Devolucion> listar();

	List<Devolucion> listarPorCompra(Long compraId);

	Devolucion obtener(Long id);

	Devolucion crear(Long compraId, Devolucion devolucion);

	void eliminar(Long id);
}
