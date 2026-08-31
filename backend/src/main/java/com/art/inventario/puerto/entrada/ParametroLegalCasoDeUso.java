package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.dominio.ParametroLegal;

public interface ParametroLegalCasoDeUso {

	List<ParametroLegal> listar();

	ParametroLegal obtener(Long id);

	ParametroLegal crear(ParametroLegal parametro);

	ParametroLegal actualizar(Long id, ParametroLegal datos);

	void eliminar(Long id);
}
