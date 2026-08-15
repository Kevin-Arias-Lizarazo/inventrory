package com.art.inventario.persistencia.adaptador;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Devolucion;
import com.art.inventario.dominio.LineaDevolucion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.DevolucionConsultaJpa;
import com.art.inventario.persistencia.consulta.LineaDevolucionConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadDevolucion;
import com.art.inventario.persistencia.entidad.EntidadLineaDevolucion;
import com.art.inventario.puerto.salida.DevolucionPersistencia;

@Repository
@Transactional(readOnly = true)
public class DevolucionPersistenciaJpa implements DevolucionPersistencia {

	private final DevolucionConsultaJpa consulta;
	private final LineaDevolucionConsultaJpa lineasConsulta;

	public DevolucionPersistenciaJpa(DevolucionConsultaJpa consulta, LineaDevolucionConsultaJpa lineasConsulta) {
		this.consulta = consulta;
		this.lineasConsulta = lineasConsulta;
	}

	@Override
	public List<Devolucion> listar() {
		return consulta.findAll().stream().map(this::aDominio).toList();
	}

	@Override
	public List<Devolucion> listarPorCompra(Long compraId) {
		return consulta.findByCompraId(compraId).stream().map(this::aDominio).toList();
	}

	@Override
	public Devolucion obtener(Long id) {
		return aDominio(consulta.findById(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Devolución no encontrada")));
	}

	@Override
	@Transactional
	public Devolucion guardar(Devolucion devolucion) {
		EntidadDevolucion entidad;
		if (devolucion.getId() != null) {
			entidad = consulta.findById(devolucion.getId())
					.orElseThrow(() -> new NoEncontradoExcepcion("Devolución no encontrada"));
			entidad.setFecha(devolucion.getFecha());
			entidad.setObservacion(devolucion.getObservacion());
			entidad.setCompraId(devolucion.getCompraId());
			entidad.getLineas().clear();
		} else {
			entidad = new EntidadDevolucion();
			entidad.setFecha(devolucion.getFecha());
			entidad.setObservacion(devolucion.getObservacion());
			entidad.setCompraId(devolucion.getCompraId());
		}
		List<EntidadLineaDevolucion> lineas = new ArrayList<>();
		if (devolucion.getLineas() != null) {
			for (LineaDevolucion linea : devolucion.getLineas()) {
				lineas.add(Mapeador.aEntidad(linea, entidad));
			}
		}
		entidad.getLineas().addAll(lineas);
		entidad = consulta.save(entidad);
		return aDominio(entidad);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Devolución no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tienePorCompra(Long compraId) {
		return consulta.existsByCompraId(compraId);
	}

	@Override
	public boolean tieneProducto(String tipo, Long productoId) {
		return lineasConsulta.existsByTipoAndProductoId(tipo, productoId);
	}

	@Override
	public int cantidadDevuelta(Long compraId, String tipo, Long productoId) {
		Integer suma = lineasConsulta.sumCantidadByCompraAndTipoAndProducto(compraId, tipo, productoId);
		return suma == null ? 0 : suma;
	}

	private Devolucion aDominio(EntidadDevolucion e) {
		return Mapeador.aDominio(e, Mapeador.aDominioLineasDevolucion(e.getLineas()));
	}
}
