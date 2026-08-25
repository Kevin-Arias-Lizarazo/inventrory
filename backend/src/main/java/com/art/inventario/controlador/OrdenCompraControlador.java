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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.OrdenCompra;
import com.art.inventario.puerto.entrada.OrdenCompraCasoDeUso;

@RestController
@RequestMapping("/api/ordenes-compra")
public class OrdenCompraControlador {

	private final OrdenCompraCasoDeUso servicio;

	public OrdenCompraControlador(OrdenCompraCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<OrdenCompra>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<OrdenCompra>> listarPagina(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Long proveedorId,
			@RequestParam(required = false) String fecha,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, proveedorId, fecha, pagina, tamano));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrdenCompra> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<OrdenCompra> crear(@RequestBody OrdenCompra orden) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(orden));
	}

	@PutMapping("/{id}")
	public ResponseEntity<OrdenCompra> actualizar(@PathVariable Long id, @RequestBody OrdenCompra datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
