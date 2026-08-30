package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.puerto.entrada.ParametroLegalCasoDeUso;

@RestController
@RequestMapping("/api/parametros-legales")
public class ParametroLegalControlador {

	private final ParametroLegalCasoDeUso servicio;

	public ParametroLegalControlador(ParametroLegalCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<ParametroLegal>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ParametroLegal> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<ParametroLegal> crear(@RequestBody ParametroLegal parametro) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(parametro));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ParametroLegal> actualizar(@PathVariable Long id, @RequestBody ParametroLegal datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}
}
