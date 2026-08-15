package com.art.inventario.persistencia.adaptador;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.art.inventario.dominio.LineaOrdenCompra;
import com.art.inventario.dominio.OrdenCompra;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.OrdenCompraConsultaJpa;
import com.art.inventario.persistencia.consulta.ProveedorConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadLineaOrdenCompra;
import com.art.inventario.persistencia.entidad.EntidadOrdenCompra;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.puerto.salida.OrdenCompraPersistencia;

@Repository
@Transactional(readOnly = true)
public class OrdenCompraPersistenciaJpa implements OrdenCompraPersistencia {
	private final OrdenCompraConsultaJpa consulta;
	private final ProveedorConsultaJpa proveedores;
	public OrdenCompraPersistenciaJpa(OrdenCompraConsultaJpa consulta, ProveedorConsultaJpa proveedores) {
		this.consulta = consulta; this.proveedores = proveedores;
	}
	@Override public List<OrdenCompra> listar() {
		return consulta.findAll().stream().map(this::aDominio).toList();
	}
	@Override public OrdenCompra obtener(Long id) {
		return aDominio(consulta.findById(id).orElseThrow(() -> new NoEncontradoExcepcion("Orden de compra no encontrada")));
	}
	@Override @Transactional public OrdenCompra guardar(OrdenCompra orden) {
		EntidadProveedor prov = null;
		if (orden.getProveedor() != null && orden.getProveedor().getId() != null) {
			prov = proveedores.findById(orden.getProveedor().getId()).orElse(null);
		}
		EntidadOrdenCompra e;
		if (orden.getId() != null) {
			e = consulta.findById(orden.getId()).orElseThrow(() -> new NoEncontradoExcepcion("Orden de compra no encontrada"));
			e.setFecha(orden.getFecha());
			e.setObservacion(orden.getObservacion());
			e.setTotal(orden.getTotal());
			e.setProveedor(prov);
			e.getLineas().clear();
		} else {
			e = new EntidadOrdenCompra();
			e.setFecha(orden.getFecha());
			e.setObservacion(orden.getObservacion());
			e.setTotal(orden.getTotal());
			e.setProveedor(prov);
		}
		if (orden.getLineas() != null) {
			List<EntidadLineaOrdenCompra> lineas = new ArrayList<>();
			for (LineaOrdenCompra l : orden.getLineas()) {
				lineas.add(Mapeador.aEntidad(l, e));
			}
			e.getLineas().addAll(lineas);
		}
		return aDominio(consulta.save(e));
	}
	@Override @Transactional public void eliminar(Long id) {
		if (!consulta.existsById(id)) throw new NoEncontradoExcepcion("Orden de compra no encontrada");
		consulta.deleteById(id);
	}
	private OrdenCompra aDominio(EntidadOrdenCompra e) {
		return Mapeador.aDominio(e, Mapeador.aDominioLineasOrden(e.getLineas()));
	}
}
