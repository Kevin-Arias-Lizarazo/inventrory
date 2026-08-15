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

import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;

@RestController
@RequestMapping("/api")
public class MovimientoHerramientaControlador {

	private final HerramientaCasoDeUso servicio;

	public MovimientoHerramientaControlador(HerramientaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/herramientas/{id}/movimientos")
	public ResponseEntity<List<MovimientoHerramienta>> porHerramienta(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarMovimientos(id));
	}

	@PostMapping("/herramientas/{id}/movimientos")
	public ResponseEntity<MovimientoHerramienta> crear(@PathVariable Long id, @RequestBody MovimientoHerramienta movimiento) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarMovimiento(id, movimiento));
	}

	@GetMapping("/movimientos-herramientas")
	public ResponseEntity<List<MovimientoHerramienta>> todos() {
		return ResponseEntity.ok(servicio.listarTodosMovimientos());
	}

	@PutMapping("/movimientos-herramientas/{id}")
	public ResponseEntity<MovimientoHerramienta> actualizar(@PathVariable Long id, @RequestBody MovimientoHerramienta datos) {
		return ResponseEntity.ok(servicio.actualizarMovimiento(id, datos));
	}

	@DeleteMapping("/movimientos-herramientas/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminarMovimiento(id);
		return ResponseEntity.noContent().build();
	}
}