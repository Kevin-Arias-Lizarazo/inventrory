package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Empleado;

public interface EmpleadoPersistencia {

	List<Empleado> todos();

	PaginaResultado<Empleado> listarPagina(ConsultaPaginada consultaPaginada);

	boolean existeNombre(String nombre, Long excluirId);

	Empleado obtener(Long id);

	Empleado obtenerPorCodigo(String codigo);

	boolean existePorCodigo(String codigo);

	Empleado guardar(Empleado empleado);

	void eliminar(Long id);

	boolean tieneReferencias(Long id);
}