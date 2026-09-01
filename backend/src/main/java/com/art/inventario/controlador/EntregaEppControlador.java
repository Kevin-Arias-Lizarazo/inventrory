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
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.puerto.entrada.EntregaEppCasoDeUso;

@RestController
@RequestMapping("/api/entregas-epp")
public class EntregaEppControlador {

	private final EntregaEppCasoDeUso servicio;

	public EntregaEppControlador(EntregaEppCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<EntregaEpp>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/filtradas")
	public ResponseEntity<PaginaResultado<EntregaEpp>> listarFiltradas(
			@RequestParam(required = false) String fecha,
			@RequestParam(required = false) Long empleadoId,
			@RequestParam(required = false) Long eppId,
			@RequestParam(defaultValue = "desc") String orden,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarFiltradas(fecha, empleadoId, eppId, orden, pagina, tamano));
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<EntregaEpp>> listarPagina(
			@RequestParam Map<String, String> params) {
		return ResponseEntity.ok(servicio.listarPagina(ConsultaPaginada.desdeParams(params)));
	}

@GetMapping("/{id}")
	public ResponseEntity<EntregaEpp> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<EntregaEpp> crear(@RequestBody EntregaEpp entrega) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(entrega));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EntregaEpp> actualizar(@PathVariable Long id, @RequestBody EntregaEpp datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}