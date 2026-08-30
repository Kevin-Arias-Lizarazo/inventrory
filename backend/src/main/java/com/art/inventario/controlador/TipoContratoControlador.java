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
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.puerto.entrada.TipoContratoCasoDeUso;

@RestController
@RequestMapping("/api/tipos-contrato")
public class TipoContratoControlador {

	private final TipoContratoCasoDeUso servicio;

	public TipoContratoControlador(TipoContratoCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<TipoContrato>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<TipoContrato>> listarPagina(
			@RequestParam(required = false) Integer pagina,
			@RequestParam(required = false) Integer tamano) {
		return ResponseEntity.ok(servicio.listarPagina(
				PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TipoContrato> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<TipoContrato> crear(@RequestBody TipoContrato tipoContrato) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(tipoContrato));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TipoContrato> actualizar(@PathVariable Long id, @RequestBody TipoContrato datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
