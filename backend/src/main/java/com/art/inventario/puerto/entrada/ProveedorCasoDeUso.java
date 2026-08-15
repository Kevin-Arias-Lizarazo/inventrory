package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proveedor;

public interface ProveedorCasoDeUso {

	List<Proveedor> listar();

	PaginaResultado<Proveedor> listarPagina(int pagina, int tamano);

	Proveedor obtener(Long id);

	Proveedor crear(Proveedor proveedor);

	Proveedor actualizar(Long id, Proveedor datos);

	void eliminar(Long id);
}