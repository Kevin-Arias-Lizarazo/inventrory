package com.art.inventario.aplicacion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.LineaCompra;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;
import com.art.inventario.puerto.entrada.CompraCasoDeUso;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.CompraPersistencia;
import com.art.inventario.puerto.salida.DevolucionPersistencia;
import com.art.inventario.puerto.salida.FacturaPersistencia;

@Service
public class CompraAplicacion implements CompraCasoDeUso {

	private final CompraPersistencia persistencia;
	private final FacturaPersistencia facturaPersistencia;
	private final DevolucionPersistencia devoluciones;
	private final HerramientaCasoDeUso herramientas;
	private final EppCasoDeUso epps;
	private final ConsumibleCasoDeUso consumibles;
	private final MaterialCasoDeUso materiales;
	private final CambiosNotificador notificador;

	public CompraAplicacion(CompraPersistencia persistencia, FacturaPersistencia facturaPersistencia,
			DevolucionPersistencia devoluciones, HerramientaCasoDeUso herramientas, EppCasoDeUso epps,
			ConsumibleCasoDeUso consumibles, MaterialCasoDeUso materiales, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.facturaPersistencia = facturaPersistencia;
		this.devoluciones = devoluciones;
		this.herramientas = herramientas;
		this.epps = epps;
		this.consumibles = consumibles;
		this.materiales = materiales;
		this.notificador = notificador;
	}

	@Override
	public List<Compra> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Compra> listarPagina(String q, Long proveedorId, String fecha,
			Boolean facturada, Integer pagina, Integer tamano) {
		List<Compra> lista = listar();
		if (q != null && !q.isBlank()) {
			String criterio = q.toLowerCase();
			lista = lista.stream()
					.filter(c -> c.getObservacion() != null && c.getObservacion().toLowerCase().contains(criterio))
					.toList();
		}
		if (proveedorId != null) {
			lista = lista.stream()
					.filter(c -> c.getProveedor() != null && proveedorId.equals(c.getProveedor().getId()))
					.toList();
		}
		if (fecha != null && !fecha.isBlank()) {
			lista = lista.stream()
					.filter(c -> fecha.equals(c.getFecha()))
					.toList();
		}
		if (facturada != null) {
			lista = lista.stream()
					.filter(c -> facturada ? c.facturada() : !c.facturada())
					.toList();
		}
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override
	public Compra obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Compra crear(Compra compra) {
		validarCompra(compra);
		validarProductos(compra.getLineas());
		Compra creada = persistencia.guardar(compra);
		aplicarMovimientos(creada.getId(), creada.getLineas(), creada.getFecha());
		notificarCompra(creada.getLineas());
		return obtener(creada.getId());
	}

	@Override
	@Transactional
	public Compra actualizar(Long id, Compra datos) {
		Compra actual = persistencia.obtener(id);
		if (actual.facturada()) {
			throw new ConflictoExcepcion("No se puede modificar una compra ya facturada");
		}
		if (devoluciones.tienePorCompra(id)) {
			throw new ConflictoExcepcion("No se puede modificar una compra con devoluciones");
		}
		validarCompra(datos);
		validarProductos(datos.getLineas());
		revertirMovimientos(id, actual.getLineas());
		datos.setId(id);
		Compra guardada = persistencia.guardar(datos);
		aplicarMovimientos(id, guardada.getLineas(), guardada.getFecha());
		notificarCompra(guardada.getLineas());
		return obtener(id);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Compra actual = persistencia.obtener(id);
		if (devoluciones.tienePorCompra(id)) {
			throw new ConflictoExcepcion("No se puede eliminar una compra con devoluciones");
		}
		if (actual.facturada()) {
			facturaPersistencia.desvincularCompra(id);
		}
		revertirMovimientos(id, actual.getLineas());
		persistencia.eliminar(id);
		notificarCompra(actual.getLineas());
	}

	private void validarCompra(Compra compra) {
		if (compra.getFecha() == null || compra.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		if (compra.getLineas() == null || compra.getLineas().isEmpty()) {
			throw new DatosInvalidosExcepcion("Debe agregar al menos un artículo");
		}
		for (LineaCompra linea : compra.getLineas()) {
			if (linea.getCantidad() == null || linea.getCantidad() <= 0) {
				throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
			}
			validarTipo(linea.getTipo());
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				if (linea.getDescripcion() == null || linea.getDescripcion().isBlank()) {
					throw new DatosInvalidosExcepcion("La ropa requiere una descripción");
				}
			} else if (linea.getProductoId() == null) {
				throw new DatosInvalidosExcepcion("Seleccione un producto válido");
			}
		}
	}

	private void validarTipo(String tipo) {
		if (!Compra.TIPO_HERRAMIENTA.equals(tipo) && !Compra.TIPO_EPP.equals(tipo)
				&& !Compra.TIPO_CONSUMIBLE.equals(tipo) && !Compra.TIPO_MATERIAL.equals(tipo)
				&& !Compra.TIPO_ROPA.equals(tipo)) {
			throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + tipo);
		}
	}

	private void validarProductos(List<LineaCompra> lineas) {
		for (LineaCompra linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			switch (linea.getTipo()) {
			case Compra.TIPO_HERRAMIENTA -> herramientas.obtener(linea.getProductoId());
			case Compra.TIPO_EPP -> epps.obtener(linea.getProductoId());
			case Compra.TIPO_CONSUMIBLE -> consumibles.obtener(linea.getProductoId());
			case Compra.TIPO_MATERIAL -> materiales.obtener(linea.getProductoId());
			default -> throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + linea.getTipo());
			}
		}
	}

	private void aplicarMovimientos(Long compraId, List<LineaCompra> lineas, String fecha) {
		String etiqueta = etiqueta(compraId);
		for (LineaCompra linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			registrarIngreso(linea.getTipo(), linea.getProductoId(), linea.getCantidad(), fecha, etiqueta);
		}
	}

	private void revertirMovimientos(Long compraId, List<LineaCompra> lineas) {
		String etiqueta = etiqueta(compraId);
		for (LineaCompra linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			eliminarMovimientosConEtiqueta(linea.getTipo(), linea.getProductoId(), etiqueta);
		}
	}

	private void registrarIngreso(String tipo, Long productoId, Integer cantidad, String fecha, String etiqueta) {
		switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> {
			MovimientoHerramienta m = new MovimientoHerramienta();
			m.setTipo("INGRESO");
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			herramientas.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_EPP -> {
			MovimientoEpp m = new MovimientoEpp();
			m.setTipo("INGRESO");
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			epps.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_CONSUMIBLE -> {
			MovimientoConsumible m = new MovimientoConsumible();
			m.setTipo("INGRESO");
			m.setCantidad(java.math.BigDecimal.valueOf(cantidad));
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			consumibles.registrarMovimiento(productoId, m);
		}
		case Compra.TIPO_MATERIAL -> {
			MovimientoMaterial m = new MovimientoMaterial();
			m.setTipo("INGRESO");
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
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> epps.eliminarMovimiento(m.getId()));
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

	private void notificarCompra(List<LineaCompra> lineas) {
		notificador.publicar(CambiosNotificador.RECURSO_COMPRAS);
		Set<String> recursos = new HashSet<>();
		for (LineaCompra linea : lineas) {
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

	private static String etiqueta(Long compraId) {
		return "Compra #" + compraId;
	}
}