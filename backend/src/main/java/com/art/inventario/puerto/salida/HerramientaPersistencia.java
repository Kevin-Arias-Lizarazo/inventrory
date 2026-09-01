package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Herramienta;

public interface HerramientaPersistencia {

	List<Herramienta> listar();

	PaginaResultado<Herramienta> listarPagina(int pagina, int tamano);

	PaginaResultado<Herramienta> listarPagina(ConsultaPaginada consulta);

	boolean existeNombre(String nombre, Long excluirId);

	Herramienta obtener(Long id);

	Herramienta obtenerPorCodigo(String codigo);

	boolean existePorCodigo(String codigo);

	Herramienta guardar(Herramienta herramienta);

	void eliminar(Long id);
}