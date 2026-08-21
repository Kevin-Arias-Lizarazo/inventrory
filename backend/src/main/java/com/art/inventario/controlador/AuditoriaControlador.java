package com.art.inventario.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.puerto.entrada.AuditoriaCasoDeUso;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaControlador {

	private final AuditoriaCasoDeUso servicio;

	public AuditoriaControlador(AuditoriaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<PaginaResultado<EventoLog>> consultar(
			@RequestParam(required = false) String fecha,
			@RequestParam(required = false) String usuario,
			@RequestParam(required = false) String accion,
			@RequestParam(required = false) String resultado,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.consultar(fecha, usuario, accion, resultado, pagina, tamano));
	}

	@GetMapping("/fechas")
	public ResponseEntity<List<String>> fechas() {
		return ResponseEntity.ok(servicio.fechasDisponibles());
	}
}