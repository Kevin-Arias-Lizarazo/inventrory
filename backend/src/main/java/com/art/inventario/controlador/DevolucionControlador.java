package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.dominio.Devolucion;
import com.art.inventario.puerto.entrada.DevolucionCasoDeUso;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionControlador {

	private final DevolucionCasoDeUso servicio;

	public DevolucionControlador(DevolucionCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Devolucion>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
