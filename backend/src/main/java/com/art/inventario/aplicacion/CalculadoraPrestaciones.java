package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.dominio.TipoContrato;

/**
 * Pure calculation engine for contract benefits (prestaciones). It has NO Spring
 * dependencies so it can be unit-tested in isolation.
 *
 * <p>It derives a category from the contract type and applies the per-category
 * legal rules while taking the percentages (and the auxilio de transporte value)
 * from a {@link ParametroLegal} snapshot. The catalog seed is a superset: the
 * actual lines produced for a contract are decided here, per category.
 *
 * <ul>
 * <li><b>LABORAL</b> (término indefinido/fijo, obra-labor): full prestaciones
 * sociales + seguridad social + parafiscales, all at the employer's expense.</li>
 * <li><b>PRESTACION_SERVICIOS</b>: only seguridad social, 100% paid by the
 * contractor, over a minimum base of 40% of the monthly value.</li>
 * <li><b>APRENDIZAJE</b>: health always (100% employer) + ARL only during the
 * practical phase; no prestaciones sociales, no pensión, no parafiscales, base =
 * the apprentice stipend (the contract's monthly remuneration).</li>
 * </ul>
 */
public final class CalculadoraPrestaciones {

	public static final String QUIEN_PAGA_EMPLEADOR = "EMPLEADOR";
	public static final String QUIEN_PAGA_CONTRATISTA = "CONTRATISTA";

	// Fixed percentages for prestaciones sociales (LABORAL), in % terms.
	private static final BigDecimal POR_PRIMA = new BigDecimal("8.33");
	private static final BigDecimal POR_CESANTIAS = new BigDecimal("8.33");
	private static final BigDecimal POR_INTERESES_CESANTIAS = new BigDecimal("1");
	private static final BigDecimal POR_VACACIONES = new BigDecimal("4.17");

	// Nominal monthly value used for the Dotación benefit (benefit-in-kind).
	static final BigDecimal DOTACION_MENSUAL = new BigDecimal("90000");

	private static final int ESCALA = 2;

	/**
	 * Computes the list of calculated-benefit snapshot lines for a contract.
	 *
	 * @param tipoContrato       the contract type (used only to derive category)
	 * @param remuneracionMensual the contract's monthly remuneration
	 * @param faseAprendizaje    the learning-contract phase (LECTIVA/PRACTICA)
	 * @param params             the legal parameters snapshot
	 * @return the computed lines (never null)
	 */
	public List<ContratoPrestacionCalculada> calcular(TipoContrato tipoContrato,
			BigDecimal remuneracionMensual, String faseAprendizaje, ParametroLegal params) {
		List<ContratoPrestacionCalculada> lineas = new ArrayList<>();
		if (tipoContrato == null || params == null || remuneracionMensual == null) {
			return lineas;
		}
		Categoria categoria = categoriaDe(tipoContrato);
		switch (categoria) {
		case LABORAL -> lineas.addAll(laboral(remuneracionMensual, params));
		case PRESTACION_SERVICIOS -> lineas.addAll(prestacionServicios(remuneracionMensual, params));
		case APRENDIZAJE -> lineas.addAll(aprendizaje(remuneracionMensual, faseAprendizaje, params));
		}
		return lineas;
	}

	public boolean aplicaCalculo(TipoContrato tipoContrato) {
		return tipoContrato != null && categoriaDe(tipoContrato) != Categoria.DESCONOCIDO;
	}

	private List<ContratoPrestacionCalculada> laboral(BigDecimal remuneracion, ParametroLegal p) {
		List<ContratoPrestacionCalculada> lineas = new ArrayList<>();
		// Prestaciones sociales (empleador)
		lineas.add(porcentual("Prima de Servicios", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, POR_PRIMA, true));
		lineas.add(porcentual("Cesantías", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, POR_CESANTIAS, true));
		lineas.add(porcentual("Intereses sobre Cesantías", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, POR_INTERESES_CESANTIAS, true));
		lineas.add(porcentual("Vacaciones", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, POR_VACACIONES, true));
		lineas.add(valorFijo("Dotación", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				DOTACION_MENSUAL, true));
		lineas.add(valorFijo("Auxilio de Transporte", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				valorNoNulo(p.getAuxilioTransporte()), true));
		// Seguridad social (empleador)
		lineas.add(porcentual("Salud", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajeSalud()), true));
		lineas.add(porcentual("Pensión", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajePension()), true));
		lineas.add(porcentual("ARL", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajeArl()), true));
		// Parafiscales (empleador)
		lineas.add(porcentual("Caja de Compensación", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajeCaja()), true));
		lineas.add(porcentual("ICBF", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajeIcbf()), true));
		lineas.add(porcentual("SENA", Prestacion.TIPO_LABORAL, QUIEN_PAGA_EMPLEADOR,
				remuneracion, porc(p.getPorcentajeSena()), true));
		return lineas;
	}

	private List<ContratoPrestacionCalculada> prestacionServicios(BigDecimal remuneracion, ParametroLegal p) {
		// Minimum base: 40% of the monthly value.
		BigDecimal base = remuneracion.multiply(new BigDecimal("0.40"));
		List<ContratoPrestacionCalculada> lineas = new ArrayList<>();
		lineas.add(porcentualSobre("Salud (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS,
				QUIEN_PAGA_CONTRATISTA, base, porc(p.getPorcentajeSalud()), true));
		lineas.add(porcentualSobre("Pensión (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS,
				QUIEN_PAGA_CONTRATISTA, base, porc(p.getPorcentajePension()), true));
		lineas.add(porcentualSobre("ARL (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS,
				QUIEN_PAGA_CONTRATISTA, base, porc(p.getPorcentajeArl()), true));
		return lineas;
	}

	private List<ContratoPrestacionCalculada> aprendizaje(BigDecimal remuneracion, String fase, ParametroLegal p) {
		// Base = the apprentice stipend (auxilio de sostenimiento), taken from the
		// contract's monthly remuneration.
		BigDecimal auxilio = remuneracion;
		List<ContratoPrestacionCalculada> lineas = new ArrayList<>();
		lineas.add(porcentualSobre("Salud (Aprendizaje)", Prestacion.TIPO_APRENDIZAJE,
				QUIEN_PAGA_EMPLEADOR, auxilio, porc(p.getPorcentajeSalud()), true));
		boolean practica = Contrato.FASE_PRACTICA.equals(fase);
		if (practica) {
			lineas.add(porcentualSobre("ARL (Aprendizaje)", Prestacion.TIPO_APRENDIZAJE,
					QUIEN_PAGA_EMPLEADOR, auxilio, porc(p.getPorcentajeArl()), true));
		}
		return lineas;
	}

	private ContratoPrestacionCalculada porcentual(String concepto, String tipo, String quienPaga,
			BigDecimal base, BigDecimal porcentaje, boolean obligatoria) {
		return construir(concepto, tipo, quienPaga, base, porcentaje, obligatoria);
	}

	private ContratoPrestacionCalculada porcentualSobre(String concepto, String tipo, String quienPaga,
			BigDecimal base, BigDecimal porcentaje, boolean obligatoria) {
		return construir(concepto, tipo, quienPaga, base, porcentaje, obligatoria);
	}

	private ContratoPrestacionCalculada valorFijo(String concepto, String tipo, String quienPaga,
			BigDecimal valor, boolean obligatoria) {
		ContratoPrestacionCalculada c = new ContratoPrestacionCalculada();
		c.setConcepto(concepto);
		c.setTipo(tipo);
		c.setQuienPaga(quienPaga);
		c.setValorMensual(redondear(valor));
		c.setValorAnual(redondear(valor.multiply(BigDecimal.valueOf(12))));
		c.setObligatoria(obligatoria);
		return c;
	}

	private ContratoPrestacionCalculada construir(String concepto, String tipo, String quienPaga,
			BigDecimal base, BigDecimal porcentaje, boolean obligatoria) {
		ContratoPrestacionCalculada c = new ContratoPrestacionCalculada();
		c.setConcepto(concepto);
		c.setTipo(tipo);
		c.setQuienPaga(quienPaga);
		c.setBase(redondear(base));
		c.setPorcentaje(porcentaje);
		BigDecimal mensual = base.multiply(porcentaje).divide(BigDecimal.valueOf(100), ESCALA, RoundingMode.HALF_UP);
		c.setValorMensual(mensual);
		c.setValorAnual(mensual.multiply(BigDecimal.valueOf(12)));
		c.setObligatoria(obligatoria);
		return c;
	}

	private enum Categoria {
		LABORAL, PRESTACION_SERVICIOS, APRENDIZAJE, DESCONOCIDO
	}

	private Categoria categoriaDe(TipoContrato tipo) {
		String nombre = tipo.getNombre();
		if (nombre == null) {
			return Categoria.DESCONOCIDO;
		}
		return switch (nombre) {
		case "TERMINO_INDEFINIDO", "TERMINO_FIJO", "OBRA_LABOR" -> Categoria.LABORAL;
		case "PRESTACION_SERVICIOS" -> Categoria.PRESTACION_SERVICIOS;
		case "APRENDIZAJE", "PRACTICAS_LABORALES" -> Categoria.APRENDIZAJE;
		default -> Categoria.DESCONOCIDO;
		};
	}

	private static BigDecimal porc(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private static BigDecimal valorNoNulo(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private static BigDecimal redondear(BigDecimal valor) {
		return valor.setScale(ESCALA, RoundingMode.HALF_UP);
	}
}
