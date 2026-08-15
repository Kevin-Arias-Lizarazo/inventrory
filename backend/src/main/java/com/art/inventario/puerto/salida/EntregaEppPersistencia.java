package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaEpp;

public interface EntregaEppPersistencia {

	List<EntregaEpp> listar();

	PaginaResultado<EntregaEpp> listarPagina(int pagina, int tamano);

	EntregaEpp obtener(Long id);

	EntregaEpp guardar(EntregaEpp entrega);

	void eliminar(Long id);

	boolean tieneEntregasConEpp(Long eppId);
}