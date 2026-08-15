package com.art.inventario.aplicacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.aplicacion.dto.BloqueEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoBloque;
import com.art.inventario.puerto.entrada.EscaneoCasoDeUso;

@Service
public class EscaneoAplicacion implements EscaneoCasoDeUso {

	private final EscaneoBloqueProcesador procesador;

	public EscaneoAplicacion(EscaneoBloqueProcesador procesador) {
		this.procesador = procesador;
	}

	@Override
	public List<ResultadoBloque> procesar(List<BloqueEscaneo> bloques) {
		if (bloques == null || bloques.isEmpty()) {
			throw new com.art.inventario.excepcion.DatosInvalidosExcepcion("No hay bloques para procesar");
		}
		List<ResultadoBloque> resultados = new ArrayList<>();
		for (BloqueEscaneo bloque : bloques) {
			try {
				resultados.add(procesador.procesar(bloque));
			} catch (Exception e) {
				resultados.add(new ResultadoBloque(bloque.getOperacion(), bloque.getDestinoCodigo(),
						false, e.getMessage(), 0));
			}
		}
		return resultados;
	}
}