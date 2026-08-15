package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.Devolucion;
import com.art.inventario.puerto.entrada.DevolucionCasoDeUso;

@RestController
@RequestMapping("/api")
public class DevolucionControlador {

	private final DevolucionCasoDeUso servicio;

	public DevolucionControlador(DevolucionCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/devoluciones")
	public ResponseEntity<List<Devolucion>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/devoluciones/{id}")
	public ResponseEntity<Devolucion> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@GetMapping("/compras/{compraId}/devoluciones")
	public ResponseEntity<List<Devolucion>> listarPorCompra(@PathVariable Long compraId) {
		return ResponseEntity.ok(servicio.listarPorCompra(compraId));
	}

	@PostMapping("/compras/{compraId}/devoluciones")
	public ResponseEntity<Devolucion> crear(@PathVariable Long compraId, @RequestBody Devolucion devolucion) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(compraId, devolucion));
	}

	@DeleteMapping("/devoluciones/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
