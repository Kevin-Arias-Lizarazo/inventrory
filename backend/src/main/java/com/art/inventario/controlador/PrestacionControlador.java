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
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.puerto.entrada.PrestacionCasoDeUso;

@RestController
@RequestMapping("/api/prestaciones")
public class PrestacionControlador {

	private final PrestacionCasoDeUso servicio;

	public PrestacionControlador(PrestacionCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<Prestacion>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Prestacion>> listarPagina(
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamano) {
		return ResponseEntity.ok(servicio.listarPagina(
				PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano)));
	}

	@GetMapping("/tipo/{tipoContratoId}")
	public ResponseEntity<List<Prestacion>> listarPorTipoContrato(@PathVariable Long tipoContratoId) {
		return ResponseEntity.ok(servicio.listarPorTipoContrato(tipoContratoId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Prestacion> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Prestacion> crear(@RequestBody Prestacion prestacion) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(prestacion));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Prestacion> actualizar(@PathVariable Long id, @RequestBody Prestacion datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
