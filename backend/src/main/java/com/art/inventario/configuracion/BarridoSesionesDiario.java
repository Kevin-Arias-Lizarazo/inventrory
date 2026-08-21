package com.art.inventario.configuracion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.dominio.Sesion;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.SesionPersistencia;

@Component
public class BarridoSesionesDiario implements ApplicationRunner {

	private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

	private final SesionPersistencia sesiones;
	private final RegistroAuditoria auditoria;
	private final Path marcador;

	public BarridoSesionesDiario(SesionPersistencia sesiones, RegistroAuditoria auditoria,
			@Value("${app.sesiones.dir:sesiones}") String sesionesDir) {
		this.sesiones = sesiones;
		this.auditoria = auditoria;
		this.marcador = Paths.get(sesionesDir).toAbsolutePath().normalize().resolve("ultimo-barrido.txt");
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			if (Files.exists(marcador)) {
				String ultimo = Files.readString(marcador, StandardCharsets.UTF_8).trim();
				if (LocalDate.now().format(FECHA).equals(ultimo)) {
					return;
				}
			}
			List<Sesion> vencidas = sesiones.vencidas(Instant.now());
			for (Sesion s : vencidas) {
				EventoLog e = new EventoLog();
				e.setFecha(Instant.now().toString());
				e.setUsuario(s.getUsername());
				e.setRol(s.getRol().name());
				e.setAccion(s.bloqueada() ? Accion.BLOQUEO_SESION : Accion.FIN_SESION);
				e.setResultado("OK");
				e.setDetalle("Barrido diario: sesión finalizada");
				auditoria.registrar(e);
				sesiones.terminar(s.getId());
			}
			Files.createDirectories(marcador.getParent());
			Files.write(marcador, (LocalDate.now().format(FECHA) + System.lineSeparator())
					.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			System.err.println("ERROR_BARRIDO: " + e.getMessage());
		}
	}
}