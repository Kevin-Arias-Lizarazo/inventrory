package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proyecto;

public interface ProyectoCasoDeUso {

	List<Proyecto> listar(String estado);

	PaginaResultado<Proyecto> listarPagina(String estado, String q, int pagina, int tamano);

	Proyecto obtener(Long id);

	Proyecto crear(Proyecto proyecto);

	Proyecto actualizar(Long id, Proyecto datos);

	Proyecto finalizar(Long id);

	void eliminar(Long id);
}