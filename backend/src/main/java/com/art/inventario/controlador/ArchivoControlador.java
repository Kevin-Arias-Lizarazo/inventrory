package com.art.inventario.controlador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/archivos")
public class ArchivoControlador {

	@Value("${app.uploads.dir:uploads}")
	private String uploadsDir;

	@PostMapping
	public ResponseEntity<?> subir(@RequestParam("archivo") MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("mensaje", "El archivo está vacío"));
		}
		try {
			String original = archivo.getOriginalFilename();
			String extension = "";
			if (original != null && original.lastIndexOf('.') > 0) {
				extension = original.substring(original.lastIndexOf('.')).toLowerCase();
			}
			String nombre = UUID.randomUUID().toString().replace("-", "") + extension;
			Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
			Files.createDirectories(dir);
			Path destino = dir.resolve(nombre);
			archivo.transferTo(destino);
			return ResponseEntity.ok(Map.of("url", "/archivos/" + nombre));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("mensaje", "No se pudo guardar el archivo"));
		}
	}
}