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
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadAsignacionHerramienta;
import com.art.inventario.persistencia.consulta.AsignacionHerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;

@Repository
@Transactional(readOnly = true)
public class AsignacionHerramientaPersistenciaJpa implements AsignacionHerramientaPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"herramientaId", new CampoFiltro("herramienta.id", TipoFiltro.ID),
			"empleadoId", new CampoFiltro("empleado.id", TipoFiltro.ID),
			"fecha", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"devuelta", new CampoFiltro("devuelta", TipoFiltro.BOOLEANO));

	private static final List<String> BUSCABLES = List.of("empleado.nombre", "lugar");

	private static final Set<String> ORDENABLES = Set.of("id", "fecha", "devuelta", "empleado.nombre");

	private final AsignacionHerramientaConsultaJpa consulta;
	private final EmpleadoConsultaJpa empleadoConsulta;
	private final HerramientaConsultaJpa herramientaConsulta;

	public AsignacionHerramientaPersistenciaJpa(AsignacionHerramientaConsultaJpa consulta,
			EmpleadoConsultaJpa empleadoConsulta, HerramientaConsultaJpa herramientaConsulta) {
		this.consulta = consulta;
		this.empleadoConsulta = empleadoConsulta;
		this.herramientaConsulta = herramientaConsulta;
	}

	@Override
	public List<AsignacionHerramienta> listar() {
		return Mapeador.aDominioAsignaciones(consulta.findAll());
	}

	@Override
	public PaginaResultado<AsignacionHerramienta> listarPagina(int pagina, int tamano) {
		Page<EntidadAsignacionHerramienta> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<AsignacionHerramienta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<AsignacionHerramienta> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadAsignacionHerramienta> spec = Especificaciones.<EntidadAsignacionHerramienta>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadAsignacionHerramienta> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<AsignacionHerramienta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public AsignacionHerramienta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Asignación no encontrada"));
	}

	@Override
	@Transactional
	public AsignacionHerramienta guardar(AsignacionHerramienta asignacion) {
		EntidadEmpleado empleado = resolverEmpleado(
				asignacion.getEmpleado() == null ? null : asignacion.getEmpleado().getId());
		EntidadHerramienta herramienta = resolverHerramienta(
				asignacion.getHerramienta() == null ? null : asignacion.getHerramienta().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(asignacion, empleado, herramienta)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Asignación no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public long contarAsignacionesActivas(Long herramientaId, Long excluirId) {
		return consulta.contarAsignacionesActivas(herramientaId, excluirId == null ? -1L : excluirId);
	}

	@Override
	public boolean tieneAsignacionActiva(Long herramientaId) {
		return consulta.existsByHerramientaIdAndDevueltaFalse(herramientaId);
	}

	@Override
	public List<AsignacionHerramienta> activasMasAntiguas(Long empleadoId, Long herramientaId, int cantidad) {
		return consulta.activasParaDevolucion(empleadoId, herramientaId,
				PageRequest.of(0, cantidad)).stream().map(Mapeador::aDominio).toList();
	}

	@Override
	public Map<Long, Long> asignacionesActivasPorHerramienta() {
		return consulta.contarAsignacionesActivasPorHerramienta().stream()
				.collect(java.util.stream.Collectors.toMap(
						(o) -> (Long) o[0],
						(o) -> (Long) o[1]));
	}

	@Override
	@Transactional
	public void desvincularHerramienta(Long herramientaId) {
		consulta.desvincularHerramienta(herramientaId);
	}

	private EntidadEmpleado resolverEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return null;
		}
		return empleadoConsulta.findById(empleadoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	private EntidadHerramienta resolverHerramienta(Long herramientaId) {
		if (herramientaId == null) {
			return null;
		}
		return herramientaConsulta.findById(herramientaId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Herramienta no encontrada"));
	}
}