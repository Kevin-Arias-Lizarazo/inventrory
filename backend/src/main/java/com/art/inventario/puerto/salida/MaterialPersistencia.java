package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoMaterial;

public interface MaterialPersistencia {

	List<Material> listar();

	PaginaResultado<Material> listarPagina(int pagina, int tamano);

	boolean existeNombre(String nombre, Long excluirId);

	Material obtener(Long id);

	Material guardar(Material material);

	void eliminar(Long id);

	boolean tieneMovimientos(Long id);

	List<MovimientoMaterial> listarMovimientos(Long materialId);

	List<MovimientoMaterial> listarTodosMovimientos();

	MovimientoMaterial obtenerMovimiento(Long id);

	MovimientoMaterial guardarMovimiento(MovimientoMaterial movimiento);

	void eliminarMovimiento(MovimientoMaterial movimiento);
}