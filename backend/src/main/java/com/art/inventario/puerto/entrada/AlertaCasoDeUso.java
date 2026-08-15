package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.dominio.AlertaReposicion;
import com.art.inventario.dominio.AlertaVencimientoEpp;

public interface AlertaCasoDeUso {

	List<AlertaReposicion> listarReposicion();

	List<AlertaVencimientoEpp> listarVencimientoEpp(int dias);
}
