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
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.Devolucion;
import com.art.inventario.puerto.entrada.CompraCasoDeUso;
import com.art.inventario.puerto.entrada.DevolucionCasoDeUso;

@RestController
@RequestMapping("/api/compras")
public class CompraControlador {

	private final CompraCasoDeUso servicio;
	private final DevolucionCasoDeUso devolucionServicio;

	public CompraControlador(CompraCasoDeUso servicio, DevolucionCasoDeUso devolucionServicio) {
		this.servicio = servicio;
		this.devolucionServicio = devolucionServicio;
	}

	@GetMapping
	public ResponseEntity<List<Compra>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Compra>> listarPagina(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Long proveedorId,
			@RequestParam(required = false) String fecha,
			@RequestParam(required = false) Boolean facturada,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, proveedorId, fecha, facturada, pagina, tamano));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Compra> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Compra> crear(@RequestBody Compra compra) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(compra));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Compra> actualizar(@PathVariable Long id, @RequestBody Compra datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{compraId}/devoluciones")
	public ResponseEntity<List<Devolucion>> listarDevoluciones(@PathVariable Long compraId) {
		return ResponseEntity.ok(devolucionServicio.listarPorCompra(compraId));
	}

	@PostMapping("/{compraId}/devoluciones")
	public ResponseEntity<Devolucion> crearDevolucion(@PathVariable Long compraId,
			@RequestBody Devolucion devolucion) {
		return ResponseEntity.status(HttpStatus.CREATED).body(devolucionServicio.crear(compraId, devolucion));
	}
}
