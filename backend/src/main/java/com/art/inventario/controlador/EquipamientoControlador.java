package com.art.inventario.controlador;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.art.inventario.puerto.entrada.EquipamientoCasoDeUso;

@RestController
@RequestMapping("/api/empleados")
public class EquipamientoControlador {
	private final EquipamientoCasoDeUso servicio;
	public EquipamientoControlador(EquipamientoCasoDeUso servicio) { this.servicio = servicio; }

	@GetMapping("/{id}/equipamiento")
	public ResponseEntity<Map<String, Object>> equipamiento(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.equipamientoEmpleado(id));
	}
}
