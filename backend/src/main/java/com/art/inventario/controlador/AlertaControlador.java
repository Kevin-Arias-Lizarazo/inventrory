package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.AlertaReposicion;
import com.art.inventario.dominio.AlertaVencimientoEpp;
import com.art.inventario.puerto.entrada.AlertaCasoDeUso;

@RestController
@RequestMapping("/api/alertas")
public class AlertaControlador {

	private final AlertaCasoDeUso servicio;

	public AlertaControlador(AlertaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/reposicion")
	public ResponseEntity<List<AlertaReposicion>> listarReposicion() {
		return ResponseEntity.ok(servicio.listarReposicion());
	}

	@GetMapping("/epp-vencimiento")
	public ResponseEntity<List<AlertaVencimientoEpp>> listarVencimientoEpp(
			@RequestParam(defaultValue = "30") int dias) {
		return ResponseEntity.ok(servicio.listarVencimientoEpp(dias));
	}
}
