package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.BloqueEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoBloque;

public interface EscaneoCasoDeUso {

	List<ResultadoBloque> procesar(List<BloqueEscaneo> bloques);
}