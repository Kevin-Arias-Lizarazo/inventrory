package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.LineaOrdenCompra;
import com.art.inventario.dominio.OrdenCompra;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.OrdenCompraCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.OrdenCompraPersistencia;

@Service
public class OrdenCompraAplicacion implements OrdenCompraCasoDeUso {
	private final OrdenCompraPersistencia persistencia;
	private final CambiosNotificador notificador;

	public OrdenCompraAplicacion(OrdenCompraPersistencia persistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.notificador = notificador;
	}

	@Override public List<OrdenCompra> listar() { return persistencia.listar(); }

	@Override
	public PaginaResultado<OrdenCompra> listarPagina(String q, Long proveedorId, String fecha,
			Integer pagina, Integer tamano) {
		List<OrdenCompra> lista = listar();
		if (q != null && !q.isBlank()) {
			String criterio = q.toLowerCase();
			lista = lista.stream()
					.filter(o -> o.getObservacion() != null && o.getObservacion().toLowerCase().contains(criterio))
					.toList();
		}
		if (proveedorId != null) {
			lista = lista.stream()
					.filter(o -> o.getProveedor() != null && proveedorId.equals(o.getProveedor().getId()))
					.toList();
		}
		if (fecha != null && !fecha.isBlank()) {
			lista = lista.stream()
					.filter(o -> fecha.equals(o.getFecha()))
					.toList();
		}
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override public OrdenCompra obtener(Long id) { return persistencia.obtener(id); }

	@Override @Transactional
	public OrdenCompra crear(OrdenCompra orden) {
		validar(orden);
		completar(orden);
		OrdenCompra creada = persistencia.guardar(orden);
		notificador.publicar(CambiosNotificador.RECURSO_ORDENES_COMPRA);
		return obtener(creada.getId());
	}

	@Override @Transactional
	public OrdenCompra actualizar(Long id, OrdenCompra datos) {
		persistencia.obtener(id);
		validar(datos);
		completar(datos);
		datos.setId(id);
		persistencia.guardar(datos);
		notificador.publicar(CambiosNotificador.RECURSO_ORDENES_COMPRA);
		return obtener(id);
	}

	@Override @Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_ORDENES_COMPRA);
	}

	private void validar(OrdenCompra orden) {
		if (orden.getFecha() == null || orden.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		if (orden.getLineas() == null || orden.getLineas().isEmpty()) {
			throw new DatosInvalidosExcepcion("Debe agregar al menos un artículo");
		}
		for (LineaOrdenCompra l : orden.getLineas()) {
			if (l.getCantidad() == null || l.getCantidad() < 1) {
				throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
			}
			if (l.getCostoUnitario() == null || l.getCostoUnitario() < 0) {
				throw new DatosInvalidosExcepcion("El costo unitario es obligatorio");
			}
			if (l.getDescripcion() == null || l.getDescripcion().isBlank()) {
				throw new DatosInvalidosExcepcion("La descripción es obligatoria");
			}
		}
	}

	private void completar(OrdenCompra orden) {
		double total = 0;
		for (LineaOrdenCompra l : orden.getLineas()) {
			double sub = l.getCantidad() * l.getCostoUnitario();
			l.setSubtotal(sub);
			total += sub;
		}
		orden.setTotal(total);
	}
}
