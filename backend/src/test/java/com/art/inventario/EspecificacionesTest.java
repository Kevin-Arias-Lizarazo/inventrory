package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;

class EspecificacionesTest {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"nombre", new CampoFiltro("nombre", TipoFiltro.TEXTO_CONTIENE),
			"marca", new CampoFiltro("marca", TipoFiltro.TEXTO_EXACTO),
			"proveedorId", new CampoFiltro("proveedor.id", TipoFiltro.ID),
			"stock", new CampoFiltro("stock", TipoFiltro.NUMERO),
			"activo", new CampoFiltro("activo", TipoFiltro.BOOLEANO),
			"fecha", new CampoFiltro("fecha", TipoFiltro.FECHA));

	private static final Set<String> ORDENABLES = Set.of("id", "nombre", "marca");

	@Test
	void ordenarRechazaCampoInvalido() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("orden", "inexistente"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.ordenar(c, ORDENABLES, "id"));
	}

	@Test
	void ordenarRechazaDireccionInvalida() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("orden", "nombre", "dir", "up"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.ordenar(c, ORDENABLES, "id"));
	}

	@Test
	void ordenarUsaCampoPorDefectoCuandoNoSeIndica() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of());
		Sort sort = Especificaciones.ordenar(c, ORDENABLES, "nombre");
		assertEquals("nombre", sort.getOrderFor("nombre").getProperty());
		assertEquals(Sort.Direction.ASC, sort.getOrderFor("nombre").getDirection());
	}

	@Test
	void ordenarAnadeTieBreakPorId() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("orden", "nombre", "dir", "desc"));
		Sort sort = Especificaciones.ordenar(c, ORDENABLES, "id");
		assertEquals(Sort.Direction.DESC, sort.getOrderFor("id").getDirection());
		assertEquals(Sort.Direction.DESC, sort.getOrderFor("nombre").getDirection());
		assertTrue(sort.isSorted());
		assertEquals(2, sort.toList().size());
	}

	@Test
	void validarRechazaCampoDeFiltroDesconocido() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("campoInexistente", "x"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.validar(c, CAMPOS, List.of("nombre")));
	}

	@Test
	void validarRechazaFiltroDeIdNoNumerico() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("proveedorId", "abc"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.validar(c, CAMPOS, List.of("nombre")));
	}

	@Test
	void validarRechazaFiltroBooleanoInvalido() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("activo", "talvez"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.validar(c, CAMPOS, List.of("nombre")));
	}

	@Test
	void validarRechazaQCuandoNoHayCamposBuscables() {
		ConsultaPaginada c = ConsultaPaginada.desdeParams(Map.of("q", "algo"));
		assertThrows(DatosInvalidosExcepcion.class,
				() -> Especificaciones.validar(c, CAMPOS, List.of()));
	}
}
