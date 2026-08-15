package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Empleado;

public interface EmpleadoPersistencia {

	List<Empleado> todos();

	boolean existeNombre(String nombre, Long excluirId);

	Empleado obtener(Long id);

	Empleado obtenerPorCodigo(String codigo);

	Empleado guardar(Empleado empleado);

	void eliminar(Long id);

	boolean tieneReferencias(Long id);
}