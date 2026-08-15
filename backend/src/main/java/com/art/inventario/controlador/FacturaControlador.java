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

import com.art.inventario.dominio.Factura;
import com.art.inventario.puerto.entrada.FacturaCasoDeUso;

@RestController
@RequestMapping("/api/facturas")
public class FacturaControlador {

	private final FacturaCasoDeUso servicio;

	public FacturaControlador(FacturaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Factura>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Factura> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Factura> crear(@RequestBody Factura factura) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(factura));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Factura> actualizar(@PathVariable Long id, @RequestBody Factura datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}