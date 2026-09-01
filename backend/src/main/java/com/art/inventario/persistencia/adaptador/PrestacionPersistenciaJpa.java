package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.PrestacionConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadPrestacion;
import com.art.inventario.puerto.salida.PrestacionPersistencia;

@Repository
@Transactional(readOnly = true)
public class PrestacionPersistenciaJpa implements PrestacionPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"tipo", new CampoFiltro("tipo", TipoFiltro.TEXTO_EXACTO),
			"obligatoria", new CampoFiltro("obligatoria", TipoFiltro.BOOLEANO),
			"activo", new CampoFiltro("activo", TipoFiltro.BOOLEANO));

	private static final List<String> BUSCABLES = List.of("nombre");

	private static final Set<String> ORDENABLES = Set.of("id", "nombre", "tipo");

	private final PrestacionConsultaJpa consulta;

	public PrestacionPersistenciaJpa(PrestacionConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Prestacion> listar() {
		return MapeadorCatalogos.aDominioPrestaciones(consulta.findAll());
	}

	@Override
	public PaginaResultado<Prestacion> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadPrestacion> spec = Especificaciones.<EntidadPrestacion>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadPrestacion> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Prestacion> contenido = page.getContent().stream().map(MapeadorCatalogos::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public Optional<Prestacion> porNombre(String nombre) {
		return consulta.findByNombre(nombre).map(MapeadorCatalogos::aDominio);
	}

	@Override
	public Prestacion obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Prestación no encontrada"));
	}

	@Override
	@Transactional
	public Prestacion guardar(Prestacion prestacion) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(prestacion)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Prestación no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		if (excluirId == null) {
			return consulta.existsByNombreIgnoreCase(nombre);
		}
		return consulta.existsByNombreIgnoreCaseAndIdNot(nombre, excluirId);
	}

	@Override
	public List<Prestacion> listarPorTipoContrato(Long tipoContratoId) {
		return MapeadorCatalogos.aDominioPrestaciones(consulta.findByTipoContratoId(tipoContratoId));
	}
}
