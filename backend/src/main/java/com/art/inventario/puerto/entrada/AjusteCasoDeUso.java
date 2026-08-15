package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.dominio.Ajuste;

public interface AjusteCasoDeUso {

	List<Ajuste> listar();

	Ajuste obtener(Long id);

	Ajuste crear(Ajuste ajuste);

	void eliminar(Long id);
}
