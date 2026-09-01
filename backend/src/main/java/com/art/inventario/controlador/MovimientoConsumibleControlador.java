package com.art.inventario.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;

@RestController
@RequestMapping("/api/movimientos-consumibles")
public class MovimientoConsumibleControlador {

	private final ConsumibleCasoDeUso servicio;

	public MovimientoConsumibleControlador(ConsumibleCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<MovimientoConsumible>> todos() {
		return ResponseEntity.ok(servicio.listarTodosMovimientos());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<MovimientoConsumible>> paginado(
			@RequestParam Map<String, String> params) {
		return ResponseEntity.ok(servicio.listarTodosMovimientosPagina(ConsultaPaginada.desdeParams(params)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<MovimientoConsumible> actualizar(@PathVariable Long id,
			@RequestBody MovimientoConsumible datos) {
		return ResponseEntity.ok(servicio.actualizarMovimiento(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminarMovimiento(id);
		return ResponseEntity.noContent().build();
	}
}
