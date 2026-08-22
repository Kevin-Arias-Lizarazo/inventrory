package com.art.inventario.controlador;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.art.inventario.aplicacion.dto.RespuestaInstalacion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.InstalacionCasoDeUso;

@RestController
@RequestMapping("/api/instalacion")
public class InstalacionControlador {

	private final InstalacionCasoDeUso servicio;

	public InstalacionControlador(InstalacionCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/estado")
	public ResponseEntity<Map<String, Boolean>> estado() {
		return ResponseEntity.ok(Map.of("pendiente", servicio.pendiente()));
	}

	@PostMapping("/completar")
	public ResponseEntity<RespuestaInstalacion> completar(
			@RequestParam("rootPassword") String rootPassword,
			@RequestParam(name = "db", required = false) MultipartFile db,
			@RequestParam(name = "uploads", required = false) MultipartFile uploads) {
		byte[] dbBytes = db == null || db.isEmpty() ? null : leer(db);
		byte[] uploadsBytes = uploads == null || uploads.isEmpty() ? null : leer(uploads);
		return ResponseEntity.ok(servicio.completar(rootPassword, dbBytes, uploadsBytes));
	}

	private byte[] leer(MultipartFile archivo) {
		try {
			return archivo.getBytes();
		} catch (Exception e) {
			throw new DatosInvalidosExcepcion("No se pudo leer el archivo subido");
		}
	}
}