package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.LoteEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoLoteEscaneo;
import com.art.inventario.puerto.entrada.EscaneoLoteCasoDeUso;

@RestController
@RequestMapping("/api/escaneos")
public class EscaneoLoteControlador {

	private final EscaneoLoteCasoDeUso servicio;

	public EscaneoLoteControlador(EscaneoLoteCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@PostMapping("/lote")
	public ResponseEntity<List<ResultadoLoteEscaneo>> procesarLote(@RequestBody List<LoteEscaneo> lotes) {
		return ResponseEntity.ok(servicio.procesar(lotes));
	}
}
