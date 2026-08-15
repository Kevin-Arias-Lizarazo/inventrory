package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;

public interface EppPersistencia {

	List<Epp> listar();

	PaginaResultado<Epp> listarPagina(int pagina, int tamano);

	boolean existeNombre(String nombre, Long excluirId);

	Epp obtener(Long id);

	Epp guardar(Epp epp);

	void eliminar(Long id);
}