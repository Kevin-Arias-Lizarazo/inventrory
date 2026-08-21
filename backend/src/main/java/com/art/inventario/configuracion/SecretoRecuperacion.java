package com.art.inventario.configuracion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecretoRecuperacion {

	private final Path archivo;
	private final String desdeEntorno;

	public SecretoRecuperacion(@Value("${app.auth.secret.file:.env.auth}") String ruta,
			@Value("${AUTH_RECOVERY_SECRET:}") String desdeEntorno) {
		this.archivo = Paths.get(ruta).toAbsolutePath().normalize();
		this.desdeEntorno = desdeEntorno;
	}

	public String generar() {
		String secreto = SeguridadUtil.generarToken(40);
		try {
			Files.createDirectories(archivo.getParent() == null ? Paths.get(".") : archivo.getParent());
			Files.write(archivo, (secreto + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo guardar el secreto de recuperación", e);
		}
		return secreto;
	}

	public boolean valida(String candidato) {
		if (candidato == null || candidato.isBlank()) {
			return false;
		}
		String actual = obtener();
		if (actual == null || actual.isBlank()) {
			return false;
		}
		return MessageDigest.isEqual(
				actual.getBytes(StandardCharsets.UTF_8),
				candidato.getBytes(StandardCharsets.UTF_8));
	}

	public String obtener() {
		if (desdeEntorno != null && !desdeEntorno.isBlank()) {
			return desdeEntorno.trim();
		}
		try {
			if (Files.exists(archivo)) {
				return Files.readString(archivo).trim();
			}
		} catch (Exception e) {
			// ignorar
		}
		return null;
	}
}