package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionConsumible;

public interface AsignacionConsumiblePersistencia {

	List<AsignacionConsumible> listar();

	PaginaResultado<AsignacionConsumible> listarPagina(int pagina, int tamano);

	AsignacionConsumible obtener(Long id);

	AsignacionConsumible guardar(AsignacionConsumible asignacion);

	void eliminar(Long id);
}