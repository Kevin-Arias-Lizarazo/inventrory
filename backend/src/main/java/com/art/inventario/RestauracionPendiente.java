package com.art.inventario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.art.inventario.excepcion.DatosInvalidosExcepcion;

/**
 * Aplica en el arranque la restauración de base de datos diferida.
 *
 * <p>
 * La restauración nunca pisa la base activa en caliente: {@code BackupAplicacion}
 * deja el archivo validado en {@code restaurar-pendiente.db} (en el mismo
 * directorio que la base) y este helper lo intercambia con la base real ANTES de
 * que Spring abra la DataSource. Si el swap ocurriera con el pool abierto, SQLite
 * seguiría usando el inode viejo en Linux (pérdida de datos) o fallaría por lock
 * en Windows, por eso el intercambio debe suceder antes de {@code SpringApplication.run}.
 */
public final class RestauracionPendiente {

	/** Nombre fijo del archivo de staging en el directorio de la base de datos. */
	public static final String NOMBRE_STAGING = "restaurar-pendiente.db";

	private static final byte[] FIRMA_SQLITE = "SQLite format 3\u0000".getBytes(StandardCharsets.US_ASCII);

	private RestauracionPendiente() {
	}

	/**
	 * Valida que el contenido tenga la firma real de un archivo SQLite
	 * ("SQLite format 3\0" en los primeros 16 bytes).
	 */
	public static void validarSqlite(byte[] contenido) {
		if (contenido == null || contenido.length < FIRMA_SQLITE.length) {
			throw new DatosInvalidosExcepcion("El archivo no es una base de datos SQLite válida");
		}
		for (int i = 0; i < FIRMA_SQLITE.length; i++) {
			if (contenido[i] != FIRMA_SQLITE[i]) {
				throw new DatosInvalidosExcepcion("El archivo no es una base de datos SQLite válida");
			}
		}
	}

	/**
	 * Si existe {@code restaurar-pendiente.db}, lo valida y lo mueve sobre la base
	 * real (REPLACE_EXISTING), eliminando el staging. Un staging inválido se
	 * descarta sin bloquear el arranque.
	 */
	public static void aplicarSiExiste() {
		String dbPath = System.getProperty("INVENTARIO_DB");
		if (dbPath == null || dbPath.isBlank()) {
			dbPath = System.getenv("INVENTARIO_DB");
		}
		if (dbPath == null || dbPath.isBlank()) {
			dbPath = "inventario.db";
		}
		Path destino = Paths.get(dbPath);
		Path staging = destino.resolveSibling(NOMBRE_STAGING);
		if (!Files.exists(staging)) {
			return;
		}
		boolean aplicada = false;
		try {
			byte[] contenido = Files.readAllBytes(staging);
			validarSqlite(contenido);
			Files.move(staging, destino, StandardCopyOption.REPLACE_EXISTING);
			aplicada = true;
		} catch (DatosInvalidosExcepcion e) {
			// Staging inválido: se descarta para no reintentarlo en cada arranque.
			System.err.println("[inventario] Restauración pendiente descartada (archivo inválido): " + e.getMessage());
			borrarSilenciosamente(staging);
		} catch (IOException e) {
			// Error de I/O (p. ej. lock): se conserva el staging para el próximo arranque.
			System.err.println("[inventario] No se pudo aplicar la restauración pendiente: " + e.getMessage());
		} finally {
			if (aplicada) {
				borrarSilenciosamente(staging);
			}
		}
	}

	private static void borrarSilenciosamente(Path archivo) {
		try {
			Files.deleteIfExists(archivo);
		} catch (IOException ignorada) {
			// Nada que hacer: quedará para el próximo intento.
		}
	}
}
