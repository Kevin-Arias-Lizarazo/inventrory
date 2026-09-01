package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoMaterial;

public interface MaterialCasoDeUso {

	List<Material> listar();

	PaginaResultado<Material> listarPagina(int pagina, int tamano);

	PaginaResultado<Material> listarPagina(ConsultaPaginada consulta);

	Material obtener(Long id);

	Material crear(Material material);

	Material actualizar(Long id, Material datos);

	void eliminar(Long id);

	List<MovimientoMaterial> listarMovimientos(Long materialId);

	List<MovimientoMaterial> listarTodosMovimientos();

	MovimientoMaterial registrarMovimiento(Long materialId, MovimientoMaterial movimiento);

	MovimientoMaterial actualizarMovimiento(Long id, MovimientoMaterial datos);

	void eliminarMovimiento(Long id);
}