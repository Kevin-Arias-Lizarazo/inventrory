package com.art.inventario.puerto.entrada;

import com.art.inventario.aplicacion.dto.CreacionExpressEscaneo;
import com.art.inventario.aplicacion.dto.IncrementoStockEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoExpress;

public interface EscaneoExpressCasoDeUso {

	ResultadoExpress incrementarStock(IncrementoStockEscaneo request);

	ResultadoExpress crearItem(CreacionExpressEscaneo request);
}
