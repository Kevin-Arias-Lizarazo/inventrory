package com.art.inventario.aplicacion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.art.inventario.RestauracionPendiente;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.BackupCasoDeUso;

@Service
public class BackupAplicacion implements BackupCasoDeUso {
	private final String dbPath;
	private final Path backupsDir;

	public BackupAplicacion(@Value("${INVENTARIO_DB:inventario.db}") String dbPath,
			@Value("${app.backups.dir:backups}") String backupsDir) {
		this.dbPath = dbPath;
		this.backupsDir = Paths.get(backupsDir);
	}

	@Override
	public byte[] crearBackup() {
		try {
			Files.createDirectories(backupsDir);
			Path origen = Paths.get(dbPath);
			if (!Files.exists(origen)) {
				throw new DatosInvalidosExcepcion("No se encontró la base de datos");
			}
			String nombre = "inventario-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".db";
			Path dest = backupsDir.resolve(nombre);
			Files.copy(origen, dest, StandardCopyOption.REPLACE_EXISTING);
			return Files.readAllBytes(dest);
		} catch (IOException e) {
			throw new DatosInvalidosExcepcion("No se pudo crear el backup: " + e.getMessage());
		}
	}

	@Override
	public void restaurar(byte[] contenido) {
		if (contenido == null || contenido.length == 0) {
			throw new DatosInvalidosExcepcion("Archivo de backup vacío");
		}
		// Solo se aceptan archivos que sean realmente SQLite (firma en los 16 primeros bytes).
		RestauracionPendiente.validarSqlite(contenido);
		try {
			Path destino = Paths.get(dbPath);
			// No se pisa la base activa en caliente: el archivo validado se deja en un
			// staging con nombre fijo dentro del MISMO directorio y el intercambio real
			// ocurre en InventarioApplication.main, antes de abrir la DataSource.
			Path staging = destino.resolveSibling(RestauracionPendiente.NOMBRE_STAGING);
			Path tmp = destino.resolveSibling(staging.getFileName().toString() + ".tmp");
			Files.write(tmp, contenido);
			Files.move(tmp, staging, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			throw new DatosInvalidosExcepcion("No se pudo preparar la restauración: " + e.getMessage());
		}
	}
}
