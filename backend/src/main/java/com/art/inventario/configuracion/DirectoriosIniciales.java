package com.art.inventario.configuracion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DirectoriosIniciales implements ApplicationRunner {

	private final String dbPath;
	private final String uploadsDir;
	private final String backupsDir;
	private final String logsDir;

	public DirectoriosIniciales(@Value("${INVENTARIO_DB:inventario.db}") String dbPath,
			@Value("${app.uploads.dir:uploads}") String uploadsDir,
			@Value("${app.backups.dir:backups}") String backupsDir,
			@Value("${app.logs.dir:logs}") String logsDir) {
		this.dbPath = dbPath;
		this.uploadsDir = uploadsDir;
		this.backupsDir = backupsDir;
		this.logsDir = logsDir;
	}

	@Override
	public void run(ApplicationArguments args) {
		crear(Paths.get(dbPath).toAbsolutePath().normalize().getParent());
		crear(Paths.get(uploadsDir).toAbsolutePath().normalize());
		crear(Paths.get(backupsDir).toAbsolutePath().normalize());
		crear(Paths.get(logsDir).toAbsolutePath().normalize());
	}

	private void crear(Path dir) {
		if (dir == null) {
			return;
		}
		try {
			Files.createDirectories(dir);
		} catch (Exception e) {
			System.err.println("ERROR_DIR: " + e.getMessage());
		}
	}
}