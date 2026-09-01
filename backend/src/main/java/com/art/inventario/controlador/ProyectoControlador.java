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
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.puerto.entrada.ProyectoCasoDeUso;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoControlador {

	private final ProyectoCasoDeUso servicio;

	public ProyectoControlador(ProyectoCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Proyecto>> listar(@RequestParam(required = false) String estado) {
		return ResponseEntity.ok(servicio.listar(estado));
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Proyecto>> listarPagina(
		@RequestParam Map<String, String> params) {
		return ResponseEntity.ok(servicio.listarPagina(ConsultaPaginada.desdeParams(params)));
	}

@GetMapping("/{id}")
	public ResponseEntity<Proyecto> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Proyecto> crear(@RequestBody Proyecto proyecto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(proyecto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Proyecto> actualizar(@PathVariable Long id, @RequestBody Proyecto datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@PostMapping("/{id}/finalizar")
	public ResponseEntity<Proyecto> finalizar(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.finalizar(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}