package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.art.inventario.aplicacion.CalculadoraPrestaciones;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.TipoContrato;

class CalculadoraPrestacionesTest {

	private CalculadoraPrestaciones calculadora;
	private ParametroLegal params;

	@BeforeEach
	void setUp() {
		calculadora = new CalculadoraPrestaciones();
		params = new ParametroLegal();
		params.setAnio(2026);
		params.setSmlmv(new BigDecimal("1520000"));
		params.setAuxilioTransporte(new BigDecimal("200000"));
		params.setPorcentajeSalud(new BigDecimal("8.5"));
		params.setPorcentajePension(new BigDecimal("12"));
		params.setPorcentajeArl(new BigDecimal("0.522"));
		params.setPorcentajeCaja(new BigDecimal("4"));
		params.setPorcentajeSena(new BigDecimal("2"));
		params.setPorcentajeIcbf(new BigDecimal("3"));
	}

	private TipoContrato tipo(String nombre) {
		TipoContrato t = new TipoContrato();
		t.setNombre(nombre);
		return t;
	}

	private ContratoPrestacionCalculada linea(List<ContratoPrestacionCalculada> lineas, String concepto) {
		return lineas.stream().filter(l -> l.getConcepto().equals(concepto)).findFirst().orElseThrow();
	}

	private Predicate<ContratoPrestacionCalculada> tieneConcepto(String concepto) {
		return l -> l.getConcepto().equals(concepto);
	}

	@Test
	void laboralGeneraConjuntoCompleto() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("TERMINO_FIJO"),
				new BigDecimal("1000000"), null, params);

		assertTrue(lineas.stream().anyMatch(tieneConcepto("Prima de Servicios")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Cesantías")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Intereses sobre Cesantías")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Vacaciones")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Dotación")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Auxilio de Transporte")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Salud")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Pensión")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("ARL")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("Caja de Compensación")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("ICBF")));
		assertTrue(lineas.stream().anyMatch(tieneConcepto("SENA")));
		assertEquals(12, lineas.size());
	}

	@Test
	void laboralValoresPorcentuales() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("OBRA_LABOR"),
				new BigDecimal("1000000"), null, params);

		ContratoPrestacionCalculada salud = linea(lineas, "Salud");
		assertEquals("LABORAL", salud.getTipo());
		assertEquals(CalculadoraPrestaciones.QUIEN_PAGA_EMPLEADOR, salud.getQuienPaga());
		assertEquals(new BigDecimal("1000000.00"), salud.getBase());
		assertEquals(new BigDecimal("8.5"), salud.getPorcentaje());
		assertEquals(new BigDecimal("85000.00"), salud.getValorMensual());
		assertEquals(new BigDecimal("1020000.00"), salud.getValorAnual());

		ContratoPrestacionCalculada pension = linea(lineas, "Pensión");
		assertEquals(new BigDecimal("12"), pension.getPorcentaje());
		assertEquals(new BigDecimal("120000.00"), pension.getValorMensual());

		ContratoPrestacionCalculada arl = linea(lineas, "ARL");
		assertEquals(new BigDecimal("0.522"), arl.getPorcentaje());
		assertEquals(new BigDecimal("5220.00"), arl.getValorMensual());

		ContratoPrestacionCalculada prima = linea(lineas, "Prima de Servicios");
		assertEquals(new BigDecimal("83300.00"), prima.getValorMensual());

		ContratoPrestacionCalculada cesantias = linea(lineas, "Cesantías");
		assertEquals(new BigDecimal("83300.00"), cesantias.getValorMensual());

		ContratoPrestacionCalculada intereses = linea(lineas, "Intereses sobre Cesantías");
		assertEquals(new BigDecimal("10000.00"), intereses.getValorMensual());

		ContratoPrestacionCalculada vacaciones = linea(lineas, "Vacaciones");
		assertEquals(new BigDecimal("41700.00"), vacaciones.getValorMensual());
	}

	@Test
	void laboralValoresFijos() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("TERMINO_INDEFINIDO"),
				new BigDecimal("1000000"), null, params);

		ContratoPrestacionCalculada dotacion = linea(lineas, "Dotación");
		assertNull(dotacion.getPorcentaje());
		assertEquals(new BigDecimal("90000.00"), dotacion.getValorMensual());
		assertEquals(new BigDecimal("1080000.00"), dotacion.getValorAnual());

		ContratoPrestacionCalculada auxilio = linea(lineas, "Auxilio de Transporte");
		assertEquals(new BigDecimal("200000.00"), auxilio.getValorMensual());
	}

	@Test
	void prestacionServiciosNoGeneraLineasCalculadas() {
		// OPS generates zero calculated lines — seguridad social is the
		// contractor's own responsibility, outside the employer's calculation.
		assertTrue(calculadora.aplicaCalculo(tipo("PRESTACION_SERVICIOS")));

		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("PRESTACION_SERVICIOS"),
				new BigDecimal("2000000"), null, params);

		assertTrue(lineas.isEmpty(),
				"PRESTACION_SERVICIOS must produce zero calculated lines");
	}

	@Test
	void prestacionServiciosAplicaCalculoTrue() {
		assertTrue(calculadora.aplicaCalculo(tipo("PRESTACION_SERVICIOS")),
				"aplicaCalculo must remain true so recalculation completes without error");
	}

	@Test
	void aprendizajeFaseLectivaSoloSalud() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("APRENDIZAJE"),
				new BigDecimal("500000"), Contrato.FASE_LECTIVA, params);

		assertEquals(1, lineas.size());
		ContratoPrestacionCalculada salud = lineas.get(0);
		assertEquals("Salud (Aprendizaje)", salud.getConcepto());
		assertEquals(CalculadoraPrestaciones.QUIEN_PAGA_EMPLEADOR, salud.getQuienPaga());
		// Base = auxilio de sostenimiento (la remuneración mensual del aprendiz)
		assertEquals(new BigDecimal("500000.00"), salud.getBase());
		assertEquals(new BigDecimal("42500.00"), salud.getValorMensual());
		assertTrue(lineas.stream().noneMatch(tieneConcepto("ARL (Aprendizaje)")));
	}

	@Test
	void aprendizajeFasePracticaIncluyeArl() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("APRENDIZAJE"),
				new BigDecimal("500000"), Contrato.FASE_PRACTICA, params);

		assertEquals(2, lineas.size());
		ContratoPrestacionCalculada arl = linea(lineas, "ARL (Aprendizaje)");
		assertEquals(CalculadoraPrestaciones.QUIEN_PAGA_EMPLEADOR, arl.getQuienPaga());
		assertEquals(new BigDecimal("500000.00"), arl.getBase());
		assertEquals(new BigDecimal("2610.00"), arl.getValorMensual());
	}

	@Test
	void practicasLaboralesSigueReglaAprendizaje() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo("PRACTICAS_LABORALES"),
				new BigDecimal("500000"), Contrato.FASE_PRACTICA, params);
		assertEquals(2, lineas.size());
	}

	@Test
	void sinTipoNoCalcula() {
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(null,
				new BigDecimal("1000000"), null, params);
		assertTrue(lineas.isEmpty());
		assertFalse(calculadora.aplicaCalculo(null));
	}
}
