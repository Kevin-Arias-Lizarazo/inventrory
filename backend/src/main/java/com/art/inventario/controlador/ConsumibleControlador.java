package com.art.inventario.controlador;

import java.util.List;
import java.util.Map;

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

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;

@RestController
@RequestMapping("/api/consumibles")
public class ConsumibleControlador {

	private final ConsumibleCasoDeUso servicio;

	public ConsumibleControlador(ConsumibleCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Consumible>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Consumible>> listarPagina(
			@RequestParam Map<String, String> params) {
		return ResponseEntity.ok(servicio.listarPagina(ConsultaPaginada.desdeParams(params)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Consumible> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Consumible> crear(@RequestBody Consumible consumible) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(consumible));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Consumible> actualizar(@PathVariable Long id, @RequestBody Consumible datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/movimientos")
	public ResponseEntity<List<MovimientoConsumible>> listarMovimientos(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarMovimientos(id));
	}

	@PostMapping("/{id}/movimientos")
	public ResponseEntity<MovimientoConsumible> crearMovimiento(@PathVariable Long id,
			@RequestBody MovimientoConsumible movimiento) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarMovimiento(id, movimiento));
	}
}