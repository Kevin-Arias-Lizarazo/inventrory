package com.art.inventario.aplicacion;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.MovimientoHerramientaPersistencia;

@Service
public class HerramientaAplicacion implements HerramientaCasoDeUso {

	private final HerramientaPersistencia persistencia;
	private final AsignacionHerramientaPersistencia asignacionPersistencia;
	private final MovimientoHerramientaPersistencia movimientoPersistencia;
	private final CambiosNotificador notificador;

	public HerramientaAplicacion(HerramientaPersistencia persistencia,
			AsignacionHerramientaPersistencia asignacionPersistencia,
			MovimientoHerramientaPersistencia movimientoPersistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.asignacionPersistencia = asignacionPersistencia;
		this.movimientoPersistencia = movimientoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Herramienta> listar() {
		Map<Long, Long> asignadas = asignacionPersistencia.asignacionesActivasPorHerramienta();
		return persistencia.listar().stream().peek(h -> {
			long asignada = asignadas.getOrDefault(h.getId(), 0L);
			h.setCantidadAsignada((int) asignada);
			h.setCantidadDisponible(total(h) - (int) asignada - danada(h) - perdida(h));
		}).toList();
	}

	@Override
	public PaginaResultado<Herramienta> listarPagina(int pagina, int tamano) {
		int p = PaginaResultado.paginaSegura(pagina);
		int t = PaginaResultado.tamanoSeguro(tamano);
		PaginaResultado<Herramienta> paginaResultado = persistencia.listarPagina(p, t);
		Map<Long, Long> asignadas = asignacionPersistencia.asignacionesActivasPorHerramienta();
		paginaResultado.getContenido().forEach(h -> {
			long asignada = asignadas.getOrDefault(h.getId(), 0L);
			h.setCantidadAsignada((int) asignada);
			h.setCantidadDisponible(total(h) - (int) asignada - danada(h) - perdida(h));
		});
		return paginaResultado;
	}

	@Override
	public Herramienta obtener(Long id) {
		Herramienta herramienta = persistencia.obtener(id);
		long asignada = asignacionPersistencia.contarAsignacionesActivas(id, -1L);
		herramienta.setCantidadAsignada((int) asignada);
		herramienta.setCantidadDisponible(total(herramienta) - (int) asignada - danada(herramienta) - perdida(herramienta));
		return herramienta;
	}

	@Override
	@Transactional
	public Herramienta crear(Herramienta herramienta) {
		validarNombre(herramienta);
		validarNombreUnico(herramienta.getNombre(), null);
		if (herramienta.getCantidadTotal() == null || herramienta.getCantidadTotal() < 1) {
			throw new DatosInvalidosExcepcion("La cantidad total debe ser mayor a cero");
		}
		if (herramienta.getCantidadDanada() == null) {
			herramienta.setCantidadDanada(0);
		}
		if (herramienta.getCantidadPerdida() == null) {
			herramienta.setCantidadPerdida(0);
		}
		Herramienta creada = persistencia.guardar(herramienta);
		creada.setCodigo("H" + creada.getId());
		creada = persistencia.guardar(creada);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return creada;
	}

	@Override
	@Transactional
	public Herramienta actualizar(Long id, Herramienta datos) {
		Herramienta actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		if (datos.getCantidadTotal() == null || datos.getCantidadTotal() < 1) {
			throw new DatosInvalidosExcepcion("La cantidad total debe ser mayor a cero");
		}
		long asignada = asignacionPersistencia.contarAsignacionesActivas(id, -1L);
		int ocupadas = (int) asignada + danada(actual) + perdida(actual);
		if (datos.getCantidadTotal() < ocupadas) {
			throw new DatosInvalidosExcepcion(
					"La cantidad total no puede ser menor a las unidades en uso (" + ocupadas + ")");
		}
		actual.setNombre(datos.getNombre());
		actual.setMarca(datos.getMarca());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFotoUrl(datos.getFotoUrl());
		actual.setCantidadTotal(datos.getCantidadTotal());
		Herramienta guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return guardada;
	}

	@Override
	@Transactional
	public Herramienta registrarDanada(Long id) {
		Herramienta actual = persistencia.obtener(id);
		if (disponible(actual) <= 0) {
			throw new DatosInvalidosExcepcion("No hay unidades disponibles para marcar como dañada");
		}
		actual.setCantidadDanada(danada(actual) + 1);
		Herramienta guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return guardada;
	}

	@Override
	@Transactional
	public Herramienta reparar(Long id) {
		Herramienta actual = persistencia.obtener(id);
		if (danada(actual) <= 0) {
			throw new DatosInvalidosExcepcion("No hay unidades dañadas para reparar");
		}
		actual.setCantidadDanada(danada(actual) - 1);
		Herramienta guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return guardada;
	}

	@Override
	@Transactional
	public Herramienta registrarPerdida(Long id) {
		Herramienta actual = persistencia.obtener(id);
		if (disponible(actual) <= 0) {
			throw new DatosInvalidosExcepcion("No hay unidades disponibles para marcar como pérdida");
		}
		actual.setCantidadPerdida(perdida(actual) + 1);
		Herramienta guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (asignacionPersistencia.tieneAsignacionActiva(id)) {
			throw new ConflictoExcepcion(
					"No se puede eliminar: la herramienta está asignada y no ha sido devuelta");
		}
		asignacionPersistencia.desvincularHerramienta(id);
		movimientoPersistencia.eliminarPorHerramienta(id);
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_HERRAMIENTAS);
	}

	@Override
	public List<MovimientoHerramienta> listarMovimientos(Long herramientaId) {
		persistencia.obtener(herramientaId);
		return movimientoPersistencia.listarPorHerramienta(herramientaId);
	}

	@Override
	public List<MovimientoHerramienta> listarTodosMovimientos() {
		return movimientoPersistencia.listarTodos().stream()
				.sorted(Comparator.comparing(HerramientaAplicacion::fechaComparable).reversed())
				.toList();
	}

	@Override
	@Transactional
	public MovimientoHerramienta registrarMovimiento(Long herramientaId, MovimientoHerramienta movimiento) {
		validarMovimiento(movimiento);
		Herramienta herramienta = persistencia.obtener(herramientaId);
		int signo = signo(movimiento.getTipo());
		int nuevoTotal = total(herramienta) + signo * movimiento.getCantidad();
		if (nuevoTotal < ocupadas(herramienta)) {
			throw new DatosInvalidosExcepcion(
					"No se puede egresar: quedarían menos unidades que las en uso/dañadas/perdidas");
		}
		herramienta.setCantidadTotal(nuevoTotal);
		persistencia.guardar(herramienta);
		movimiento.setHerramienta(herramienta);
		MovimientoHerramienta creado = movimientoPersistencia.guardar(movimiento);
		notificarMovimiento();
		return creado;
	}

	@Override
	@Transactional
	public MovimientoHerramienta actualizarMovimiento(Long id, MovimientoHerramienta datos) {
		validarMovimiento(datos);
		MovimientoHerramienta actual = movimientoPersistencia.obtener(id);
		Herramienta herramienta = persistencia.obtener(actual.getHerramienta().getId());
		int ajuste = signo(datos.getTipo()) * datos.getCantidad() - signo(actual.getTipo()) * actual.getCantidad();
		int nuevoTotal = total(herramienta) + ajuste;
		if (nuevoTotal < ocupadas(herramienta)) {
			throw new DatosInvalidosExcepcion(
					"No se puede egresar: quedarían menos unidades que las en uso/dañadas/perdidas");
		}
		herramienta.setCantidadTotal(nuevoTotal);
		persistencia.guardar(herramienta);
		actual.setTipo(datos.getTipo());
		actual.setCantidad(datos.getCantidad());
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		MovimientoHerramienta guardado = movimientoPersistencia.guardar(actual);
		notificarMovimiento();
		return guardado;
	}

	@Override
	@Transactional
	public void eliminarMovimiento(Long id) {
		MovimientoHerramienta actual = movimientoPersistencia.obtener(id);
		Herramienta herramienta = persistencia.obtener(actual.getHerramienta().getId());
		herramienta.setCantidadTotal(total(herramienta) - signo(actual.getTipo()) * actual.getCantidad());
		persistencia.guardar(herramienta);
		movimientoPersistencia.eliminar(id);
		notificarMovimiento();
	}

	private void notificarMovimiento() {
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_HERRAMIENTAS);
	}

	private void validarMovimiento(MovimientoHerramienta movimiento) {
		if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		if (!"INGRESO".equals(movimiento.getTipo()) && !"EGRESO".equals(movimiento.getTipo())) {
			throw new DatosInvalidosExcepcion("El tipo debe ser INGRESO o EGRESO");
		}
	}

	private static int signo(String tipo) {
		return "INGRESO".equals(tipo) ? 1 : -1;
	}

	private static String fechaComparable(MovimientoHerramienta m) {
		return String.valueOf(m.getFecha() == null ? "" : m.getFecha())
				.replace("-", "")
				.replace("/", "");
	}

	private void validarNombre(Herramienta herramienta) {
		if (herramienta.getNombre() == null || herramienta.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe una herramienta con ese nombre");
		}
	}

	private static int total(Herramienta h) {
		return h.getCantidadTotal() == null ? 0 : h.getCantidadTotal();
	}

	private static int danada(Herramienta h) {
		return h.getCantidadDanada() == null ? 0 : h.getCantidadDanada();
	}

	private static int perdida(Herramienta h) {
		return h.getCantidadPerdida() == null ? 0 : h.getCantidadPerdida();
	}

	private int disponible(Herramienta h) {
		long asignada = asignacionPersistencia.contarAsignacionesActivas(h.getId(), -1L);
		return total(h) - (int) asignada - danada(h) - perdida(h);
	}

	private int ocupadas(Herramienta h) {
		long asignada = asignacionPersistencia.contarAsignacionesActivas(h.getId(), -1L);
		return (int) asignada + danada(h) + perdida(h);
	}
}