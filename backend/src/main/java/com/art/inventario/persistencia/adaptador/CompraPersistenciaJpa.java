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
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.LineaCompra;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.CompraConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.LineaCompraConsultaJpa;
import com.art.inventario.persistencia.consulta.ProveedorConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadCompra;
import com.art.inventario.persistencia.entidad.EntidadLineaCompra;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.puerto.salida.CompraPersistencia;

@Repository
@Transactional(readOnly = true)
public class CompraPersistenciaJpa implements CompraPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"proveedorId", new CampoFiltro("proveedor.id", TipoFiltro.ID),
			"facturaId", new CampoFiltro("facturaId", TipoFiltro.ID),
			"fecha", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"fechaDesde", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"fechaHasta", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"facturada", new CampoFiltro("facturaId", TipoFiltro.NULO));

	private static final List<String> BUSCABLES = List.of("observacion", "proveedor.nombre");

	private static final Set<String> ORDENABLES = Set.of("id", "fecha", "proveedor.nombre");

	private final CompraConsultaJpa consulta;
	private final LineaCompraConsultaJpa lineasConsulta;
	private final ProveedorConsultaJpa proveedoresConsulta;

	public CompraPersistenciaJpa(CompraConsultaJpa consulta, LineaCompraConsultaJpa lineasConsulta,
			ProveedorConsultaJpa proveedoresConsulta) {
		this.consulta = consulta;
		this.lineasConsulta = lineasConsulta;
		this.proveedoresConsulta = proveedoresConsulta;
	}

	@Override
	public List<Compra> listar() {
		return Mapeador.aDominioCompras(consulta.findAll(),
				e -> Mapeador.aDominioLineasCompra(lineasConsulta.findByCompraId(e.getId())));
	}

	@Override
	public PaginaResultado<Compra> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadCompra> spec = Especificaciones.<EntidadCompra>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadCompra> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Compra> contenido = page.getContent().stream()
				.map(e -> Mapeador.aDominio(e,
						Mapeador.aDominioLineasCompra(lineasConsulta.findByCompraId(e.getId()))))
				.toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public Compra obtener(Long id) {
		EntidadCompra entidad = consulta.findById(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Compra no encontrada"));
		return Mapeador.aDominio(entidad, Mapeador.aDominioLineasCompra(lineasConsulta.findByCompraId(id)));
	}

	@Override
	@Transactional
	public Compra guardar(Compra compra) {
		EntidadProveedor proveedor = resolverProveedor(compra.getProveedor() == null ? null : compra.getProveedor().getId());
		EntidadCompra entidad;
		if (compra.getId() != null) {
			entidad = consulta.findById(compra.getId())
					.orElseThrow(() -> new NoEncontradoExcepcion("Compra no encontrada"));
			entidad.setFecha(compra.getFecha());
			entidad.setObservacion(compra.getObservacion());
			entidad.setProveedor(proveedor);
			entidad = consulta.save(entidad);
			lineasConsulta.deleteAll(lineasConsulta.findByCompraId(entidad.getId()));
		} else {
			entidad = new EntidadCompra();
			entidad.setFecha(compra.getFecha());
			entidad.setObservacion(compra.getObservacion());
			entidad.setFacturaId(compra.getFacturaId());
			entidad.setProveedor(proveedor);
			entidad = consulta.save(entidad);
		}
		final EntidadCompra entidadRef = entidad;
		List<EntidadLineaCompra> lineas = compra.getLineas().stream()
				.map(l -> Mapeador.aEntidad(l, entidadRef))
				.toList();
		lineasConsulta.saveAll(lineas);
		return obtener(entidad.getId());
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Compra no encontrada");
		}
		lineasConsulta.deleteAll(lineasConsulta.findByCompraId(id));
		consulta.deleteById(id);
	}

	@Override
	public boolean existeLineaProducto(String tipo, Long productoId) {
		return lineasConsulta.existsByTipoAndProductoId(tipo, productoId);
	}

	@Override
	@Transactional
	public void vincularFactura(Long compraId, Long facturaId) {
		EntidadCompra entidad = consulta.findById(compraId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Compra no encontrada"));
		entidad.setFacturaId(facturaId);
		consulta.save(entidad);
	}

	@Override
	@Transactional
	public void desvincularFactura(Long compraId) {
		consulta.findById(compraId).ifPresent(c -> {
			c.setFacturaId(null);
			consulta.save(c);
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