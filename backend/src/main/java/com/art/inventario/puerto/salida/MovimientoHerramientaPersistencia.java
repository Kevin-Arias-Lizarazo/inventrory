package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.MovimientoHerramienta;

public interface MovimientoHerramientaPersistencia {

	List<MovimientoHerramienta> listarPorHerramienta(Long herramientaId);

	List<MovimientoHerramienta> listarTodos();

	PaginaResultado<MovimientoHerramienta> listarTodosPagina(ConsultaPaginada consulta);

	PaginaResultado<MovimientoHerramienta> listarPorHerramientaPagina(Long herramientaId, ConsultaPaginada consulta);

	MovimientoHerramienta obtener(Long id);

	MovimientoHerramienta guardar(MovimientoHerramienta movimiento);

	void eliminar(Long id);

	void eliminarPorHerramienta(Long herramientaId);
}