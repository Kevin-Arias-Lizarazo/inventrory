package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ResultadoBusqueda;

public interface BusquedaCasoDeUso {

	List<ResultadoBusqueda> buscar(String q);
}