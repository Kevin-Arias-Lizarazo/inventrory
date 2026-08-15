package com.art.inventario.puerto.salida;

public interface ProductoCostoPersistencia {

	void actualizarUltimoCosto(String tipo, Long productoId, Double costo);
}