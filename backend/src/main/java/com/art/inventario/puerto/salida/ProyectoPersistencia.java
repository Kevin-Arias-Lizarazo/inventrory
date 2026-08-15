package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Proyecto;

public interface ProyectoPersistencia {

	List<Proyecto> listar();

	boolean existeNombre(String nombre, Long excluirId);

	Proyecto obtener(Long id);

	Proyecto obtenerPorCodigo(String codigo);

	Proyecto guardar(Proyecto proyecto);

	void eliminar(Long id);
}