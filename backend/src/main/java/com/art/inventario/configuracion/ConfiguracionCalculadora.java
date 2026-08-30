package com.art.inventario.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.art.inventario.aplicacion.CalculadoraPrestaciones;

/**
 * Registers the pure calculation engine as a bean. The calculator class itself
 * carries no Spring annotations so it stays a plain, unit-testable POJO; its
 * only dependency is this wiring point.
 */
@Configuration
public class ConfiguracionCalculadora {

	@Bean
	CalculadoraPrestaciones calculadoraPrestaciones() {
		return new CalculadoraPrestaciones();
	}
}
