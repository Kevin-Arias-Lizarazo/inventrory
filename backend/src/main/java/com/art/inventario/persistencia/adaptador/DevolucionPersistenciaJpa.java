package com.art.inventario.persistencia.adaptador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Devolucion;
import com.art.inventario.dominio.LineaDevolucion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.DevolucionConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.LineaDevolucionConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadDevolucion;
import com.art.inventario.persistencia.entidad.EntidadLineaDevolucion;
import com.art.inventario.puerto.salida.DevolucionPersistencia;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class DevolucionPersistenciaJpa implements DevolucionPersistencia {

	private static final Set<String> ORDENABLES_DEVOLUCION = Set.of("id", "fecha", "compraId");

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
	public PaginaResultado<Devolucion> listarPagina(ConsultaPaginada c) {
		Especificaciones.validarRangoFechas(c);
		Map<String, String> filtros = c.getFiltros();
		Specification<EntidadDevolucion> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			String compraId = filtros.get("compraId");
			if (compraId != null && !compraId.isBlank()) {
				predicates.add(cb.equal(root.get("compraId"), Long.valueOf(compraId)));
			}
			String fechaDesde = filtros.get("fechaDesde");
			if (fechaDesde != null && !fechaDesde.isBlank()) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
			}
			String fechaHasta = filtros.get("fechaHasta");
			if (fechaHasta != null && !fechaHasta.isBlank()) {
				predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
			}
			String tipo = filtros.get("tipo");
			String recursoId = filtros.get("recursoId");
			if ((tipo != null && !tipo.isBlank()) || (recursoId != null && !recursoId.isBlank())) {
				predicates.add(existeLinea(cb, query, root, tipo, recursoId));
			}
			String q = c.getQ();
			if (q != null && !q.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("observacion")), "%" + q.toLowerCase() + "%"));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		Sort sort = Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
		Page<EntidadDevolucion> page = consulta.findAll(spec,
				PageRequest.of(c.getPagina(), c.getTamano(), sort));
		List<Devolucion> contenido = page.getContent().stream().map(this::aDominio).toList();
		return new PaginaResultado<>(contenido, c.getPagina(), c.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	private Predicate existeLinea(CriteriaBuilder cb, jakarta.persistence.criteria.CriteriaQuery<?> query,
			Root<EntidadDevolucion> root, String tipo, String recursoId) {
		Subquery<Long> sub = query.subquery(Long.class);
		Root<EntidadLineaDevolucion> linea = sub.from(EntidadLineaDevolucion.class);
		List<Predicate> conds = new ArrayList<>();
		conds.add(cb.equal(linea.get("devolucion"), root));
		if (tipo != null && !tipo.isBlank()) {
			conds.add(cb.equal(linea.get("tipo"), tipo));
		}
		if (recursoId != null && !recursoId.isBlank()) {
			conds.add(cb.equal(linea.get("productoId"), Long.valueOf(recursoId)));
		}
		sub.select(linea.get("id")).where(conds.toArray(new Predicate[0]));
		return cb.exists(sub);
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
