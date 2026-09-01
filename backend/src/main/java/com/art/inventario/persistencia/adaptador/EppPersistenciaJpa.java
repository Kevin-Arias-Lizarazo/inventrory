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
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadEpp;
import com.art.inventario.persistencia.entidad.EntidadMovimientoEpp;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.EppConsultaJpa;
import com.art.inventario.persistencia.consulta.MovimientoEppConsultaJpa;
import com.art.inventario.puerto.salida.EppPersistencia;

@Repository
@Transactional(readOnly = true)
public class EppPersistenciaJpa implements EppPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"marca", new CampoFiltro("marca", TipoFiltro.TEXTO_EXACTO));

	private static final List<String> BUSCABLES = List.of("nombre", "marca", "descripcion");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "nombre", "marca", "stock", "ultimoCosto", "stockMinimo", "fechaVencimiento");

	private static final Map<String, CampoFiltro> CAMPOS_MOVIMIENTOS = Map.of(
			"recursoId", new CampoFiltro("epp.id", TipoFiltro.ID),
			"tipo", new CampoFiltro("tipo", TipoFiltro.TEXTO_EXACTO),
			"fechaDesde", new CampoFiltro(
					(r, q, cb, v) -> cb.greaterThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA),
			"fechaHasta", new CampoFiltro(
					(r, q, cb, v) -> cb.lessThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA));

	private static final List<String> BUSCABLES_MOVIMIENTOS = List.of("observacion");

	private static final Set<String> ORDENABLES_MOVIMIENTOS = Set.of("id", "fecha", "tipo", "cantidad");

	private final EppConsultaJpa consulta;
	private final MovimientoEppConsultaJpa movimientosConsulta;

	public EppPersistenciaJpa(EppConsultaJpa consulta, MovimientoEppConsultaJpa movimientosConsulta) {
		this.consulta = consulta;
		this.movimientosConsulta = movimientosConsulta;
	}

	@Override
	public List<Epp> listar() {
		return Mapeador.aDominioEpps(consulta.findAll());
	}

	@Override
	public PaginaResultado<Epp> listarPagina(int pagina, int tamano) {
		Page<EntidadEpp> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Epp> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Epp> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadEpp> spec = Especificaciones.<EntidadEpp>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadEpp> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Epp> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Epp obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("EPP no encontrado"));
	}

	@Override
	@Transactional
	public Epp guardar(Epp epp) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(epp)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("EPP no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneMovimientos(Long id) {
		return movimientosConsulta.existsByEppId(id);
	}

	@Override
	public List<MovimientoEpp> listarMovimientos(Long eppId) {
		return Mapeador.aDominioMovimientosEpp(movimientosConsulta.findByEppIdOrderByFechaDesc(eppId));
	}

	@Override
	public List<MovimientoEpp> listarTodosMovimientos() {
		return Mapeador.aDominioMovimientosEpp(movimientosConsulta.findAll());
	}

	@Override
	public PaginaResultado<MovimientoEpp> listarTodosMovimientosPagina(ConsultaPaginada c) {
		Especificaciones.validarRangoFechas(c);
		Specification<EntidadMovimientoEpp> spec = Especificaciones.<EntidadMovimientoEpp>filtrar(
				c, CAMPOS_MOVIMIENTOS, BUSCABLES_MOVIMIENTOS);
		Sort sort = Especificaciones.ordenarMovimientos(c, ORDENABLES_MOVIMIENTOS);
		Page<EntidadMovimientoEpp> page = movimientosConsulta.findAll(spec,
				PageRequest.of(c.getPagina(), c.getTamano(), sort));
		return new PaginaResultado<>(Mapeador.aDominioMovimientosEpp(page.getContent()),
				c.getPagina(), c.getTamano(), page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<MovimientoEpp> listarMovimientosPagina(Long eppId, ConsultaPaginada c) {
		ConsultaPaginada conRecurso = c.conCopy();
		conRecurso.getFiltros().put("recursoId", String.valueOf(eppId));
		return listarTodosMovimientosPagina(conRecurso);
	}

	@Override
	public MovimientoEpp obtenerMovimiento(Long id) {
		return movimientosConsulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoEpp guardarMovimiento(MovimientoEpp movimiento) {
		EntidadEpp epp = resolverEpp(movimiento.getEpp() == null ? null : movimiento.getEpp().getId());
		return Mapeador.aDominio(movimientosConsulta.save(Mapeador.aEntidad(movimiento, epp)));
	}

	@Override
	@Transactional
	public void eliminarMovimiento(MovimientoEpp movimiento) {
		movimientosConsulta.deleteById(movimiento.getId());
	}

	private EntidadEpp resolverEpp(Long eppId) {
		if (eppId == null) {
			throw new NoEncontradoExcepcion("EPP no encontrado");
		}
		return consulta.findById(eppId)
				.orElseThrow(() -> new NoEncontradoExcepcion("EPP no encontrado"));
	}
}