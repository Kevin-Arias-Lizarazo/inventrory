package com.art.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.art.inventario.dominio.ResumenDashboard;
import com.art.inventario.puerto.entrada.DashboardCasoDeUso;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControlador {
	private final DashboardCasoDeUso servicio;
	public DashboardControlador(DashboardCasoDeUso servicio) { this.servicio = servicio; }

	@GetMapping
	public ResponseEntity<ResumenDashboard> resumen(
			@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta) {
		return ResponseEntity.ok(servicio.resumen(desde, hasta));
	}
}
