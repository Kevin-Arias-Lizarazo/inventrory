package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Minuta;

public interface MinutaPersistencia {

	List<Minuta> listar();

	PaginaResultado<Minuta> listarPaginaRecientes(int pagina, int tamano);

	Minuta obtener(Long id);

	Minuta guardar(Minuta minuta);

	void eliminar(Long id);

	boolean tieneMinutasConProyecto(Long proyectoId);
}