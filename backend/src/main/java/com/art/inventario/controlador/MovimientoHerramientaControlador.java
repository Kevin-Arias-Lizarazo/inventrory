package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;

@RestController
@RequestMapping("/api/movimientos-herramientas")
public class MovimientoHerramientaControlador {

	private final HerramientaCasoDeUso servicio;

	public MovimientoHerramientaControlador(HerramientaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<MovimientoHerramienta>> todos() {
		return ResponseEntity.ok(servicio.listarTodosMovimientos());
	}

	@PutMapping("/{id}")
	public ResponseEntity<MovimientoHerramienta> actualizar(@PathVariable Long id,
			@RequestBody MovimientoHerramienta datos) {
		return ResponseEntity.ok(servicio.actualizarMovimiento(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminarMovimiento(id);
		return ResponseEntity.noContent().build();
	}
}
