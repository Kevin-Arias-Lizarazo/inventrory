package com.art.inventario.controlador;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.BackupCasoDeUso;

@RestController
@RequestMapping("/api/backup")
public class BackupControlador {
	private final BackupCasoDeUso servicio;
	public BackupControlador(BackupCasoDeUso servicio) { this.servicio = servicio; }

	@GetMapping
	public ResponseEntity<byte[]> descargar() {
		byte[] data = servicio.crearBackup();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventario-backup.db\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(data);
	}

	@PostMapping("/restaurar")
	public ResponseEntity<Map<String, String>> restaurar(@RequestParam("archivo") MultipartFile archivo) {
		try {
			servicio.restaurar(archivo.getBytes());
			return ResponseEntity.ok(Map.of(
					"mensaje",
					"Backup validado. La restauración se aplicará al reiniciar el servicio"));
		} catch (Exception e) {
			throw new DatosInvalidosExcepcion("No se pudo leer el archivo: " + e.getMessage());
		}
	}
}
