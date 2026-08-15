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
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.puerto.entrada.EntregaRopaCasoDeUso;

@RestController
@RequestMapping("/api/entregas-ropa")
public class EntregaRopaControlador {

	private final EntregaRopaCasoDeUso servicio;

	public EntregaRopaControlador(EntregaRopaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<EntregaRopa>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<EntregaRopa>> listarPagina(
		@RequestParam(defaultValue = "0") int pagina,
		@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(pagina, tamano));
	}

@GetMapping("/{id}")
	public ResponseEntity<EntregaRopa> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<EntregaRopa> crear(@RequestBody EntregaRopa entrega) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(entrega));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EntregaRopa> actualizar(@PathVariable Long id, @RequestBody EntregaRopa datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}