package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.art.inventario.aplicacion.HerramientaAplicacion;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.puerto.salida.AjusteConsultaSalida;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.DevolucionPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.MovimientoHerramientaPersistencia;

@ExtendWith(MockitoExtension.class)
class HerramientaAplicacionTest {

	@Mock
	private HerramientaPersistencia persistencia;
	@Mock
	private AsignacionHerramientaPersistencia asignacionPersistencia;
	@Mock
	private MovimientoHerramientaPersistencia movimientoPersistencia;
	@Mock
	private CambiosNotificador notificador;
	@Mock
	private AjusteConsultaSalida ajustes;
	@Mock
	private DevolucionPersistencia devoluciones;

	private HerramientaAplicacion aplicacion;

	@BeforeEach
	void setUp() {
		aplicacion = new HerramientaAplicacion(persistencia, asignacionPersistencia, movimientoPersistencia,
				notificador, ajustes, devoluciones);
	}

	@Test
	void crearConCodigoFijaCodigoEscaneado() {
		when(persistencia.existePorCodigo("H999")).thenReturn(false);
		Herramienta nueva = new Herramienta();
		nueva.setNombre("Express");
		nueva.setCodigo("H999");
		nueva.setCantidadTotal(1);

		when(persistencia.guardar(any(Herramienta.class))).thenAnswer(inv -> {
			Herramienta h = inv.getArgument(0);
			h.setId(500L);
			return h;
		});

		Herramienta creada = aplicacion.crearConCodigo(nueva);
		assertEquals("H999", creada.getCodigo());
		assertEquals(500L, creada.getId());
	}

	@Test
	void crearConCodigoColisionRechaza() {
		when(persistencia.existePorCodigo("H42")).thenReturn(true);
		Herramienta h = new Herramienta();
		h.setNombre("Dup");
		h.setCodigo("H42");
		h.setCantidadTotal(1);

		assertThrows(ConflictoExcepcion.class, () -> aplicacion.crearConCodigo(h));
		verify(persistencia, never()).guardar(any(Herramienta.class));
	}
}