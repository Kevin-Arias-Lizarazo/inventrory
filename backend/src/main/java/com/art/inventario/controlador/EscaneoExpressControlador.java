package com.art.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.CreacionExpressEscaneo;
import com.art.inventario.aplicacion.dto.IncrementoStockEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoExpress;
import com.art.inventario.puerto.entrada.EscaneoExpressCasoDeUso;

@RestController
@RequestMapping("/api/escaneos")
public class EscaneoExpressControlador {

	private final EscaneoExpressCasoDeUso servicio;

	public EscaneoExpressControlador(EscaneoExpressCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@PostMapping("/incrementar-stock")
	public ResponseEntity<ResultadoExpress> incrementarStock(@RequestBody IncrementoStockEscaneo request) {
		return ResponseEntity.ok(servicio.incrementarStock(request));
	}

	@PostMapping("/items")
	public ResponseEntity<ResultadoExpress> crearItem(@RequestBody CreacionExpressEscaneo request) {
		return ResponseEntity.ok(servicio.crearItem(request));
	}
}
