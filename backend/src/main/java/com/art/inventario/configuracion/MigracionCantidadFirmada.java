package com.art.inventario.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MigracionCantidadFirmada implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(MigracionCantidadFirmada.class);

	private final JdbcTemplate jdbc;

	public MigracionCantidadFirmada(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void run(ApplicationArguments args) {
		int actualizadas = jdbc.update(
				"UPDATE asignaciones_herramientas "
						+ "SET cantidad = CASE WHEN devuelta = 1 THEN -1 ELSE 1 END "
						+ "WHERE cantidad IS NULL");
		if (actualizadas > 0) {
			log.info("MigracionCantidadFirmada: backfilled {} rows with cantidad", actualizadas);
		}
	}
}
