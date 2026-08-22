package com.art.inventario.persistencia.adaptador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.art.inventario.dominio.EventoLog;
import com.art.inventario.puerto.salida.AuditoriaPersistencia;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import tools.jackson.databind.ObjectMapper;

@Repository
public class RegistroAuditoriaArchivo implements RegistroAuditoria, AuditoriaPersistencia {

	private static final DateTimeFormatter NOMBRE = DateTimeFormatter.ofPattern("dd_MM_yyyy");

	private final Path directorio;
	private final ObjectMapper json;

	public RegistroAuditoriaArchivo(@Value("${app.logs.dir:logs}") String logsDir, ObjectMapper json) {
		this.directorio = Paths.get(logsDir).toAbsolutePath().normalize();
		this.json = json;
		try {
			Files.createDirectories(directorio);
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo crear el directorio de logs: " + logsDir, e);
		}
	}

	@Override
	public synchronized void registrar(EventoLog evento) {
		try {
			boolean esGet = "GET".equalsIgnoreCase(evento.getMetodo());
			String prefijo = esGet ? "log_get_" : "log_";
			String nombre = prefijo + LocalDate.now().format(NOMBRE) + ".jsonl";
			Path archivo = directorio.resolve(nombre);
			String linea = json.writeValueAsString(evento) + System.lineSeparator();
			Files.write(archivo, linea.getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception e) {
			System.err.println("ERROR_LOGS: " + e.getMessage());
		}
	}

	@Override
	public List<EventoLog> leer(String fecha, String usuario, String accion, String resultado) {
		List<Path> archivos = new ArrayList<>();
		if (fecha != null && !fecha.isBlank()) {
			String base = formatearFecha(fecha);
			Path normal = directorio.resolve("log_" + base + ".jsonl");
			Path get = directorio.resolve("log_get_" + base + ".jsonl");
			if (Files.exists(normal)) {
				archivos.add(normal);
			}
			if (Files.exists(get)) {
				archivos.add(get);
			}
		} else {
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(directorio)) {
				for (Path p : ds) {
					String n = p.getFileName().toString();
					if (n.startsWith("log_") && n.endsWith(".jsonl")) {
						archivos.add(p);
					}
				}
			} catch (IOException e) {
				return List.of();
			}
			archivos.sort(Comparator.comparing(p -> p.getFileName().toString()));
		}
		List<EventoLog> resultadoLista = new ArrayList<>();
		for (Path p : archivos) {
			try (Stream<String> lineas = Files.lines(p, StandardCharsets.UTF_8)) {
				lineas.forEach(linea -> {
					try {
						EventoLog e = json.readValue(linea, EventoLog.class);
						if (coincide(e, usuario, accion, resultado)) {
							resultadoLista.add(e);
						}
					} catch (Exception ignored) {
						// línea malformada se ignora
					}
				});
			} catch (IOException e) {
				// archivo no legible se ignora
			}
		}
		return resultadoLista;
	}

	@Override
	public List<String> fechasDisponibles() {
		List<String> fechas = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directorio)) {
			for (Path p : ds) {
				String n = p.getFileName().toString();
				if (n.startsWith("log_") && n.endsWith(".jsonl")) {
					String base = n.replace("log_get_", "").replace("log_", "").replace(".jsonl", "");
					fechas.add(base);
				}
			}
		} catch (IOException e) {
			return List.of();
		}
		return fechas.stream().distinct().sorted().toList();
	}

	private boolean coincide(EventoLog e, String usuario, String accion, String resultado) {
		if (usuario != null && !usuario.isBlank() && !usuario.equalsIgnoreCase(e.getUsuario() == null ? "" : e.getUsuario())) {
			return false;
		}
		if (accion != null && !accion.isBlank()
				&& !accion.equalsIgnoreCase(e.getAccion() == null ? "" : e.getAccion().name())) {
			return false;
		}
		if (resultado != null && !resultado.isBlank()
				&& !resultado.equalsIgnoreCase(e.getResultado() == null ? "" : e.getResultado())) {
			return false;
		}
		return true;
	}

	private static String formatearFecha(String fecha) {
		try {
			LocalDate d = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT));
			return d.format(NOMBRE);
		} catch (Exception e) {
			return fecha;
		}
	}
}