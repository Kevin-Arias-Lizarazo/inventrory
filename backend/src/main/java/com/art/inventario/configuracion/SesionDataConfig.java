package com.art.inventario.configuracion;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class SesionDataConfig {

	@Bean
	@Primary
	DataSource inventarioDataSource(@Value("${spring.datasource.url}") String url,
			@Value("${spring.datasource.driver-class-name}") String driver) {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		return ds;
	}

	@Bean
	DataSource sesionDataSource(@Value("${app.sesiones.dir:sesiones}") String sesionesDir) {
		Path dir = Paths.get(sesionesDir).toAbsolutePath().normalize();
		try {
			java.nio.file.Files.createDirectories(dir);
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo crear el directorio de sesiones: " + sesionesDir, e);
		}
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:" + dir.resolve("sesiones.db"));
		return ds;
	}

	@Bean
	JdbcTemplate sesionJdbcTemplate(DataSource sesionDataSource) {
		return new JdbcTemplate(sesionDataSource);
	}
}