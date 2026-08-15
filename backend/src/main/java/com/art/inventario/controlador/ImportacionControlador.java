package com.art.inventario.controlador;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.art.inventario.puerto.entrada.ImportacionCasoDeUso;

@RestController
@RequestMapping("/api/importar")
public class ImportacionControlador {
	private final ImportacionCasoDeUso servicio;
	public ImportacionControlador(ImportacionCasoDeUso servicio) { this.servicio = servicio; }

	@PostMapping("/{recurso}")
	public ResponseEntity<Map<String, Object>> importar(@PathVariable String recurso, @RequestBody String csv) {
		return ResponseEntity.ok(servicio.importarCsv(recurso, csv));
	}
}
