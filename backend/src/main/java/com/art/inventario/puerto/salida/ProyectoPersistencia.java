package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proyecto;

public interface ProyectoPersistencia {

	List<Proyecto> listar();

	PaginaResultado<Proyecto> listarPagina(ConsultaPaginada consultaPaginada);

	boolean existeNombre(String nombre, Long excluirId);

	Proyecto obtener(Long id);

	Proyecto obtenerPorCodigo(String codigo);

	boolean existePorCodigo(String codigo);

	Proyecto guardar(Proyecto proyecto);

	void eliminar(Long id);
}