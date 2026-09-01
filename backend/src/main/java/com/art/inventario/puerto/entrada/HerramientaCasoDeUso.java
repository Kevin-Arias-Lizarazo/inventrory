package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.MovimientoHerramienta;

public interface HerramientaCasoDeUso {

	List<Herramienta> listar();

	PaginaResultado<Herramienta> listarPagina(int pagina, int tamano);

	PaginaResultado<Herramienta> listarPagina(ConsultaPaginada consulta);

	Herramienta obtener(Long id);

	Herramienta crear(Herramienta herramienta);

	Herramienta crearConCodigo(Herramienta herramienta);

	Herramienta actualizar(Long id, Herramienta datos);

	Herramienta registrarDanada(Long id);

	Herramienta reparar(Long id);

	Herramienta desecharDanada(Long id);

	Herramienta registrarPerdida(Long id);

	List<MovimientoHerramienta> listarMovimientos(Long herramientaId);

	List<MovimientoHerramienta> listarTodosMovimientos();

	MovimientoHerramienta registrarMovimiento(Long herramientaId, MovimientoHerramienta movimiento);

	MovimientoHerramienta actualizarMovimiento(Long id, MovimientoHerramienta datos);

	void eliminarMovimiento(Long id);

	void eliminar(Long id);
}
