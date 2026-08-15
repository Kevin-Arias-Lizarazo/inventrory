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

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Minuta;
import com.art.inventario.puerto.entrada.MinutaCasoDeUso;

@RestController
@RequestMapping("/api/minutas")
public class MinutaControlador {

	private final MinutaCasoDeUso servicio;

	public MinutaControlador(MinutaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Minuta>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/recientes")
	public ResponseEntity<PaginaResultado<Minuta>> listarRecientes(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPaginaRecientes(pagina, tamano));
	}

	@GetMapping("/filtradas")
	public ResponseEntity<PaginaResultado<Minuta>> listarFiltradas(
			@RequestParam(required = false) String fecha,
			@RequestParam(required = false) Long empleadoId,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "desc") String orden,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarFiltradas(fecha, empleadoId, q, orden, pagina, tamano));
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Minuta>> listarPagina(
		@RequestParam(required = false) String q,
		@RequestParam(defaultValue = "0") int pagina,
		@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, pagina, tamano));
	}

@GetMapping("/{id}")
	public ResponseEntity<Minuta> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Minuta> crear(@RequestBody Minuta minuta) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(minuta));
	}

	@PostMapping("/lote")
	public ResponseEntity<Map<String, Integer>> crearLote(@RequestBody List<Minuta> minutas) {
		return ResponseEntity.ok(Map.of("creadas", servicio.crearLote(minutas)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Minuta> actualizar(@PathVariable Long id, @RequestBody Minuta datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}