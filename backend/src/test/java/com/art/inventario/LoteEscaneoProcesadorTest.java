package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.art.inventario.aplicacion.LoteEscaneoProcesador;
import com.art.inventario.aplicacion.dto.ItemEscaneoLote;
import com.art.inventario.aplicacion.dto.LoteEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoLoteEscaneo;
import com.art.inventario.aplicacion.dto.TipoLoteEscaneo;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.puerto.salida.AsignacionConsumiblePersistencia;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@ExtendWith(MockitoExtension.class)
class LoteEscaneoProcesadorTest {

	@Mock
	private EmpleadoPersistencia empleadoPersistencia;
	@Mock
	private ProyectoPersistencia proyectoPersistencia;
	@Mock
	private HerramientaPersistencia herramientaPersistencia;
	@Mock
	private ConsumiblePersistencia consumiblePersistencia;
	@Mock
	private AsignacionHerramientaPersistencia asignacionHerramientaPersistencia;
	@Mock
	private AsignacionConsumiblePersistencia asignacionConsumiblePersistencia;
	@Mock
	private ContratoPersistencia contratoPersistencia;
	@Mock
	private CambiosNotificador notificador;

	private LoteEscaneoProcesador procesador;

	private Empleado empleado;
	private Proyecto proyecto;
	private Herramienta herramienta;
	private Consumible consumible;

	@BeforeEach
	void setUp() {
		procesador = new LoteEscaneoProcesador(empleadoPersistencia, proyectoPersistencia, herramientaPersistencia,
				consumiblePersistencia, asignacionHerramientaPersistencia, asignacionConsumiblePersistencia,
				contratoPersistencia, notificador);

		empleado = new Empleado();
		empleado.setId(7L);
		empleado.setCodigo("E7");

		proyecto = new Proyecto();
		proyecto.setId(3L);
		proyecto.setCodigo("P3");
		proyecto.setEstado(Proyecto.ACTIVO);

		herramienta = new Herramienta();
		herramienta.setId(42L);
		herramienta.setCodigo("H42");
		herramienta.setNombre("Pinza");
		herramienta.setCantidadTotal(5);
		herramienta.setCantidadDanada(0);
		herramienta.setCantidadPerdida(0);

		consumible = new Consumible();
		consumible.setId(9L);
		consumible.setCodigo("C9");
		consumible.setNombre("Pintura");
		consumible.setStock(new BigDecimal("10.0"));
	}

	private LoteEscaneo lote(TipoLoteEscaneo tipo, String destino, ItemEscaneoLote... items) {
		LoteEscaneo l = new LoteEscaneo();
		l.setTipo(tipo);
		l.setDestinoCodigo(destino);
		l.setItems(List.of(items));
		return l;
	}

	private ItemEscaneoLote item(String codigo, String cantidad) {
		ItemEscaneoLote i = new ItemEscaneoLote();
		i.setCodigo(codigo);
		i.setCantidad(new BigDecimal(cantidad));
		return i;
	}

	private void mocksAsignacionFeliz() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(contratoPersistencia.empleadoContratado(7L)).thenReturn(true);
		when(herramientaPersistencia.existePorCodigo("H42")).thenReturn(true);
		when(herramientaPersistencia.obtenerPorCodigo("H42")).thenReturn(herramienta);
		when(asignacionHerramientaPersistencia.contarAsignacionesActivas(42L, -1L)).thenReturn(0L);
	}

	@Test
	void asignacionFelizCreaRegistros() {
		mocksAsignacionFeliz();
		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E7",
				item("H42", "2"), item("H42", "1")));
		assertTrue(r.isOk());
		// Duplicados se fusionan en una única asignación con cantidad sumada (2+1=3)
		assertEquals(1, r.getRegistrosCreados());
		verify(asignacionHerramientaPersistencia).guardar(any(AsignacionHerramienta.class));
		verify(notificador).publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
	}

	@Test
	void consumiblesFelizCreaRegistro() {
		when(proyectoPersistencia.existePorCodigo("P3")).thenReturn(true);
		when(proyectoPersistencia.obtenerPorCodigo("P3")).thenReturn(proyecto);
		when(consumiblePersistencia.existePorCodigo("C9")).thenReturn(true);
		when(consumiblePersistencia.obtenerPorCodigo("C9")).thenReturn(consumible);

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "P3",
				item("C9", "4")));
		assertTrue(r.isOk());
		assertEquals(1, r.getRegistrosCreados());
		verify(asignacionConsumiblePersistencia).guardar(any(AsignacionConsumible.class));
	}

	@Test
	void tipoCruzadoRechazaLote() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(contratoPersistencia.empleadoContratado(7L)).thenReturn(true);
		// E# destino con item C# -> tipo cruzado (detectado antes del existence check)
		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E7",
				item("C9", "1")));
		assertFalse(r.isOk());
		assertEquals(0, r.getRegistrosCreados());
		assertFalse(r.getErrores().isEmpty());
		assertEquals("TIPO_CRUZADO", r.getErrores().get(0).getMotivo());
	}

	@Test
	void contratoInactivoRechazaLote() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(contratoPersistencia.empleadoContratado(7L)).thenReturn(false);

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E7",
				item("H42", "1")));
		assertFalse(r.isOk());
		assertEquals("CONTRATO_INACTIVO", r.getErrores().get(0).getMotivo());
	}

	@Test
	void sinDisponibilidadRechazaLote() {
		mocksAsignacionFeliz();
		when(asignacionHerramientaPersistencia.contarAsignacionesActivas(42L, -1L)).thenReturn(5L);
		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E7",
				item("H42", "1")));
		assertFalse(r.isOk());
		assertEquals("SIN_DISPONIBILIDAD", r.getErrores().get(0).getMotivo());
	}

	@Test
	void itemNoRegistradoBloqueaLote() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(contratoPersistencia.empleadoContratado(7L)).thenReturn(true);
		when(herramientaPersistencia.existePorCodigo("H999")).thenReturn(false);

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E7",
				item("H999", "1")));
		assertFalse(r.isOk());
		assertEquals(0, r.getRegistrosCreados());
		assertFalse(r.getPendientes().isEmpty());
		assertEquals("ITEM_NO_REGISTRADO", r.getPendientes().get(0).getMotivo());
	}

	@Test
	void destinoNoRegistradoRechazaLote() {
		when(empleadoPersistencia.existePorCodigo("E999")).thenReturn(false);
		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.ASIGNACION, "E999",
				item("H42", "1")));
		assertFalse(r.isOk());
		assertFalse(r.getErrores().isEmpty());
		assertEquals("DESTINO_NO_REGISTRADO", r.getErrores().get(0).getMotivo());
	}

	@Test
	void loteVacioRechaza() {
		LoteEscaneo l = new LoteEscaneo();
		l.setTipo(TipoLoteEscaneo.ASIGNACION);
		l.setDestinoCodigo("E7");
		l.setItems(List.of());
		ResultadoLoteEscaneo r = procesador.procesar(l);
		assertFalse(r.isOk());
	}

	@Test
	void devolucionFIFODevuelve() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(herramientaPersistencia.existePorCodigo("H42")).thenReturn(true);
		when(herramientaPersistencia.obtenerPorCodigo("H42")).thenReturn(herramienta);

		AsignacionHerramienta abierta1 = new AsignacionHerramienta();
		abierta1.setId(1L);
		abierta1.setCantidad(2);
		AsignacionHerramienta abierta2 = new AsignacionHerramienta();
		abierta2.setId(2L);
		abierta2.setCantidad(1);
		when(asignacionHerramientaPersistencia.activasMasAntiguas(7L, 42L, Integer.MAX_VALUE))
				.thenReturn(List.of(abierta1, abierta2));

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.DEVOLUCION, "E7",
				item("H42", "2")));
		assertTrue(r.isOk());
		verify(asignacionHerramientaPersistencia).guardar(any(AsignacionHerramienta.class));
	}

	@Test
	void devolucionExcesoRechaza() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(herramientaPersistencia.existePorCodigo("H42")).thenReturn(true);
		when(herramientaPersistencia.obtenerPorCodigo("H42")).thenReturn(herramienta);
		when(asignacionHerramientaPersistencia.activasMasAntiguas(7L, 42L, Integer.MAX_VALUE))
				.thenReturn(List.of());

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.DEVOLUCION, "E7",
				item("H42", "5")));
		assertFalse(r.isOk());
		assertEquals("EXCESO_DEVOLUCION", r.getErrores().get(0).getMotivo());
	}

	@Test
	void remapeoInvalidoRechaza() {
		when(empleadoPersistencia.existePorCodigo("E7")).thenReturn(true);
		when(empleadoPersistencia.obtenerPorCodigo("E7")).thenReturn(empleado);
		when(herramientaPersistencia.existePorCodigo("H42")).thenReturn(true);
		when(herramientaPersistencia.obtenerPorCodigo("H42")).thenReturn(herramienta);

		AsignacionHerramienta abierta = new AsignacionHerramienta();
		abierta.setId(1L);
		abierta.setCantidad(2);
		when(asignacionHerramientaPersistencia.activasMasAntiguas(7L, 42L, Integer.MAX_VALUE))
				.thenReturn(List.of(abierta));

		ItemEscaneoLote i = item("H42", "3");
		com.art.inventario.aplicacion.dto.DevolucionAsignacion da = new com.art.inventario.aplicacion.dto.DevolucionAsignacion();
		da.setId(1L);
		da.setCantidad(new BigDecimal("2"));
		i.setAsignaciones(List.of(da));

		ResultadoLoteEscaneo r = procesador.procesar(lote(TipoLoteEscaneo.DEVOLUCION, "E7", i));
		assertFalse(r.isOk());
		assertEquals("ASIGNACION_REMAP_INVALIDA", r.getErrores().get(0).getMotivo());
	}
}