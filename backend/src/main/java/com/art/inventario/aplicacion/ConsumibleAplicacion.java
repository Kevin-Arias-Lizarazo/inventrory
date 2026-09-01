package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;
import com.art.inventario.puerto.salida.AjusteConsultaSalida;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.DevolucionPersistencia;

@Service
public class ConsumibleAplicacion implements ConsumibleCasoDeUso {

	private final ConsumiblePersistencia persistencia;
	private final CambiosNotificador notificador;
	private final AjusteConsultaSalida ajustes;
	private final DevolucionPersistencia devoluciones;

	public ConsumibleAplicacion(ConsumiblePersistencia persistencia, CambiosNotificador notificador,
			AjusteConsultaSalida ajustes, DevolucionPersistencia devoluciones) {
		this.persistencia = persistencia;
		this.notificador = notificador;
		this.ajustes = ajustes;
		this.devoluciones = devoluciones;
	}

	@Override
	public List<Consumible> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Consumible> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public PaginaResultado<Consumible> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public Consumible obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Consumible crear(Consumible consumible) {
		validarNombre(consumible);
		validarNombreUnico(consumible.getNombre(), null);
		if (consumible.getStock() == null) {
			consumible.setStock(BigDecimal.ZERO);
		}
		Consumible creado = persistencia.guardar(consumible);
		creado.setCodigo("C" + creado.getId());
		creado = persistencia.guardar(creado);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		return creado;
	}

	@Override
	@Transactional
	public Consumible crearConCodigo(Consumible consumible) {
		validarNombre(consumible);
		validarNombreUnico(consumible.getNombre(), null);
		if (consumible.getCodigo() == null || consumible.getCodigo().isBlank()) {
			throw new DatosInvalidosExcepcion("El código es obligatorio para creación express");
		}
		if (persistencia.existePorCodigo(consumible.getCodigo())) {
			throw new ConflictoExcepcion("Ya existe un ítem con el código " + consumible.getCodigo());
		}
		if (consumible.getStock() == null) {
			consumible.setStock(BigDecimal.ZERO);
		}
		Consumible creado = persistencia.guardar(consumible);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		return creado;
	}

	@Override
	@Transactional
	public Consumible actualizar(Long id, Consumible datos) {
		Consumible actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		actual.setNombre(datos.getNombre());
		actual.setMarca(datos.getMarca());
		actual.setStockMinimo(datos.getStockMinimo());
		actual.setUnidad(datos.getUnidad());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFotoUrl(datos.getFotoUrl());
		Consumible guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (persistencia.tieneMovimientos(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el consumible tiene movimientos asociados");
		}
		if (ajustes.tieneProducto("CONSUMIBLE", id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el consumible tiene ajustes asociados");
		}
		if (devoluciones.tieneProducto("CONSUMIBLE", id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el consumible tiene devoluciones asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
	}

	@Override
	public List<MovimientoConsumible> listarMovimientos(Long consumibleId) {
		persistencia.obtener(consumibleId);
		return persistencia.listarMovimientos(consumibleId);
	}

	@Override
	public List<MovimientoConsumible> listarTodosMovimientos() {
		return persistencia.listarTodosMovimientos().stream()
				.sorted(Comparator.comparing(ConsumibleAplicacion::fechaComparable).reversed())
				.toList();
	}

	@Override
	@Transactional
	public MovimientoConsumible registrarMovimiento(Long consumibleId, MovimientoConsumible movimiento) {
		validarMovimiento(movimiento);
		Consumible consumible = persistencia.obtener(consumibleId);
		int signo = signo(movimiento.getTipo());
		BigDecimal nuevoStock = stock(consumible).add(movimiento.getCantidad().multiply(BigDecimal.valueOf(signo)));
		if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		consumible.setStock(nuevoStock);
		persistencia.guardar(consumible);
		movimiento.setConsumible(consumible);
		MovimientoConsumible creado = persistencia.guardarMovimiento(movimiento);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_CONSUMIBLES);
		return creado;
	}

	@Override
	@Transactional
	public MovimientoConsumible actualizarMovimiento(Long id, MovimientoConsumible datos) {
		validarMovimiento(datos);
		MovimientoConsumible actual = persistencia.obtenerMovimiento(id);
		Consumible consumible = actual.getConsumible();
		int signoNuevo = signo(datos.getTipo());
		BigDecimal ajuste = datos.getCantidad().multiply(BigDecimal.valueOf(signoNuevo))
				.subtract(actual.getCantidad().multiply(BigDecimal.valueOf(signo(actual.getTipo()))));
		BigDecimal nuevoStock = stock(consumible).add(ajuste);
		if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		consumible.setStock(nuevoStock);
		persistencia.guardar(consumible);
		actual.setTipo(datos.getTipo());
		actual.setCantidad(datos.getCantidad());
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		MovimientoConsumible guardado = persistencia.guardarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_CONSUMIBLES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminarMovimiento(Long id) {
		MovimientoConsumible actual = persistencia.obtenerMovimiento(id);
		Consumible consumible = actual.getConsumible();
		BigDecimal nuevoStock = stock(consumible)
				.subtract(actual.getCantidad().multiply(BigDecimal.valueOf(signo(actual.getTipo()))));
		if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		consumible.setStock(nuevoStock);
		persistencia.guardar(consumible);
		persistencia.eliminarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_CONSUMIBLES);
	}

	private void validarMovimiento(MovimientoConsumible movimiento) {
		if (movimiento.getCantidad() == null || movimiento.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		validarPrecision(movimiento.getCantidad());
		if (!"INGRESO".equals(movimiento.getTipo()) && !"EGRESO".equals(movimiento.getTipo())) {
			throw new DatosInvalidosExcepcion("El tipo debe ser INGRESO o EGRESO");
		}
	}

	private void validarNombre(Consumible consumible) {
		if (consumible.getNombre() == null || consumible.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un consumible con ese nombre");
		}
	}

	static void validarPrecision(BigDecimal cantidad) {
		BigDecimal stripped = cantidad.stripTrailingZeros();
		if (stripped.scale() > 1) {
			throw new DatosInvalidosExcepcion("La cantidad admite máximo un decimal");
		}
	}

	static BigDecimal stock(Consumible consumible) {
		return consumible.getStock() == null ? BigDecimal.ZERO : consumible.getStock();
	}

	private static int signo(String tipo) {
		return "INGRESO".equals(tipo) ? 1 : -1;
	}

	private static String fechaComparable(MovimientoConsumible m) {
		return String.valueOf(m.getFecha() == null ? "" : m.getFecha())
				.replace("-", "")
				.replace("/", "");
	}
}