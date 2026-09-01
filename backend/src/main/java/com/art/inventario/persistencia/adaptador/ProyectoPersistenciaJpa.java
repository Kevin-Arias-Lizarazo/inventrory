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
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.ProyectoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadProyecto;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Repository
@Transactional(readOnly = true)
public class ProyectoPersistenciaJpa implements ProyectoPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"estado", new CampoFiltro("estado", TipoFiltro.TEXTO_EXACTO),
			"cliente", new CampoFiltro("cliente", TipoFiltro.TEXTO_EXACTO));

	private static final List<String> BUSCABLES = List.of("codigo", "nombre", "cliente", "ubicacion", "descripcion");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "codigo", "nombre", "cliente", "estado", "fechaInicio", "fechaFin");

	private final ProyectoConsultaJpa consulta;

	public ProyectoPersistenciaJpa(ProyectoConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Proyecto> listar() {
		return Mapeador.aDominioProyectos(consulta.findAll());
	}

	@Override
	public PaginaResultado<Proyecto> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadProyecto> spec = Especificaciones.<EntidadProyecto>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadProyecto> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Proyecto> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Proyecto obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proyecto no encontrado"));
	}

	@Override
	public Proyecto obtenerPorCodigo(String codigo) {
		EntidadProyecto entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Proyecto no encontrado");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return consulta.findByCodigo(codigo) != null;
	}

	@Override
	@Transactional
	public Proyecto guardar(Proyecto proyecto) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(proyecto)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Proyecto no encontrado");
		}
		consulta.deleteById(id);
	}
}