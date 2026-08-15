package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Ajuste;

public interface AjustePersistencia {

	List<Ajuste> listar();

	Ajuste obtener(Long id);

	Ajuste guardar(Ajuste ajuste);

	void eliminar(Long id);

	boolean tieneProducto(String tipoProducto, Long productoId);
}
