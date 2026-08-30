package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.art.inventario.dominio.ParametroLegal;

public interface ParametroLegalPersistencia {

	List<ParametroLegal> listar();

	ParametroLegal obtener(Long id);

	ParametroLegal guardar(ParametroLegal parametro);

	Optional<ParametroLegal> porAnio(int anio);

	boolean existeAnio(int anio);
}
