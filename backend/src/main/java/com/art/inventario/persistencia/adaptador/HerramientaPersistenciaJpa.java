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
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadAsignacionHerramienta;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.puerto.salida.HerramientaPersistencia;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class HerramientaPersistenciaJpa implements HerramientaPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"marca", new CampoFiltro("marca", TipoFiltro.TEXTO_EXACTO));

	private static final List<String> BUSCABLES = List.of("nombre", "codigo", "marca", "descripcion");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "nombre", "codigo", "marca", "cantidadTotal", "ultimoCosto", "stockMinimo");

	private static final Set<String> ESTADOS = Set.of("asignadas", "disponibles", "danadas", "perdidas");

	private final HerramientaConsultaJpa consulta;

	public HerramientaPersistenciaJpa(HerramientaConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Herramienta> listar() {
		return Mapeador.aDominioHerramientas(consulta.findAll());
	}

	@Override
	public PaginaResultado<Herramienta> listarPagina(int pagina, int tamano) {
		Page<EntidadHerramienta> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Herramienta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Herramienta> listarPagina(ConsultaPaginada consultaPaginada) {
		Map<String, String> filtros = new java.util.HashMap<>(consultaPaginada.getFiltros());
		String estado = filtros.remove("estado");
		Specification<EntidadHerramienta> spec = Especificaciones.<EntidadHerramienta>filtrar(
				consultaSinEstado(consultaPaginada, filtros), CAMPOS, BUSCABLES);
		if (estado != null && !estado.isBlank()) {
			if (!ESTADOS.contains(estado)) {
				throw new DatosInvalidosExcepcion("Estado de herramienta no válido: " + estado);
			}
			spec = spec.and(filtroEstado(estado));
		}
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadHerramienta> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Herramienta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	private static ConsultaPaginada consultaSinEstado(ConsultaPaginada original, Map<String, String> filtros) {
		ConsultaPaginada c = new ConsultaPaginada();
		c.setPagina(original.getPagina());
		c.setTamano(original.getTamano());
		c.setQ(original.getQ());
		c.setOrden(original.getOrden());
		c.setDir(original.getDir());
		c.setFiltros(filtros);
		return c;
	}

	private static Specification<EntidadHerramienta> filtroEstado(String estado) {
		return (root, query, cb) -> {
			Subquery<Long> asignadas = query.subquery(Long.class);
			Root<EntidadAsignacionHerramienta> a = asignadas.from(EntidadAsignacionHerramienta.class);
			asignadas.select(cb.coalesce(cb.sum(a.get("cantidad")), 0L))
					.where(cb.and(
							cb.equal(a.get("herramienta").get("id"), root.get("id")),
							cb.isFalse(a.get("devuelta"))));

			Expression<Long> total = cb.toLong(cb.coalesce(root.get("cantidadTotal"), 0));
			Expression<Long> danada = cb.toLong(cb.coalesce(root.get("cantidadDanada"), 0));
			Expression<Long> perdida = cb.toLong(cb.coalesce(root.get("cantidadPerdida"), 0));

			return switch (estado) {
				case "asignadas" -> cb.gt(asignadas, 0L);
				case "disponibles" -> cb.gt(
						cb.diff(cb.diff(cb.diff(total, danada), perdida), asignadas), 0L);
				case "danadas" -> cb.gt(danada, 0L);
				case "perdidas" -> cb.gt(perdida, 0L);
				default -> throw new DatosInvalidosExcepcion("Estado de herramienta no válido: " + estado);
			};
		};
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Herramienta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Herramienta no encontrada"));
	}

	@Override
	public Herramienta obtenerPorCodigo(String codigo) {
		EntidadHerramienta entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Herramienta no encontrada");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return consulta.findByCodigo(codigo) != null;
	}

	@Override
	@Transactional
	public Herramienta guardar(Herramienta herramienta) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(herramienta)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Herramienta no encontrada");
		}
		consulta.deleteById(id);
	}
}