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
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.puerto.entrada.AsignacionConsumibleCasoDeUso;

@RestController
@RequestMapping("/api/asignaciones-consumibles")
public class AsignacionConsumibleControlador {

	private final AsignacionConsumibleCasoDeUso servicio;

	public AsignacionConsumibleControlador(AsignacionConsumibleCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<AsignacionConsumible>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<AsignacionConsumible>> listarPagina(
		@RequestParam Map<String, String> params) {
		return ResponseEntity.ok(servicio.listarPagina(ConsultaPaginada.desdeParams(params)));
	}

@GetMapping("/{id}")
	public ResponseEntity<AsignacionConsumible> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<AsignacionConsumible> crear(@RequestBody AsignacionConsumible asignacion) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(asignacion));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AsignacionConsumible> actualizar(@PathVariable Long id, @RequestBody AsignacionConsumible datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}