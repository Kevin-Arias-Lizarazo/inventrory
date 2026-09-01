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
import com.art.inventario.dominio.Minuta;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.MinutaConsultaJpa;
import com.art.inventario.persistencia.consulta.ProyectoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadMinuta;
import com.art.inventario.persistencia.entidad.EntidadProyecto;
import com.art.inventario.puerto.salida.MinutaPersistencia;

@Repository
@Transactional(readOnly = true)
public class MinutaPersistenciaJpa implements MinutaPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"empleadoId", new CampoFiltro("empleado.id", TipoFiltro.ID),
			"proyectoId", new CampoFiltro("proyecto.id", TipoFiltro.ID),
			"fecha", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"fechaDesde", new CampoFiltro("fecha", TipoFiltro.FECHA),
			"fechaHasta", new CampoFiltro("fecha", TipoFiltro.FECHA));

	private static final List<String> BUSCABLES = List.of("empleado.nombre");

	private static final Set<String> ORDENABLES = Set.of("id", "fecha", "hora", "empleado.nombre");

	private final MinutaConsultaJpa consulta;
	private final EmpleadoConsultaJpa empleadoConsulta;
	private final ProyectoConsultaJpa proyectoConsulta;

	public MinutaPersistenciaJpa(MinutaConsultaJpa consulta, EmpleadoConsultaJpa empleadoConsulta,
			ProyectoConsultaJpa proyectoConsulta) {
		this.consulta = consulta;
		this.empleadoConsulta = empleadoConsulta;
		this.proyectoConsulta = proyectoConsulta;
	}

	@Override
	public List<Minuta> listar() {
		return Mapeador.aDominioMinutas(consulta.findAll());
	}

	@Override
	public PaginaResultado<Minuta> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadMinuta> spec = Especificaciones.<EntidadMinuta>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadMinuta> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Minuta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Minuta> listarPaginaRecientes(int pagina, int tamano) {
		Page<EntidadMinuta> page = consulta.findAll(PageRequest.of(pagina, tamano,
				Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("hora"), Sort.Order.desc("id"))));
		List<Minuta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public Minuta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Minuta no encontrada"));
	}

	@Override
	@Transactional
	public Minuta guardar(Minuta minuta) {
		EntidadEmpleado empleado = resolverEmpleado(
				minuta.getEmpleado() == null ? null : minuta.getEmpleado().getId());
		EntidadProyecto proyecto = resolverProyecto(
				minuta.getProyecto() == null ? null : minuta.getProyecto().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(minuta, empleado, proyecto)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Minuta no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneMinutasConProyecto(Long proyectoId) {
		return consulta.existsByProyectoId(proyectoId);
	}

	private EntidadEmpleado resolverEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return null;
		}
		return empleadoConsulta.findById(empleadoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	private EntidadProyecto resolverProyecto(Long proyectoId) {
		if (proyectoId == null) {
			return null;
		}
		return proyectoConsulta.findById(proyectoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proyecto no encontrado"));
	}
}