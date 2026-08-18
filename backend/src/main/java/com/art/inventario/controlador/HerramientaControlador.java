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
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;

@RestController
@RequestMapping("/api/herramientas")
public class HerramientaControlador {

	private final HerramientaCasoDeUso servicio;

	public HerramientaControlador(HerramientaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Herramienta>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Herramienta>> listarPagina(
		@RequestParam(defaultValue = "0") int pagina,
		@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(pagina, tamano));
	}

@GetMapping("/{id}")
	public ResponseEntity<Herramienta> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Herramienta> crear(@RequestBody Herramienta herramienta) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(herramienta));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Herramienta> actualizar(@PathVariable Long id, @RequestBody Herramienta datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@PostMapping("/{id}/danada")
	public ResponseEntity<Herramienta> registrarDanada(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.registrarDanada(id));
	}

	@PostMapping("/{id}/reparar")
	public ResponseEntity<Herramienta> reparar(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.reparar(id));
	}

	@PostMapping("/{id}/desechar-danada")
	public ResponseEntity<Herramienta> desecharDanada(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.desecharDanada(id));
	}

	@PostMapping("/{id}/perdida")
	public ResponseEntity<Herramienta> registrarPerdida(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.registrarPerdida(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
