package com.art.inventario.aplicacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.aplicacion.dto.LoteEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoLoteEscaneo;
import com.art.inventario.puerto.entrada.EscaneoLoteCasoDeUso;

@Service
public class EscaneoLoteAplicacion implements EscaneoLoteCasoDeUso {

	private final LoteEscaneoProcesador procesador;

	public EscaneoLoteAplicacion(LoteEscaneoProcesador procesador) {
		this.procesador = procesador;
	}

	@Override
	public List<ResultadoLoteEscaneo> procesar(List<LoteEscaneo> lotes) {
		if (lotes == null || lotes.isEmpty()) {
			throw new com.art.inventario.excepcion.DatosInvalidosExcepcion("No hay lotes para procesar");
		}
		List<ResultadoLoteEscaneo> resultados = new ArrayList<>();
		for (LoteEscaneo lote : lotes) {
			try {
				resultados.add(procesador.procesar(lote));
			} catch (Exception e) {
				resultados.add(new ResultadoLoteEscaneo(lote.getTipo(), lote.getDestinoCodigo(),
						false, e.getMessage(), 0));
			}
		}
		return resultados;
	}
}
