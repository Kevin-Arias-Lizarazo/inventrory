package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.LineaFactura;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.FacturaConsultaJpa;
import com.art.inventario.persistencia.consulta.LineaFacturaConsultaJpa;
import com.art.inventario.persistencia.consulta.ProveedorConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadFactura;
import com.art.inventario.persistencia.entidad.EntidadLineaFactura;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.puerto.salida.FacturaPersistencia;

@Repository
@Transactional(readOnly = true)
public class FacturaPersistenciaJpa implements FacturaPersistencia {

	private final FacturaConsultaJpa consulta;
	private final LineaFacturaConsultaJpa lineasConsulta;
	private final ProveedorConsultaJpa proveedoresConsulta;

	public FacturaPersistenciaJpa(FacturaConsultaJpa consulta, LineaFacturaConsultaJpa lineasConsulta,
			ProveedorConsultaJpa proveedoresConsulta) {
		this.consulta = consulta;
		this.lineasConsulta = lineasConsulta;
		this.proveedoresConsulta = proveedoresConsulta;
	}

	@Override
	public List<Factura> listar() {
		return Mapeador.aDominioFacturas(consulta.findAll(),
				e -> Mapeador.aDominioLineasFactura(lineasConsulta.findByFacturaId(e.getId())));
	}

	@Override
	public Factura obtener(Long id) {
		EntidadFactura entidad = consulta.findById(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Factura no encontrada"));
		return Mapeador.aDominio(entidad, Mapeador.aDominioLineasFactura(lineasConsulta.findByFacturaId(id)));
	}

	@Override
	@Transactional
	public Factura guardar(Factura factura) {
		EntidadProveedor proveedor = resolverProveedor(factura.getProveedor() == null ? null : factura.getProveedor().getId());
		EntidadFactura entidad;
		if (factura.getId() != null) {
			entidad = consulta.findById(factura.getId())
					.orElseThrow(() -> new NoEncontradoExcepcion("Factura no encontrada"));
			entidad.setNumero(factura.getNumero());
			entidad.setFecha(factura.getFecha());
			entidad.setObservacion(factura.getObservacion());
			entidad.setTotal(factura.getTotal());
			entidad.setProveedor(proveedor);
			entidad = consulta.save(entidad);
			lineasConsulta.deleteAll(lineasConsulta.findByFacturaId(entidad.getId()));
		} else {
			entidad = new EntidadFactura();
			entidad.setNumero(factura.getNumero());
			entidad.setFecha(factura.getFecha());
			entidad.setObservacion(factura.getObservacion());
			entidad.setCompraId(factura.getCompraId());
			entidad.setTotal(factura.getTotal());
			entidad.setProveedor(proveedor);
			entidad = consulta.save(entidad);
		}
		final EntidadFactura entidadRef = entidad;
		List<EntidadLineaFactura> lineas = factura.getLineas().stream()
				.map(l -> Mapeador.aEntidad(l, entidadRef))
				.toList();
		lineasConsulta.saveAll(lineas);
		return obtener(entidad.getId());
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Factura no encontrada");
		}
		lineasConsulta.deleteAll(lineasConsulta.findByFacturaId(id));
		consulta.deleteById(id);
	}

	@Override
	public boolean existeLineaProducto(String tipo, Long productoId) {
		return lineasConsulta.existsByTipoAndProductoId(tipo, productoId);
	}

	@Override
	public Double ultimoCosto(String tipo, Long productoId) {
		List<Double> costos = lineasConsulta.costosMasRecientes(tipo, productoId, PageRequest.of(0, 1));
		return costos.isEmpty() ? null : costos.get(0);
	}

	@Override
	@Transactional
	public void vincularCompra(Long facturaId, Long compraId) {
		EntidadFactura entidad = consulta.findById(facturaId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Factura no encontrada"));
		entidad.setCompraId(compraId);
		consulta.save(entidad);
	}

	@Override
	@Transactional
	public void desvincularCompra(Long compraId) {
		consulta.findByCompraId(compraId).ifPresent(f -> {
			f.setCompraId(null);
			consulta.save(f);
		});
	}

	private EntidadProveedor resolverProveedor(Long proveedorId) {
		if (proveedorId == null) {
			return null;
		}
		return proveedoresConsulta.findById(proveedorId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proveedor no encontrado"));
	}
}