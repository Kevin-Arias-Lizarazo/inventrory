package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;

@RestController
@RequestMapping("/api")
public class MovimientoConsumibleControlador {

	private final ConsumibleCasoDeUso servicio;

	public MovimientoConsumibleControlador(ConsumibleCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/consumibles/{id}/movimientos")
	public ResponseEntity<List<MovimientoConsumible>> porConsumible(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarMovimientos(id));
	}

	@PostMapping("/consumibles/{id}/movimientos")
	public ResponseEntity<MovimientoConsumible> crear(@PathVariable Long id, @RequestBody MovimientoConsumible movimiento) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarMovimiento(id, movimiento));
	}

	@GetMapping("/movimientos-consumibles")
	public ResponseEntity<List<MovimientoConsumible>> todos() {
		return ResponseEntity.ok(servicio.listarTodosMovimientos());
	}

	@PutMapping("/movimientos-consumibles/{id}")
	public ResponseEntity<MovimientoConsumible> actualizar(@PathVariable Long id, @RequestBody MovimientoConsumible datos) {
		return ResponseEntity.ok(servicio.actualizarMovimiento(id, datos));
	}

	@DeleteMapping("/movimientos-consumibles/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminarMovimiento(id);
		return ResponseEntity.noContent().build();
	}
}