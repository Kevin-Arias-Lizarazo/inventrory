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
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.puerto.entrada.EppCasoDeUso;

@RestController
@RequestMapping("/api/epp")
public class EppControlador {

	private final EppCasoDeUso servicio;

	public EppControlador(EppCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Epp>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Epp>> listarPagina(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(pagina, tamano));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Epp> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Epp> crear(@RequestBody Epp epp) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(epp));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Epp> actualizar(@PathVariable Long id, @RequestBody Epp datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/movimientos")
	public ResponseEntity<List<MovimientoEpp>> listarMovimientos(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarMovimientos(id));
	}

	@PostMapping("/{id}/movimientos")
	public ResponseEntity<MovimientoEpp> crearMovimiento(@PathVariable Long id,
			@RequestBody MovimientoEpp movimiento) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarMovimiento(id, movimiento));
	}
}