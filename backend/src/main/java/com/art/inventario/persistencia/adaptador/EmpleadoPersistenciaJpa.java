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
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.AsignacionHerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaEppConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaRopaConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.PredicadoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.MinutaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadContrato;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class EmpleadoPersistenciaJpa implements EmpleadoPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"cargo", new CampoFiltro("cargo", TipoFiltro.TEXTO_EXACTO),
			"contratados", new CampoFiltro(EmpleadoPersistenciaJpa::porContratado, TipoFiltro.BOOLEANO));

	private static final List<String> BUSCABLES = List.of("codigo", "nombre", "documento", "cargo", "correo");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "codigo", "nombre", "documento", "cargo", "fechaIngreso");

	private final EmpleadoConsultaJpa consulta;
	private final MinutaConsultaJpa minutas;
	private final EntregaRopaConsultaJpa entregasRopa;
	private final EntregaEppConsultaJpa entregasEpp;
	private final AsignacionHerramientaConsultaJpa asignaciones;

	public EmpleadoPersistenciaJpa(EmpleadoConsultaJpa consulta, MinutaConsultaJpa minutas,
			EntregaRopaConsultaJpa entregasRopa, EntregaEppConsultaJpa entregasEpp,
			AsignacionHerramientaConsultaJpa asignaciones) {
		this.consulta = consulta;
		this.minutas = minutas;
		this.entregasRopa = entregasRopa;
		this.entregasEpp = entregasEpp;
		this.asignaciones = asignaciones;
	}

	@Override
	public List<Empleado> todos() {
		return consulta.findAll().stream().map(Mapeador::aDominio).toList();
	}

	@Override
	public PaginaResultado<Empleado> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadEmpleado> spec = Especificaciones.<EntidadEmpleado>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadEmpleado> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Empleado> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Empleado obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	@Override
	public Empleado obtenerPorCodigo(String codigo) {
		EntidadEmpleado entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Empleado no encontrado");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return consulta.findByCodigo(codigo) != null;
	}

	@Override
	@Transactional
	public Empleado guardar(Empleado empleado) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(empleado)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Empleado no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneReferencias(Long id) {
		return minutas.existsByEmpleadoId(id)
				|| entregasRopa.existsByEmpleadoId(id)
				|| entregasEpp.existsByEmpleadoId(id)
				|| asignaciones.existsByEmpleadoId(id);
	}

	/**
	 * Predicado para el filtro legacy {@code contratados} (true/false): un empleado
	 * est&aacute; contratado si tiene al menos un contrato en estado ACTIVO.
	 * Respeta la sem&aacute;ntica exacta de {@code ContratoPersistencia.empleadosContratados()}.
	 */
	private static Predicate porContratado(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb, String valor) {
		Subquery<Long> sub = query.subquery(Long.class);
		Root<EntidadContrato> contrato = sub.from(EntidadContrato.class);
		sub.select(contrato.get("id"));
		sub.where(cb.equal(contrato.get("empleado").get("id"), root.get("id")),
				cb.equal(contrato.get("estado"), Contrato.ACTIVO));
		return Boolean.parseBoolean(valor.trim())
				? cb.exists(sub)
				: cb.not(cb.exists(sub));
	}
}