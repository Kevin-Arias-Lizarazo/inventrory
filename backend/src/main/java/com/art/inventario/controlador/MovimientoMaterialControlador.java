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

import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;

@RestController
@RequestMapping("/api")
public class MovimientoMaterialControlador {

	private final MaterialCasoDeUso servicio;

	public MovimientoMaterialControlador(MaterialCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/materiales/{id}/movimientos")
	public ResponseEntity<List<MovimientoMaterial>> porMaterial(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarMovimientos(id));
	}

	@PostMapping("/materiales/{id}/movimientos")
	public ResponseEntity<MovimientoMaterial> crear(@PathVariable Long id, @RequestBody MovimientoMaterial movimiento) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarMovimiento(id, movimiento));
	}

	@GetMapping("/movimientos-materiales")
	public ResponseEntity<List<MovimientoMaterial>> todos() {
		return ResponseEntity.ok(servicio.listarTodosMovimientos());
	}

	@PutMapping("/movimientos-materiales/{id}")
	public ResponseEntity<MovimientoMaterial> actualizar(@PathVariable Long id, @RequestBody MovimientoMaterial datos) {
		return ResponseEntity.ok(servicio.actualizarMovimiento(id, datos));
	}

	@DeleteMapping("/movimientos-materiales/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminarMovimiento(id);
		return ResponseEntity.noContent().build();
	}
}