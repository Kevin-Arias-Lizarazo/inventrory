package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.puerto.entrada.ContratoCasoDeUso;
import com.art.inventario.puerto.salida.ContratoPrestacionCalculadaPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;

@SpringBootTest
class SnapshotInmutabilidadTest {

	@Autowired
	private ContratoCasoDeUso contratos;

	@Autowired
	private EmpleadoPersistencia empleados;

	@Autowired
	private TipoContratoPersistencia tipos;

	@Autowired
	private ParametroLegalPersistencia parametros;

	@Autowired
	private ContratoPrestacionCalculadaPersistencia calculadas;

	private Long contratoId;

	@BeforeEach
	void crearContratoLaboral() {
		resetearParametro();
		Empleado e = new Empleado();
		e.setNombre("Snapshot Test");
		e.setDocumento("SNAP-001");
		e.setCodigo("SNAP");
		Empleado guardado = empleados.guardar(e);

		TipoContrato tipo = tipos.porNombre("TERMINO_FIJO").orElseThrow();
		Contrato c = new Contrato();
		c.setEmpleado(guardado);
		c.setFechaInicio("2026-01-01");
		c.setEstado(Contrato.ACTIVO);
		c.setTipoContrato(tipo);
		c.setRemuneracionMensual(new BigDecimal("1000000"));
		Contrato creado = contratos.crear(c);
		contratoId = creado.getId();
	}

	/**
	 * Restores the legal parameter to the seeded baseline before each test so a
	 * previous test's mutation (e.g. changing the health percentage) never leaks
	 * into another test's auto-recalculation.
	 */
	private void resetearParametro() {
		ParametroLegal p = parametros.porAnio(2026).orElseThrow();
		p.setSmlmv(new BigDecimal("1520000"));
		p.setPorcentajeSalud(new BigDecimal("8.5"));
		parametros.guardar(p);
	}

	private ContratoPrestacionCalculada salud() {
		return calculadas.listarPorContrato(contratoId).stream()
				.filter(l -> l.getConcepto().equals("Salud"))
				.findFirst().orElseThrow();
	}

	private List<ContratoPrestacionCalculada> lineas() {
		return calculadas.listarPorContrato(contratoId);
	}

	@Test
	void autoRecalculoAlCrearGeneraSnapshot() {
		assertTrue(!lineas().isEmpty(), "El snapshot debe generarse al crear con tipo de contrato");
		assertEquals(0, new BigDecimal("85000").compareTo(salud().getValorMensual()));
	}

	@Test
	void cambiarParametroNoAlteraSnapshotExistente() {
		// 8.5% de 1.000.000 = 85.000 (seeded amount before any param change)
		BigDecimal antes = salud().getValorMensual();

		// Change both SMLMV and the health percentage to verify the snapshot is a
		// literal copy with no live link to the catalogs.
		ParametroLegal p = parametros.porAnio(2026).orElseThrow();
		p.setSmlmv(new BigDecimal("2000000"));
		p.setPorcentajeSalud(new BigDecimal("10"));
		parametros.guardar(p);

		BigDecimal despues = salud().getValorMensual();
		assertEquals(antes, despues, "El snapshot no debe cambiar al editar el parámetro legal");
	}

	@Test
	void recalculoUsaNuevosValores() {
		// 8.5% de 1.000.000 = 85.000
		assertEquals(0, new BigDecimal("85000").compareTo(salud().getValorMensual()));

		ParametroLegal p = parametros.porAnio(2026).orElseThrow();
		p.setSmlmv(new BigDecimal("2000000"));
		p.setPorcentajeSalud(new BigDecimal("10"));
		parametros.guardar(p);

		// The old snapshot is untouched.
		assertEquals(0, new BigDecimal("85000").compareTo(salud().getValorMensual()));
		assertTrue(lineas().stream().anyMatch(l -> new BigDecimal("85000").compareTo(l.getValorMensual()) == 0),
				"La fila vieja conserva su valor literal");

		// Recalculate: the new snapshot rows use the new 10% value.
		contratos.calcularPrestaciones(contratoId);
		assertEquals(0, new BigDecimal("100000").compareTo(salud().getValorMensual()));
	}

	@Test
	void quitarTipoDeContratoLimpiaSnapshot() {
		assertTrue(!lineas().isEmpty(), "El snapshot debe existir antes de quitar el tipo");

		Contrato actual = contratos.obtener(contratoId);
		actual.setTipoContrato(null);
		contratos.actualizar(contratoId, actual);

		assertTrue(lineas().isEmpty(),
				"Quitar el tipo de contrato debe borrar las líneas de snapshot previas");
	}
}
