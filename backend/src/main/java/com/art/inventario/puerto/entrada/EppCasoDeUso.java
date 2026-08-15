package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;

public interface EppCasoDeUso {

	List<Epp> listar();

	PaginaResultado<Epp> listarPagina(int pagina, int tamano);

	Epp obtener(Long id);

	Epp crear(Epp epp);

	Epp actualizar(Long id, Epp datos);

	void eliminar(Long id);
}