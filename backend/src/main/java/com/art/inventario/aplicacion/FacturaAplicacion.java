package com.art.inventario.aplicacion;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.LineaCompra;
import com.art.inventario.dominio.LineaFactura;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.CompraCasoDeUso;
import com.art.inventario.puerto.entrada.FacturaCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.CompraPersistencia;
import com.art.inventario.puerto.salida.FacturaPersistencia;
import com.art.inventario.puerto.salida.ProductoCostoPersistencia;

@Service
public class FacturaAplicacion implements FacturaCasoDeUso {

	private final FacturaPersistencia persistencia;
	private final CompraPersistencia compraPersistencia;
	private final CompraCasoDeUso compras;
	private final ProductoCostoPersistencia costos;
	private final CambiosNotificador notificador;

	public FacturaAplicacion(FacturaPersistencia persistencia, CompraPersistencia compraPersistencia,
			CompraCasoDeUso compras, ProductoCostoPersistencia costos, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.compraPersistencia = compraPersistencia;
		this.compras = compras;
		this.costos = costos;
		this.notificador = notificador;
	}

	@Override
	public List<Factura> listar() {
		return persistencia.listar();
	}

	@Override
	public Factura obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Factura crear(Factura factura) {
		validarFactura(factura);
		completarSubtotales(factura);
		if (factura.isCrearCompra()) {
			Compra creada = compras.crear(compraDesdeFactura(factura));
			factura.setCompraId(creada.getId());
			factura.setCrearCompra(false);
			Factura guardada = persistencia.guardar(factura);
			compraPersistencia.vincularFactura(creada.getId(), guardada.getId());
			actualizarCostos(guardada);
			notificar(guardada);
			return obtener(guardada.getId());
		}
		if (factura.getCompraId() != null) {
			Compra compra = compraPersistencia.obtener(factura.getCompraId());
			if (compra.facturada()) {
				throw new ConflictoExcepcion("La compra ya está asociada a otra factura");
			}
			Factura guardada = persistencia.guardar(factura);
			compraPersistencia.vincularFactura(compra.getId(), guardada.getId());
			actualizarCostos(guardada);
			notificar(guardada);
			return obtener(guardada.getId());
		}
		Factura guardada = persistencia.guardar(factura);
		actualizarCostos(guardada);
		notificar(guardada);
		return obtener(guardada.getId());
	}

	@Override
	@Transactional
	public Factura actualizar(Long id, Factura datos) {
		Factura actual = persistencia.obtener(id);
		if (actual.getCompraId() != null) {
			throw new ConflictoExcepcion("No se puede modificar una factura vinculada a una compra");
		}
		validarFactura(datos);
		completarSubtotales(datos);
		Set<LineaFactura> afectadas = new HashSet<>(actual.getLineas());
		afectadas.addAll(datos.getLineas());
		datos.setId(id);
		datos.setCompraId(null);
		persistencia.guardar(datos);
		recalcularCostos(afectadas);
		notificador.publicar(CambiosNotificador.RECURSO_FACTURAS);
		return obtener(id);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Factura actual = persistencia.obtener(id);
		if (actual.getCompraId() != null) {
			compraPersistencia.desvincularFactura(actual.getCompraId());
		}
		persistencia.eliminar(id);
		recalcularCostos(actual.getLineas());
		notificador.publicar(CambiosNotificador.RECURSO_FACTURAS);
	}

	private Compra compraDesdeFactura(Factura factura) {
		Compra compra = new Compra();
		compra.setFecha(factura.getFecha());
		compra.setObservacion(factura.getObservacion());
		compra.setProveedor(factura.getProveedor());
		List<LineaCompra> lineas = factura.getLineas().stream().map(l -> {
			LineaCompra lc = new LineaCompra();
			lc.setTipo(l.getTipo());
			lc.setProductoId(l.getProductoId());
			lc.setDescripcion(l.getDescripcion());
			lc.setCantidad(l.getCantidad());
			return lc;
		}).toList();
		compra.setLineas(lineas);
		return compra;
	}

	private void validarFactura(Factura factura) {
		if (factura.getFecha() == null || factura.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		if (factura.getLineas() == null || factura.getLineas().isEmpty()) {
			throw new DatosInvalidosExcepcion("Debe agregar al menos un artículo");
		}
		for (LineaFactura linea : factura.getLineas()) {
			if (linea.getCantidad() == null || linea.getCantidad() <= 0) {
				throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
			}
			if (linea.getCostoUnitario() == null || linea.getCostoUnitario() < 0) {
				throw new DatosInvalidosExcepcion("El costo unitario es obligatorio y no puede ser negativo");
			}
			if (!Compra.TIPO_HERRAMIENTA.equals(linea.getTipo()) && !Compra.TIPO_EPP.equals(linea.getTipo())
					&& !Compra.TIPO_CONSUMIBLE.equals(linea.getTipo()) && !Compra.TIPO_MATERIAL.equals(linea.getTipo())
					&& !Compra.TIPO_ROPA.equals(linea.getTipo())) {
				throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + linea.getTipo());
			}
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				if (linea.getDescripcion() == null || linea.getDescripcion().isBlank()) {
					throw new DatosInvalidosExcepcion("La ropa requiere una descripción");
				}
			} else if (linea.getProductoId() == null) {
				throw new DatosInvalidosExcepcion("Seleccione un producto válido");
			}
		}
	}

	private void completarSubtotales(Factura factura) {
		double total = 0;
		for (LineaFactura linea : factura.getLineas()) {
			double subtotal = linea.getCantidad() * linea.getCostoUnitario();
			linea.setSubtotal(subtotal);
			total += subtotal;
		}
		factura.setTotal(total);
	}

	private void actualizarCostos(Factura factura) {
		for (LineaFactura linea : factura.getLineas()) {
			if (!Compra.TIPO_ROPA.equals(linea.getTipo())) {
				costos.actualizarUltimoCosto(linea.getTipo(), linea.getProductoId(), linea.getCostoUnitario());
			}
		}
	}

	private void recalcularCostos(Collection<LineaFactura> lineas) {
		Set<String> claves = new HashSet<>();
		for (LineaFactura linea : lineas) {
			if (Compra.TIPO_ROPA.equals(linea.getTipo())) {
				continue;
			}
			String clave = linea.getTipo() + ":" + linea.getProductoId();
			if (claves.add(clave)) {
				Double costo = persistencia.ultimoCosto(linea.getTipo(), linea.getProductoId());
				costos.actualizarUltimoCosto(linea.getTipo(), linea.getProductoId(), costo);
			}
		}
	}

	private void notificar(Factura factura) {
		notificador.publicar(CambiosNotificador.RECURSO_FACTURAS);
		notificador.publicar(CambiosNotificador.RECURSO_COMPRAS);
	}
}