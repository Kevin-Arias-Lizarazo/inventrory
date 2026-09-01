package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.MovimientoEpp;

public interface EppPersistencia {

	List<Epp> listar();

	PaginaResultado<Epp> listarPagina(int pagina, int tamano);

	PaginaResultado<Epp> listarPagina(ConsultaPaginada consulta);

	boolean existeNombre(String nombre, Long excluirId);

	Epp obtener(Long id);

	Epp guardar(Epp epp);

	void eliminar(Long id);

	boolean tieneMovimientos(Long id);

	List<MovimientoEpp> listarMovimientos(Long eppId);

	List<MovimientoEpp> listarTodosMovimientos();

	PaginaResultado<MovimientoEpp> listarTodosMovimientosPagina(ConsultaPaginada consulta);

	PaginaResultado<MovimientoEpp> listarMovimientosPagina(Long eppId, ConsultaPaginada consulta);

	MovimientoEpp obtenerMovimiento(Long id);

	MovimientoEpp guardarMovimiento(MovimientoEpp movimiento);

	void eliminarMovimiento(MovimientoEpp movimiento);
}