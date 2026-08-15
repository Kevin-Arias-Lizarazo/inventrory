package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.BloqueEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoBloque;
import com.art.inventario.puerto.entrada.EscaneoCasoDeUso;

@RestController
@RequestMapping("/api/escaneos")
public class EscaneoControlador {

	private final EscaneoCasoDeUso servicio;

	public EscaneoControlador(EscaneoCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@PostMapping
	public ResponseEntity<List<ResultadoBloque>> procesar(@RequestBody List<BloqueEscaneo> bloques) {
		return ResponseEntity.ok(servicio.procesar(bloques));
	}
}