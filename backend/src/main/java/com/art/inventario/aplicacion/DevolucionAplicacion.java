package com.art.inventario.aplicacion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Devolucion;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.LineaCompra;
import com.art.inventario.dominio.LineaDevolucion;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;
import com.art.inventario.puerto.entrada.DevolucionCasoDeUso;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.CompraPersistencia;
import com.art.inventario.puerto.salida.DevolucionPersistencia;

@Service
public class DevolucionAplicacion implements DevolucionCasoDeUso {

	private final DevolucionPersistencia persistencia;
	private final CompraPersistencia compraPersistencia;
	private final HerramientaCasoDeUso herramientas;
	private final EppCasoDeUso epps;
	private final ConsumibleCasoDeUso consumibles;
	private final MaterialCasoDeUso materiales;
	private final CambiosNotificador notificador;

	public DevolucionAplicacion(DevolucionPersistencia persistencia, CompraPersistencia compraPersistencia,
			HerramientaCasoDeUso herramientas, EppCasoDeUso epps, ConsumibleCasoDeUso consumibles,
			MaterialCasoDeUso materiales, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.compraPersistencia = compraPersistencia;
		this.herramientas = herramientas;
		this.epps = epps;
		this.consumibles = consumibles;
		this.materiales = materiales;
		this.notificador = notificador;
	}

	@Override
	public List<Devolucion> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Devolucion> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public List<Devolucion> listarPorCompra(Long compraId) {
		compraPersistencia.obtener(compraId);
		return persistencia.listarPorCompra(compraId);
	}

	@Override
	public Devolucion obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Devolucion crear(Long compraId, Devolucion devolucion) {
		Compra compra = compraPersistencia.obtener(compraId);
		devolucion.setCompraId(compraId);
		validarDevolucion(devolucion, compra);
		Devolucion creada = persistencia.guardar(devolucion);
		aplicarMovimientos(creada.getId(), creada.getLineas(), creada.getFecha());
		notificar(creada.getLineas());
		return obtener(creada.getId());
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Devolucion actual = persistencia.obtener(id);
		revertirMovimientos(id, actual.getLineas());
		persistencia.eliminar(id);
		notificar(actual.getLineas());
	}

	private void validarDevolucion(Devolucion devolucion, Compra compra) {
		if (devolucion.getFecha() == null || devolucion.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		if (devolucion.getLineas() == null || devolucion.getLineas().isEmpty()) {
			throw new DatosInvalidosExcepcion("Debe agregar al menos un artículo");
		}
		Map<String, Integer> compradas = cantidadesCompradas(compra.getLineas());
		Map<String, Integer> nuevas = new HashMap<>();
		for (LineaDevolucion linea : devolucion.getLineas()) {
			if (linea.getCantidad() == null || linea.getCantidad() <= 0) {
				throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
			}
			validarTipo(linea.getTipo());
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				if (linea.getDescripcion() == null || linea.getDescripcion().isBlank()) {
					throw new DatosInvalidosExcepcion("La ropa requiere una descripción");
				}
				continue;
			}
			if (linea.getProductoId() == null) {
				throw new DatosInvalidosExcepcion("Seleccione un producto válido");
			}
			validarProductoExiste(linea.getTipo(), linea.getProductoId());
			String clave = clave(linea.getTipo(), linea.getProductoId());
			if (!compradas.containsKey(clave)) {
				throw new DatosInvalidosExcepcion("El producto no pertenece a la compra");
			}
			nuevas.merge(clave, linea.getCantidad(), Integer::sum);
			int yaDevuelto = persistencia.cantidadDevuelta(compra.getId(), linea.getTipo(), linea.getProductoId());
			int totalNueva = nuevas.get(clave);
			if (yaDevuelto + totalNueva > compradas.get(clave)) {
				throw new DatosInvalidosExcepcion(
						"La cantidad devuelta supera la cantidad comprada del producto");
			}
			int stock = stockActual(linea.getTipo(), linea.getProductoId());
			if (stock < linea.getCantidad()) {
				throw new DatosInvalidosExcepcion("Stock insuficiente para realizar la devolución");
			}
		}
	}

	private Map<String, Integer> cantidadesCompradas(List<LineaCompra> lineas) {
		Map<String, Integer> map = new HashMap<>();
		for (LineaCompra linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo()) || linea.getProductoId() == null) {
				continue;
			}
			map.merge(clave(linea.getTipo(), linea.getProductoId()), linea.getCantidad(), Integer::sum);
		}
		return map;
	}

	private void validarTipo(String tipo) {
		if (!Compra.TIPO_HERRAMIENTA.equals(tipo) && !Compra.TIPO_EPP.equals(tipo)
				&& !Compra.TIPO_CONSUMIBLE.equals(tipo) && !Compra.TIPO_MATERIAL.equals(tipo)
				&& !Compra.TIPO_ROPA.equals(tipo)) {
			throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + tipo);
		}
	}

	private void validarProductoExiste(String tipo, Long productoId) {
		switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> herramientas.obtener(productoId);
		case Compra.TIPO_EPP -> epps.obtener(productoId);
		case Compra.TIPO_CONSUMIBLE -> consumibles.obtener(productoId);
		case Compra.TIPO_MATERIAL -> materiales.obtener(productoId);
		default -> {
		}
		}
	}

	private int stockActual(String tipo, Long productoId) {
		return switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> {
			Herramienta h = herramientas.obtener(productoId);
			Integer disp = h.getCantidadDisponible();
			if (disp != null) {
				yield disp;
			}
			int total = h.getCantidadTotal() == null ? 0 : h.getCantidadTotal();
			int danada = h.getCantidadDanada() == null ? 0 : h.getCantidadDanada();
			int perdida = h.getCantidadPerdida() == null ? 0 : h.getCantidadPerdida();
			int asignada = h.getCantidadAsignada() == null ? 0 : h.getCantidadAsignada();
			yield total - danada - perdida - asignada;
		}
		case Compra.TIPO_EPP -> {
			Epp e = epps.obtener(productoId);
			yield e.getStock() == null ? 0 : e.getStock();
		}
		case Compra.TIPO_CONSUMIBLE -> {
			Consumible c = consumibles.obtener(productoId);
			yield c.getStock() == null ? 0 : c.getStock().intValue();
		}
		case Compra.TIPO_MATERIAL -> {
			Material m = materiales.obtener(productoId);
			yield m.getStock() == null ? 0 : m.getStock();
		}
		default -> 0;
		};
	}

	private void aplicarMovimientos(Long devolucionId, List<LineaDevolucion> lineas, String fecha) {
		String etiqueta = etiqueta(devolucionId);
		for (LineaDevolucion linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			registrarEgreso(linea.getTipo(), linea.getProductoId(), linea.getCantidad(), fecha, etiqueta);
		}
	}

	private void revertirMovimientos(Long devolucionId, List<LineaDevolucion> lineas) {
		String etiqueta = etiqueta(devolucionId);
		for (LineaDevolucion linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			eliminarMovimientosConEtiqueta(linea.getTipo(), linea.getProductoId(), etiqueta);
		}
	}

	private void registrarEgreso(String tipo, Long productoId, Integer cantidad, String fecha, String etiqueta) {
		switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> {
			MovimientoHerramienta m = new MovimientoHerramienta();
			m.setTipo("EGRESO");
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			herramientas.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_EPP -> {
			MovimientoEpp m = new MovimientoEpp();
			m.setTipo("EGRESO");
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			epps.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_CONSUMIBLE -> {
			MovimientoConsumible m = new MovimientoConsumible();
			m.setTipo("EGRESO");
			m.setCantidad(java.math.BigDecimal.valueOf(cantidad));
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			consumibles.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_MATERIAL -> {
			MovimientoMaterial m = new MovimientoMaterial();
			m.setTipo("EGRESO");
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			materiales.registrarMovimiento(productoId, m);
		}
		default -> {
		}
		}
	}

	private void eliminarMovimientosConEtiqueta(String tipo, Long productoId, String etiqueta) {
		switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> herramientas.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> herramientas.eliminarMovimiento(m.getId()));
		case Compra.TIPO_EPP -> epps.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion())).forEach(m -> epps.eliminarMovimiento(m.getId()));
		case Compra.TIPO_CONSUMIBLE -> consumibles.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> consumibles.eliminarMovimiento(m.getId()));
		case Compra.TIPO_MATERIAL -> materiales.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> materiales.eliminarMovimiento(m.getId()));
		default -> {
		}
		}
	}

	private void notificar(List<LineaDevolucion> lineas) {
		notificador.publicar(CambiosNotificador.RECURSO_DEVOLUCIONES);
		notificador.publicar(CambiosNotificador.RECURSO_COMPRAS);
		Set<String> recursos = new HashSet<>();
		for (LineaDevolucion linea : lineas) {
			switch (linea.getTipo()) {
			case Compra.TIPO_HERRAMIENTA -> recursos.add(CambiosNotificador.RECURSO_HERRAMIENTAS);
			case Compra.TIPO_EPP -> recursos.add(CambiosNotificador.RECURSO_EPP);
			case Compra.TIPO_CONSUMIBLE -> recursos.add(CambiosNotificador.RECURSO_CONSUMIBLES);
			case Compra.TIPO_MATERIAL -> recursos.add(CambiosNotificador.RECURSO_MATERIALES);
			default -> {
			}
			}
		}
		recursos.forEach(notificador::publicar);
	}

	private static String etiqueta(Long devolucionId) {
		return "Devolución #" + devolucionId;
	}

	private static String clave(String tipo, Long productoId) {
		return tipo + ":" + productoId;
	}
}
