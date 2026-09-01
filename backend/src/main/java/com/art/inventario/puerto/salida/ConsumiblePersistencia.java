package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.MovimientoConsumible;

public interface ConsumiblePersistencia {

	List<Consumible> listar();

	PaginaResultado<Consumible> listarPagina(int pagina, int tamano);

	PaginaResultado<Consumible> listarPagina(ConsultaPaginada consulta);

	boolean existeNombre(String nombre, Long excluirId);

	Consumible obtener(Long id);

	Consumible obtenerPorCodigo(String codigo);

	boolean existePorCodigo(String codigo);

	Consumible guardar(Consumible consumible);

	void eliminar(Long id);

	boolean tieneMovimientos(Long id);

	List<MovimientoConsumible> listarMovimientos(Long consumibleId);

	List<MovimientoConsumible> listarTodosMovimientos();

	MovimientoConsumible obtenerMovimiento(Long id);

	MovimientoConsumible guardarMovimiento(MovimientoConsumible movimiento);

	void eliminarMovimiento(MovimientoConsumible movimiento);
}