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
import com.art.inventario.dominio.Empleado;
import com.art.inventario.puerto.entrada.EmpleadoCasoDeUso;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoControlador {

	private final EmpleadoCasoDeUso servicio;

	public EmpleadoControlador(EmpleadoCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Empleado>> listar(
			@RequestParam(required = false) String q,
			@RequestParam(required = false, name = "contratados") Boolean contratados) {
		return ResponseEntity.ok(servicio.listar(q, Boolean.TRUE.equals(contratados)));
	}

		@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Empleado>> listarPagina(
		@RequestParam(required = false) String q,
		@RequestParam(required = false) Boolean contratados,
		@RequestParam(defaultValue = "0") int pagina,
		@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, Boolean.TRUE.equals(contratados), pagina, tamano));
	}

@GetMapping("/{id}")
	public ResponseEntity<Empleado> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Empleado> crear(@RequestBody Empleado empleado) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(empleado));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @RequestBody Empleado datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}