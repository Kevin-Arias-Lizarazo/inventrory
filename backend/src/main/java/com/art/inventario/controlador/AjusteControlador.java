package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.Ajuste;
import com.art.inventario.puerto.entrada.AjusteCasoDeUso;

@RestController
@RequestMapping("/api/ajustes")
public class AjusteControlador {

	private final AjusteCasoDeUso servicio;

	public AjusteControlador(AjusteCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Ajuste>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@PostMapping
	public ResponseEntity<Ajuste> crear(@RequestBody Ajuste ajuste) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(ajuste));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
