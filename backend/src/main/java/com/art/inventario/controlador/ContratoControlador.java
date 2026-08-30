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
import com.art.inventario.aplicacion.dto.PrestacionesContrato;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionExtra;
import com.art.inventario.puerto.entrada.ContratoCasoDeUso;

@RestController
@RequestMapping("/api/contratos")
public class ContratoControlador {

	private final ContratoCasoDeUso servicio;

	public ContratoControlador(ContratoCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Contrato>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Contrato>> listarPagina(
		@RequestParam(required = false) String q,
		@RequestParam(defaultValue = "0") int pagina,
		@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, pagina, tamano));
	}

@GetMapping("/{id}")
	public ResponseEntity<Contrato> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Contrato> crear(@RequestBody Contrato contrato) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(contrato));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Contrato> actualizar(@PathVariable Long id, @RequestBody Contrato datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@PostMapping("/{id}/concluir")
	public ResponseEntity<Contrato> concluir(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.concluir(id));
	}

	@PostMapping("/{id}/calcular-prestaciones")
	public ResponseEntity<PrestacionesContrato> calcularPrestaciones(@PathVariable Long id) {
		servicio.calcularPrestaciones(id);
		return ResponseEntity.ok(servicio.listarPrestaciones(id));
	}

	@GetMapping("/{id}/prestaciones")
	public ResponseEntity<PrestacionesContrato> listarPrestaciones(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.listarPrestaciones(id));
	}

	@PostMapping("/{id}/prestaciones")
	public ResponseEntity<ContratoPrestacionExtra> agregarExtra(@PathVariable Long id,
			@RequestBody ContratoPrestacionExtra extra) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.agregarExtra(id, extra));
	}

	@DeleteMapping("/{id}/prestaciones/{extraId}")
	public ResponseEntity<Void> eliminarExtra(@PathVariable Long id, @PathVariable Long extraId) {
		servicio.eliminarExtra(id, extraId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}