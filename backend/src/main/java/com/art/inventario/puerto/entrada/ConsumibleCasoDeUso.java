package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.MovimientoConsumible;

public interface ConsumibleCasoDeUso {

	List<Consumible> listar();

	PaginaResultado<Consumible> listarPagina(int pagina, int tamano);

	PaginaResultado<Consumible> listarPagina(ConsultaPaginada consulta);

	Consumible obtener(Long id);

	Consumible crear(Consumible consumible);

	Consumible crearConCodigo(Consumible consumible);

	Consumible actualizar(Long id, Consumible datos);

	void eliminar(Long id);

	List<MovimientoConsumible> listarMovimientos(Long consumibleId);

	List<MovimientoConsumible> listarTodosMovimientos();

	PaginaResultado<MovimientoConsumible> listarTodosMovimientosPagina(ConsultaPaginada consulta);

	PaginaResultado<MovimientoConsumible> listarMovimientosPagina(Long consumibleId, ConsultaPaginada consulta);

	MovimientoConsumible registrarMovimiento(Long consumibleId, MovimientoConsumible movimiento);

	MovimientoConsumible actualizarMovimiento(Long id, MovimientoConsumible datos);

	void eliminarMovimiento(Long id);
}