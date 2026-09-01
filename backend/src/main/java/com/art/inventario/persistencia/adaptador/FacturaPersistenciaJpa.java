package com.art.inventario.persistencia.adaptador;

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
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.LineaFactura;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.PredicadoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.FacturaConsultaJpa;
import com.art.inventario.persistencia.consulta.LineaFacturaConsultaJpa;
import com.art.inventario.persistencia.consulta.ProveedorConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadFactura;
import com.art.inventario.persistencia.entidad.EntidadLineaFactura;
import com.art.inventario.persistencia.entidad.EntidadPagoFactura;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.puerto.salida.FacturaPersistencia;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class FacturaPersistenciaJpa implements FacturaPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"proveedorId", new CampoFiltro("proveedor.id", TipoFiltro.ID),
			"compraId", new CampoFiltro("compraId", TipoFiltro.ID),
			"fecha", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"estadoPago", new CampoFiltro(FacturaPersistenciaJpa::porEstadoPago));

	private static final List<String> BUSCABLES = List.of("numero", "observacion", "proveedor.nombre");

	private static final Set<String> ORDENABLES = Set.of("id", "numero", "fecha", "total", "proveedor.nombre");

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
	public PaginaResultado<Factura> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadFactura> spec = Especificaciones.<EntidadFactura>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadFactura> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Factura> contenido = page.getContent().stream()
				.map(e -> Mapeador.aDominio(e,
						Mapeador.aDominioLineasFactura(lineasConsulta.findByFacturaId(e.getId()))))
				.toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
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

	/**
	 * Predicado para el filtro legacy {@code estadoPago} (PENDIENTE/PARCIAL/PAGADA),
	 * derivado de la suma de pagos de la factura frente a su total. Respeta la
	 * sem&aacute;ntica exacta de {@link com.art.inventario.dominio.Factura}.
	 */
	private static Predicate porEstadoPago(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb, String valor) {
		String estado = valor.trim().toUpperCase(java.util.Locale.ROOT);
		Subquery<Double> sub = query.subquery(Double.class);
		Root<EntidadPagoFactura> pago = sub.from(EntidadPagoFactura.class);
		sub.select(cb.coalesce(cb.sum(pago.get("monto")), 0.0));
		sub.where(cb.equal(pago.get("facturaId"), root.get("id")));
		// Comparar contra la subconsulta escalar (sub), nunca contra la expresi&oacute;n
		// agregada interna: Hibernate 7 rechaza un path de un &aacute;rbol distinto.
		Expression<Double> total = root.get("total");
		Expression<Double> umbral = cb.sum(total, -0.001);
		switch (estado) {
			case "PENDIENTE":
				return cb.equal(sub, 0.0);
			case "PAGADA":
				return cb.greaterThanOrEqualTo(sub, umbral);
			case "PARCIAL":
				return cb.and(cb.greaterThan(sub, 0.0), cb.lessThan(sub, umbral));
			default:
				// Estado desconocido: no coincide con nada (misma sem&aacute;ntica que el legacy).
				return cb.equal(cb.literal(1), cb.literal(2));
		}
	}
}