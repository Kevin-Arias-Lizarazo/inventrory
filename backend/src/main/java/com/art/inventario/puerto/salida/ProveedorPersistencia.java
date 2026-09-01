package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proveedor;

public interface ProveedorPersistencia {

	List<Proveedor> listar();

	PaginaResultado<Proveedor> listarPagina(int pagina, int tamano);

	PaginaResultado<Proveedor> listarPagina(ConsultaPaginada consulta);

	boolean existeNombre(String nombre, Long excluirId);

	Proveedor obtener(Long id);

	Proveedor guardar(Proveedor proveedor);

	void eliminar(Long id);

	boolean tieneComprasOFacturas(Long id);
}