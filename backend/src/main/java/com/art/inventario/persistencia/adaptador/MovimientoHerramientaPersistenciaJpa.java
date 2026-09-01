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
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.MovimientoHerramientaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.persistencia.entidad.EntidadMovimientoHerramienta;
import com.art.inventario.puerto.salida.MovimientoHerramientaPersistencia;

@Repository
@Transactional(readOnly = true)
public class MovimientoHerramientaPersistenciaJpa implements MovimientoHerramientaPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS_MOVIMIENTOS = Map.of(
			"recursoId", new CampoFiltro("herramienta.id", TipoFiltro.ID),
			"tipo", new CampoFiltro("tipo", TipoFiltro.TEXTO_EXACTO),
			"fechaDesde", new CampoFiltro(
					(r, q, cb, v) -> cb.greaterThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA),
			"fechaHasta", new CampoFiltro(
					(r, q, cb, v) -> cb.lessThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA));

	private static final List<String> BUSCABLES_MOVIMIENTOS = List.of("observacion");

	private static final Set<String> ORDENABLES_MOVIMIENTOS = Set.of("id", "fecha", "tipo", "cantidad");

	private final MovimientoHerramientaConsultaJpa consulta;
	private final HerramientaConsultaJpa herramientaConsulta;

	public MovimientoHerramientaPersistenciaJpa(MovimientoHerramientaConsultaJpa consulta,
			HerramientaConsultaJpa herramientaConsulta) {
		this.consulta = consulta;
		this.herramientaConsulta = herramientaConsulta;
	}

	@Override
	public List<MovimientoHerramienta> listarPorHerramienta(Long herramientaId) {
		return Mapeador.aDominioMovimientosHerramienta(consulta.findByHerramientaIdOrderByFechaDesc(herramientaId));
	}

	@Override
	public List<MovimientoHerramienta> listarTodos() {
		return Mapeador.aDominioMovimientosHerramienta(consulta.findAll());
	}

	@Override
	public PaginaResultado<MovimientoHerramienta> listarTodosPagina(ConsultaPaginada c) {
		Especificaciones.validarRangoFechas(c);
		Specification<EntidadMovimientoHerramienta> spec = Especificaciones.<EntidadMovimientoHerramienta>filtrar(
				c, CAMPOS_MOVIMIENTOS, BUSCABLES_MOVIMIENTOS);
		Sort sort = Especificaciones.ordenarMovimientos(c, ORDENABLES_MOVIMIENTOS);
		Page<EntidadMovimientoHerramienta> page = consulta.findAll(spec,
				PageRequest.of(c.getPagina(), c.getTamano(), sort));
		return new PaginaResultado<>(Mapeador.aDominioMovimientosHerramienta(page.getContent()),
				c.getPagina(), c.getTamano(), page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<MovimientoHerramienta> listarPorHerramientaPagina(Long herramientaId, ConsultaPaginada c) {
		ConsultaPaginada conRecurso = c.conCopy();
		conRecurso.getFiltros().put("recursoId", String.valueOf(herramientaId));
		return listarTodosPagina(conRecurso);
	}

	@Override
	public MovimientoHerramienta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoHerramienta guardar(MovimientoHerramienta movimiento) {
		EntidadHerramienta herramienta = resolverHerramienta(
				movimiento.getHerramienta() == null ? null : movimiento.getHerramienta().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(movimiento, herramienta)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Movimiento no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	@Transactional
	public void eliminarPorHerramienta(Long herramientaId) {
		consulta.deleteByHerramientaId(herramientaId);
	}

	private EntidadHerramienta resolverHerramienta(Long herramientaId) {
		if (herramientaId == null) {
			return null;
		}
		return herramientaConsulta.findById(herramientaId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Herramienta no encontrada"));
	}
}