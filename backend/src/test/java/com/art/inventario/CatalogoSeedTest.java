package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.art.inventario.configuracion.InicializadorCatalogosPrestaciones;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;
import com.art.inventario.puerto.salida.PrestacionPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPrestacionPersistencia;

@SpringBootTest
class CatalogoSeedTest {

	@Autowired
	private InicializadorCatalogosPrestaciones inicializador;

	@Autowired
	private TipoContratoPersistencia tipos;

	@Autowired
	private PrestacionPersistencia prestaciones;

	@Autowired
	private TipoContratoPrestacionPersistencia matriz;

	@Autowired
	private ParametroLegalPersistencia parametros;

	@Test
	void seedPobladaConConteosEsperados() {
		inicializador.run(null);

		assertEquals(6, tipos.listar().size(), "Deben existir 6 tipos de contrato");
		assertEquals(17, prestaciones.listar().size(), "Deben existir 17 prestaciones");
		assertTrue(parametros.existeAnio(2026), "Debe existir el parámetro legal 2026");
	}

	@Test
	void seedEsIdempotenteAlRepetirse() {
		inicializador.run(null);

		int tiposAntes = tipos.listar().size();
		int prestacionesAntes = prestaciones.listar().size();
		int matrizAntes = matriz.listar().size();

		// Re-run the seed: it must insert nothing.
		inicializador.run(null);

		assertEquals(tiposAntes, tipos.listar().size(), "Re-run no debe agregar tipos");
		assertEquals(prestacionesAntes, prestaciones.listar().size(), "Re-run no debe agregar prestaciones");
		assertEquals(matrizAntes, matriz.listar().size(), "Re-run no debe agregar relaciones de la matriz");
	}

	@Test
	void matrizLaboralIncluyePrestacionesSociales() {
		inicializador.run(null);

		TipoContrato terminoFijo = tipos.porNombre("TERMINO_FIJO").orElseThrow();
		List<Prestacion> prestacionesTipo = prestaciones.listarPorTipoContrato(terminoFijo.getId());

		assertTrue(prestacionesTipo.stream().anyMatch(p -> p.getNombre().equals("Prima de Servicios")));
		assertTrue(prestacionesTipo.stream().anyMatch(p -> p.getNombre().equals("Cesantías")));
		assertTrue(prestacionesTipo.stream().anyMatch(p -> p.getNombre().equals("Vacaciones")));
		assertTrue(prestacionesTipo.stream().anyMatch(p -> p.getNombre().equals("Auxilio de Transporte")));
	}
}
