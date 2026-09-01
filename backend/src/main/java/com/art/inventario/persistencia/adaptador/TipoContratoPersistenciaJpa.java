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
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.TipoContratoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;

@Repository
@Transactional(readOnly = true)
public class TipoContratoPersistenciaJpa implements TipoContratoPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"activo", new CampoFiltro("activo", TipoFiltro.BOOLEANO));

	private static final List<String> BUSCABLES = List.of("nombre", "descripcion");

	private static final Set<String> ORDENABLES = Set.of("id", "nombre");

	private final TipoContratoConsultaJpa consulta;

	public TipoContratoPersistenciaJpa(TipoContratoConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<TipoContrato> listar() {
		return MapeadorCatalogos.aDominioTiposContrato(consulta.findAll());
	}

	@Override
	public PaginaResultado<TipoContrato> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadTipoContrato> spec = Especificaciones.<EntidadTipoContrato>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadTipoContrato> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<TipoContrato> contenido = page.getContent().stream().map(MapeadorCatalogos::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public Optional<TipoContrato> porNombre(String nombre) {
		return consulta.findByNombre(nombre).map(MapeadorCatalogos::aDominio);
	}

	@Override
	public TipoContrato obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Tipo de contrato no encontrado"));
	}

	@Override
	@Transactional
	public TipoContrato guardar(TipoContrato tipoContrato) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(tipoContrato)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Tipo de contrato no encontrado");
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
}
