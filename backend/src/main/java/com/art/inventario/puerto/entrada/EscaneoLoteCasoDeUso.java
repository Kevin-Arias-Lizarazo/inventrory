package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.LoteEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoLoteEscaneo;

public interface EscaneoLoteCasoDeUso {

	List<ResultadoLoteEscaneo> procesar(List<LoteEscaneo> lotes);
}
